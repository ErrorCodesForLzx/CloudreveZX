package com.ecsoft.cloudreve;

import static com.ecsoft.cloudreve.system.UriUtil.getPath;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout llUploadFile;
    private LinearLayout llUploadProgressModule;
    private TextView tvChunkUploadProgress;
    private TextView tvUploadChunkSize;
    private TextView tvUploadTotalProgress;
    private ProgressBar pbUploadProgress;
    private Button btnCancelUpload;
    private String uploadDir = "/";
    /* 存储策略ID */
    private String storagePolicyId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }


        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewData() {
        // 获取传递的上传目录
        Intent intent = getIntent();
        uploadDir = intent.getStringExtra("uploadDir");
        String encodeUploadDir = uploadDir.replaceAll("/", "%2F");
        Thread getStoragePolicyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_tree+encodeUploadDir), new HashMap<>(), GlobalRunningConfiguration.authentication_cookie_token);
                JSONTokener  tokener  = new JSONTokener(responseText);
                try {
                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                    // 获取当先目录的存储策略ID
                    JSONObject policyJsonObject = responseJsonObj.getJSONObject("data").getJSONObject("policy");
                    storagePolicyId = policyJsonObject.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getStoragePolicyThread.start();
    }

    private void initViewEvent() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        llUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!storagePolicyId.equals("")){
                    // 跳转到文件选择视图
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent,"请选择需要上传的文件"),0xF0001);
                } else {
                    Toast.makeText(UploadActivity.this, "对不起，存储策略获取失败，请稍后", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            // TODO：执行上传逻辑
            // 获取信息
            Toast.makeText(this, "文件即将上传，请勿关闭此界面", Toast.LENGTH_SHORT).show();
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                try {
                    String selectPath = uri.getPath();
                    uploadFile(selectPath);
                } catch (Exception e) {
                    Toast.makeText(this, "文件信息获取失败，可能是权限不足！", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }
            }
            //4.4以后
            String selectPath = getPath(this, uri);
            uploadFile(selectPath);

        }

    }


    private void initViewComponent() {

        ivBack = findViewById(R.id.iv_back);
        llUploadFile = findViewById(R.id.ll_upload_file);
        llUploadProgressModule = findViewById(R.id.ll_upload_progress_module);
        tvChunkUploadProgress = findViewById(R.id.tv_chunk_upload_progress);
        tvUploadChunkSize = findViewById(R.id.tv_upload_chunk_size);
        tvUploadTotalProgress = findViewById(R.id.tv_total_upload_progress);
        pbUploadProgress = findViewById(R.id.pb_upload_progress);
        btnCancelUpload = findViewById(R.id.btn_cancel_upload);

    }

    private void uploadFile(String filePath){
        // 将上传设置为可见状态
        llUploadProgressModule.setVisibility(View.VISIBLE);
        filePath = filePath.replaceAll("raw:/","");
        File selectedFile = new File(filePath);
        String selectedFileName = selectedFile.getName();
        // 通告服务器准备上传，让服务器充分准备上传IO流
        Thread announcementServerUpload = new Thread(new Runnable() {
            @Override
            public void run() {
                // 构建请求体
                JSONObject requestJsonObj = new JSONObject();
                try {
                    requestJsonObj.put("last_modified",System.currentTimeMillis()/1000L);
                    requestJsonObj.put("name",selectedFileName);
                    requestJsonObj.put("path",uploadDir);
                    requestJsonObj.put("policy_id",storagePolicyId);
                    requestJsonObj.put("size",selectedFile.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // 通告服务器
                String responseText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_tell_upload_file), new HashMap<>(), requestJsonObj, GlobalRunningConfiguration.authentication_cookie_token);
                JSONTokener tokener = new JSONTokener(responseText);
                // 获取文件总大小
                long localFileSize = selectedFile.length();
                // 服务器通告返回的上传会话UUID（重要）
                // 形如：XXXXXXXX-XXXX-XXXX-XXXXXXXXXXXX
                String uploadSessionUuid = "XXXXXXXX-XXXX-XXXX-XXXXXXXXXXXX";
                // 服务器返回的最大承受的分段大小（单位：字节）
                long uploadChunkSize = 0L;
                // 分段段数
                int uploadChunkCount = 0;
                // 上传链接集
                List<String> uploadUrls = new ArrayList<>();
                try {
                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                    JSONObject data = responseJsonObj.getJSONObject("data");
                    uploadSessionUuid = data.getString("sessionID");
                    uploadChunkSize = data.getLong("chunkSize");
                    // 计算分段 文件总大小 / 分段大小
                    uploadChunkCount = (int)(localFileSize / uploadChunkSize);
                    // 构建上传链接集
                    for (int i=0;i<=uploadChunkCount-1;i++){
                        Map<String,String> params = new LinkedHashMap<>();
                        params.put("sessionId",uploadSessionUuid);
                        params.put("chunkIndex",String.valueOf(i));
                        String url = NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_upload_file,params);
                        uploadUrls.add(url);
                    }
                    Log.e("CLOUDREVE_ZX","文件分段成功~~~");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        announcementServerUpload.start();
    }
}