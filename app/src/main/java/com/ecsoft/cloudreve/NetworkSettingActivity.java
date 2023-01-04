package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.DbSettingsService;
import com.ecsoft.cloudreve.network.CheckNetworkConnection;

public class NetworkSettingActivity extends AppCompatActivity {

    private EditText etNetworkServer;
    private EditText etNetworkPort;
    private Button   btnCancel;
    private Button   btnApply;

    private ProgressDialog pdCheckNetworkConnection;

    private static final int HANDLER_CHECK_NETWORK_SUCCESS = 0x10001;
    private static final int HANDLER_CHECK_NETWORK_FAILED  = 0x10002;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_CHECK_NETWORK_SUCCESS:{
                    DbSettingsService settingsService = new DbSettingsService(NetworkSettingActivity.this);
                    // 网络通过
                    pdCheckNetworkConnection.dismiss();
                    GlobalRunningConfiguration.network_address = etNetworkServer.getText().toString();
                    GlobalRunningConfiguration.network_port    = etNetworkPort.getText().toString();
                    settingsService.setSettings("network_address",GlobalRunningConfiguration.network_address);
                    settingsService.setSettings("network_port",GlobalRunningConfiguration.network_port);
                    new AlertDialog.Builder(NetworkSettingActivity.this)
                            .setTitle("成功")
                            .setMessage("客户端，能够和服务器建立通讯，并且已经应用设置，请重启软件!")
                            .setCancelable(false)
                            .setPositiveButton("重启软件", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 杀死进程
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            })
                            .show();
                    break;
                }
                case HANDLER_CHECK_NETWORK_FAILED:{
                    pdCheckNetworkConnection.dismiss();
                    new AlertDialog.Builder(NetworkSettingActivity.this)
                            .setTitle("网络错误")
                            .setMessage("客户端无法和您指定的服务器通讯，请检查输入")
                            .setCancelable(false)
                            .setPositiveButton("重试",null)
                            .show();
                    break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setting);

        initViewComponent();
        initViewEvent();
    }

    private void initViewEvent() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 结束视图
            }
        });
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 设置加载视图
                pdCheckNetworkConnection = new ProgressDialog(NetworkSettingActivity.this);
                pdCheckNetworkConnection.setMessage("校验服务器连通性中...");
                pdCheckNetworkConnection.setCancelable(false);
                // 当设置按钮被单击，执行设置逻辑
                Thread getCheckNetworkActive = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 判断网络是否与服务器能建立TCP连通性。
                        if (CheckNetworkConnection.check(etNetworkServer.getText().toString()+":"+etNetworkPort.getText().toString())) {
                            // 网络检测通过
                            handler.sendEmptyMessage(HANDLER_CHECK_NETWORK_SUCCESS);
                        } else {
                            // 网络检测不通过
                            handler.sendEmptyMessage(HANDLER_CHECK_NETWORK_FAILED);
                        }

                    }
                });
                getCheckNetworkActive.start();
            }
        });
    }

    private void initViewComponent() {
        etNetworkServer = findViewById(R.id.et_network_server);
        etNetworkPort   = findViewById(R.id.et_network_port);
        btnCancel       = findViewById(R.id.btn_cancel);
        btnApply        = findViewById(R.id.btn_apply);
    }
}