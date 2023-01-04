package com.ecsoft.cloudreve;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.CopyDBUtils;
import com.ecsoft.cloudreve.database.DbSettingsService;

import java.io.File;

public class BuildActivity extends AppCompatActivity {

    private EditText etServerAddress;
    private EditText etServerPort;
    private Button   btnEnterCloudDisk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);

        initViewComponent();
        initViewEvent();
    }

    private void initViewEvent() {
        btnEnterCloudDisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 单击事件被触发
                String serverAddress = "";
                String serverPort = "";
                serverAddress = etServerAddress.getText().toString();
                serverPort= etServerPort.getText().toString();
                if (!serverAddress.equals("") && !serverPort.equals("")){
                    if (serverAddress.charAt(serverAddress.length() - 1) == '/'){
                        // 判断输入的地址最后有没有 "/"，如：https://baidu.com/ 转为——https://baidu.com
                        serverAddress = serverAddress.replace("/","");
                    }

                    try {
                        int serverPortInt = Integer.parseInt(serverPort);
                        if (!(serverPortInt > 0 && serverPortInt < 65535)){
                            new AlertDialog.Builder(BuildActivity.this)
                                    .setTitle("错误")
                                    .setMessage("请输入合法的 Cloudreve 服务端端口\n端口 ∈ (0,65535]")
                                    .setPositiveButton("返回填写",null)
                                    .setCancelable(false)
                                    .setIcon(R.drawable.ic_icon_dialog_warning)
                                    .show();
                            return;
                        }
                    } catch (Exception e){
                        new AlertDialog.Builder(BuildActivity.this)
                                .setTitle("错误")
                                .setMessage("请输入合法的 Cloudreve 服务端端口\n端口 ∈ (0,65535]")
                                .setPositiveButton("返回填写",null)
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_icon_dialog_warning)
                                .show();
                        return;
                    }
                    // 各方面都校验完毕开始创建数据库并写入数据
                    createDatabase();
                    // 写出数据
                    DbSettingsService dbSettingsService = new DbSettingsService(BuildActivity.this);
                    dbSettingsService.setSettings("network_address",serverAddress);
                    dbSettingsService.setSettings("network_port",serverPort);
                    GlobalRunningConfiguration.network_address = serverAddress;
                    GlobalRunningConfiguration.network_port    = serverPort;

                    // 载入开屏Activity，结束客户端的搭建
                    Intent intent = new Intent(BuildActivity.this,OpenScreenActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    new AlertDialog.Builder(BuildActivity.this)
                            .setTitle("提示")
                            .setMessage("请输入完整的地址和端口号，保证客户端正常连接服务器哟~")
                            .setPositiveButton("返回填写",null)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .show();
                }
            }
        });
    }

    private void initViewComponent() {
        etServerAddress   = findViewById(R.id.et_server_addr);
        etServerPort      = findViewById(R.id.et_server_port);
        btnEnterCloudDisk = findViewById(R.id.btn_enter_your_cloud_disk);
    }

    private void createDatabase(){
        String fileStr = "/data/data/" + getPackageName() + "/data.db";
        File file = new File(fileStr);
        if (!file.exists()){ // 数据库文件不存在复制数据库
            CopyDBUtils.copyDbFile(BuildActivity.this,"data.db");
            Intent intent = new Intent(BuildActivity.this,WelcomeActivity.class);
            startActivity(intent);
        }
    }
}