package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText etAuthEmail;
    private EditText etAuthCaptcha;
    private ImageView ivAuthCaptcha;
    private Button btnSendResetEmail;
    private TextView tvLogin;
    private LinearLayout llForgetCaptchaModule;

    private String auth_email;
    private String auth_captcha;
    private boolean hasForgetCaptcha;

    private static final int HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS = 0x10001;
    private static final int HANDLER_REFRESH_CAPTCHA = 0x20001;
    private static final int HANDLER_NO_CAPTCHA = 0x30001;
    private static final int HANDLER_FORGET_SUCCESS = 0x40001;
    private static final int HANDLER_FORGET_FAILED = 0x40002;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            switch (message.what){
                case HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS:{
                    Bundle data = message.getData();
                    String captchaImageBase64 = data.getString("captcha");
                    captchaImageBase64 = captchaImageBase64.replace("data:image/png;base64,",""); // 截断Base64的MIME前缀
                    byte[] decodedString = Base64.decode(captchaImageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivAuthCaptcha.setImageBitmap(decodedByte); // 将转码的数据加载到ImageView中
                    break;
                }
                case HANDLER_REFRESH_CAPTCHA:{
                    refreshCaptchaCode();
                    break;
                }
                case HANDLER_NO_CAPTCHA:{
                    // 隐藏验证码
                    llForgetCaptchaModule.setVisibility(View.GONE);
                    break;
                }
                case HANDLER_FORGET_SUCCESS:{
                    // 忘记密码邮件发送成功
                    Toast.makeText(ForgetPasswordActivity.this, "含有重置密码的链接已经发送到您的邮箱", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgetPasswordActivity.this,AuthenticationLoginActivity.class);
                    intent.putExtra("email",auth_email);
                    startActivity(intent);
                    finish();
                    break;
                }
                case HANDLER_FORGET_FAILED:{
                    // 获取失败信息
                    Bundle data = message.getData();

                    // 失败
                    new AlertDialog.Builder(ForgetPasswordActivity.this)
                            .setTitle("错误")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setMessage(data.getString("msg"))
                            .setCancelable(false)
                            .setPositiveButton("我知道了",null)
                            .show();
                }
            }

            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewData() {
        // 更新视图的数据信息
        Thread getConfigThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 发送config请求给服务器
                String responseConfigText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new HashMap<>());
                // 转换获取到的配置信息到Json对象
                JSONTokener tokener = new JSONTokener(responseConfigText);
                try {
                    JSONObject responseConfigJsonObj = (JSONObject) tokener.nextValue();
                    // 读取Json的Data对象
                    JSONObject dataJsonObj = responseConfigJsonObj.getJSONObject("data");
                    // 判断登录是否需要人机校验
                    if ((hasForgetCaptcha = dataJsonObj.getBoolean("forgetCaptcha"))) {
                        refreshCaptchaCode(); // 刷新一次验证码
                    } else {
                        handler.sendEmptyMessage(HANDLER_NO_CAPTCHA);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        getConfigThread.start();
    }

    private void initViewEvent() {
        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 当召回密码按钮被单击
                auth_email = etAuthEmail.getText().toString();
                auth_captcha = etAuthCaptcha.getText().toString();
                if (hasForgetCaptcha){
                    // 如果服务器要求输入验证码，而用户没有输入提示
                    Toast.makeText(ForgetPasswordActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(HANDLER_REFRESH_CAPTCHA);
                    return;
                }
                if (auth_email.isEmpty() && auth_captcha.isEmpty()){
                    new AlertDialog.Builder(ForgetPasswordActivity.this)
                            .setTitle("提示")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setMessage("请输入完整的信息，后继续")
                            .setPositiveButton("我知道了",null)
                            .setCancelable(false)
                            .show();
                } else {
                    // 用户输入完整
                    Thread forgetThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject requestJsonObj = new JSONObject();
                            try {
                                requestJsonObj.put("userName",auth_email);
                                requestJsonObj.put("captchaCode",hasForgetCaptcha ? auth_captcha:"");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String responseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_forget_password), new HashMap<>(), requestJsonObj, GlobalRunningConfiguration.authentication_cookie_token);
                            JSONTokener  tokener = new JSONTokener(responseText);
                            try {
                                JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                int code = responseJsonObj.getInt("code");
                                String msg = responseJsonObj.getString("msg");
                                if (code == 0){
                                    // 代表没有错误
                                    handler.sendEmptyMessage(HANDLER_FORGET_SUCCESS);
                                } else {
                                    // 发生错误
                                    Bundle data = new Bundle();
                                    data.putString("msg",msg);
                                    Message message = new Message();
                                    message.what = HANDLER_FORGET_FAILED;
                                    message.setData(data);
                                    handler.sendMessage(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    forgetThread.start();

                }
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgetPasswordActivity.this,AuthenticationLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void initViewComponent() {
        etAuthEmail = findViewById(R.id.et_auth_email);
        etAuthCaptcha = findViewById(R.id.et_auth_captcha);
        ivAuthCaptcha = findViewById(R.id.iv_auth_captcha);
        btnSendResetEmail = findViewById(R.id.btn_send_reset_email);
        llForgetCaptchaModule = findViewById(R.id.ll_forget_captcha_module);
        tvLogin = findViewById(R.id.tv_login);
    }

    /**
     * 定义刷新验证码逻辑
     */
    private void refreshCaptchaCode(){
        ivAuthCaptcha.setImageResource(R.drawable.captcha); // 替换为加载中的图片避免网络问题导致时间过久
        etAuthCaptcha.setText(""); // 将输入的验证码置空
        Thread getCaptchaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String captchaResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_capture), new HashMap<>(), "", true, ForgetPasswordActivity.this);
                JSONTokener tokener = new JSONTokener(captchaResponseText);
                try {
                    JSONObject captchaResponseJsonObj = (JSONObject) tokener.nextValue();
                    String captchaImageBase64 = captchaResponseJsonObj.getString("data"); // 获取服务器传递的Base64数据
                    Bundle data = new Bundle();
                    data.putString("captcha",captchaImageBase64);
                    Message message = new Message();
                    message.what = HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS;
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getCaptchaThread.start();
    }
}