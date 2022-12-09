package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.CopyDBUtils;
import com.ecsoft.cloudreve.network.CheckNetworkConnection;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;

public class OpenScreenActivity extends AppCompatActivity {

    private static final int HANDLER_NETWORK_CHECK_SUCCESS = 0x10001;
    private static final int HANDLER_NETWORK_CHECK_FAILED  = 0x10002;
    private static final int HANDLER_OPEN_SCREEN_SURPLUS   = 0x20001;
    private static final int HANDLER_OPEN_SCREEN_FINISH   = 0x20002;
    private TextView tvSurplus;

    // 创建开屏线程
    private Thread loadingTimerThread;

    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message message) {

            switch (message.what){
                case HANDLER_NETWORK_CHECK_FAILED:{
                    // 网络连接失败
                    new AlertDialog.Builder(OpenScreenActivity.this)
                            .setMessage("网络检查失败，无法连接到您的服务器\n错误代码：E_CODE_"+message.what)
                            .setTitle("网络错误")
                            .setPositiveButton("进入网络设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(OpenScreenActivity.this,NetworkSettingActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                    break;
                }
                case HANDLER_OPEN_SCREEN_SURPLUS:{
                    Bundle data = message.getData();
                    int surplusSecond = (int) data.get("surplusSecond");
                    tvSurplus.setText(surplusSecond+" S");
                    break;
                }
                case HANDLER_OPEN_SCREEN_FINISH:{
                    Intent intent = new Intent(OpenScreenActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_screen);

        // 取消ActionBar
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        // 判断是否为第一次运行
        if (isFirstRun()){
            Intent intent = new Intent(OpenScreenActivity.this,WelcomeActivity.class);
            startActivity(intent);
            // 结束本Activity
            finish();
        }

        // 加载组件
        initViewComponent();
        initViewEvent();
        // APP自检工作
        appSelfCheck();


    }

    private void initViewComponent(){
        tvSurplus = findViewById(R.id.tv_surplus);
    }

    private void initViewEvent(){
        tvSurplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 结束线程
                // Intent intent = new Intent(OpenScreenActivity.this,MainActivity.class);
                // startActivity(intent);
                // finish();
            }
        });
    }


    private void appSelfCheck(){
        // 检测是否APP被第一次启动
        if (!isFirstRun()){
            // 加载全局配置
            GlobalRunningConfiguration.init(OpenScreenActivity.this);
            NetworkTrafficUrlBuilder.init(GlobalRunningConfiguration.network_address,GlobalRunningConfiguration.network_port);
            // 如果不是第一次启动，开始网络连通性
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (CheckNetworkConnection.check()) {
                        // 判断登录的有效性
                        Thread judgeLoginTokenThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 获取系统数据库保存的Cookie

                                //发送config请求判断当前用户组
                                String configResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new ArrayMap<>(), GlobalRunningConfiguration.authentication_cookie_token, false,OpenScreenActivity.this);
                                // 将获取到的响应文本转换为JSON对象
                                JSONTokener tokener = new JSONTokener(configResponseText);
                                try {
                                    JSONObject configResponseJsonObj = (JSONObject) tokener.nextValue();
                                    // 获取用户的权限级别
                                    JSONObject responseUserPermission = configResponseJsonObj.getJSONObject("data").getJSONObject("user");
                                    // 判断是否token对应的是匿名用户
                                    if (responseUserPermission.getBoolean("anonymous")){
                                        // 如果匿名用户，则跳转登录界面
                                        Intent intent = new Intent(OpenScreenActivity.this,AuthenticationLoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // 如果为false的话代表为注册用户，执行跳转后续操作
                                        // 计算开屏倒计时的线程
                                        loadingTimerThread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int i = 0; i <= 3;i++){

                                                    int s = i;
                                                    int surplusSecond = 3 - i ;
                                                    try {
                                                        Thread.sleep(1000);
                                                        Message message = new Message();
                                                        Bundle data = new Bundle();
                                                        data.putInt("surplusSecond",surplusSecond);
                                                        message.setData(data);
                                                        message.what = HANDLER_OPEN_SCREEN_SURPLUS;
                                                        handler.sendMessage(message);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                handler.sendEmptyMessage(HANDLER_OPEN_SCREEN_FINISH);
                                            }
                                        });
                                        loadingTimerThread.start();

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        judgeLoginTokenThread.start(); // 启动线程

                    } else {
                        // 网络校验失败
                        handler.sendEmptyMessage(HANDLER_NETWORK_CHECK_FAILED);
                        return;
                    }
                }
            });
            // 启动校验线程
            thread.start();



        }
    }

    private boolean isFirstRun(){
        String fileStr = "/data/data/" + getPackageName() + "/data.db";
        File file = new File(fileStr);
        return !file.exists();
    }
    private void createDatabase(){
        String fileStr = "/data/data/" + getPackageName() + "/data.db";
        File file = new File(fileStr);
        if (!file.exists()){ // 数据库文件不存在复制数据库
            CopyDBUtils.copyDbFile(OpenScreenActivity.this,"data.db");
            Intent intent = new Intent(OpenScreenActivity.this,WelcomeActivity.class);
            startActivity(intent);
        }
    }
}