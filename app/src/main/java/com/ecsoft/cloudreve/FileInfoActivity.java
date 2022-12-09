package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.DbSettingsService;
import com.ecsoft.cloudreve.download.DownloadTaskListUtil;
import com.ecsoft.cloudreve.download.entity.DownloadTaskPO;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;
import com.ecsoft.cloudreve.storage.FileStorageUtil;
import com.ecsoft.cloudreve.storage.entity.FileSizePO;
import com.ecsoft.cloudreve.system.ClipboardUtil;
import com.ecsoft.cloudreve.time.TimeFormatUtil;
import com.ecsoft.cloudreve.ui.fileType.FileTypeJudgeUtil;
import com.ecsoft.cloudreve.ui.recyclerView.entity.FileTreePO;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

public class FileInfoActivity extends AppCompatActivity {

    private FileTreePO currentEntity;

    private static final int HANDLER_GET_DOWNLOAD_URL_SUCCESS = 0x10001;
    private static final int HANDLER_DELETE_FILE_SUCCESS      = 0x20001;
    private static final int HANDLER_DELETE_FILE_FAILED       = 0x20002;
    private static final int HANDLER_GET_PREVIEW_SUCCESS      = 0x30001;
    private static final int HANDLER_GET_PREVIEW_FAILED       = 0x30002;
    private static final int HANDLER_SHARE_FILE_SUCCESS       = 0x40001;
    private static final int HANDLER_SHARE_FILE_FAILED        = 0x40002;

    // 定义全局加载框
    private ProgressDialog pdLoadingDownloadUrl;

    // 声明组件
    private ImageView ivBack;
    private ImageView ivInfoFileTypeShow;
    private TextView  tvInfoFileName;
    private TextView  tvInfoImgFileSize;
    private TextView  tvInfoFileDate;
    private TextView  tvInfoFileCreateDate;
    private TextView  tvInfoFileSize;
    private TextView  tvInfoFilePath;
    private ImageView ivInfoActionPreview;
    private ImageView ivInfoActionShare;
    private ImageView ivInfoActionDelete;
    private ImageView ivInfoActionEdit;
    private ImageView ivInfoActionDownload;

    // 定义消息队列
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_GET_DOWNLOAD_URL_SUCCESS:{
                    if (pdLoadingDownloadUrl != null){
                        Bundle data = message.getData();
                        String fileId      = data.getString("fileId");
                        String downloadUrl = data.getString("downloadUrl");
                        //DownloadTaskPO task = new DownloadTaskPO();
                        //task.setFileId(currentEntity.getId());
                        //task.setNowStatus(DownloadTaskPO.STATUS_PAUSED);
                        //task.setTotalByte(currentEntity.getSize());
                        //task.setDownloadedByte(0);
                        //DownloadTaskListUtil.insertUnfinishedTask(FileInfoActivity.this,task); // 添加任务
                        //Toast.makeText(FileInfoActivity.this, "文件已经添加到下载队列，稍后统一下载~", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(FileInfoActivity.this)
                                .setTitle("🚧施工公告🚧")
                                .setIcon(R.drawable.ic_icon_dialog_construction)
                                .setMessage("由于下载库未兼容部分机型，暂不支持APP内部下载！")
                                .setPositiveButton("浏览器下载", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri uri = Uri.parse(downloadUrl);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent); // 打开浏览器操作
                                    }
                                })
                                .setNegativeButton("复制到剪贴板", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager cmb = (ClipboardManager)FileInfoActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                        cmb.setText(downloadUrl.trim());
                                        Toast.makeText(FileInfoActivity.this, "下载链接复制到剪贴板了", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                        pdLoadingDownloadUrl.dismiss();
                    }
                    break;
                }
                case HANDLER_DELETE_FILE_SUCCESS:{
                    // 文件删除成功
                    Toast.makeText(FileInfoActivity.this, "文件删除成功，服务器刷新可能会存在延迟", Toast.LENGTH_SHORT).show();
                    setResult(0x10001); // 响应文件已经被修改的响应码
                    finish();
                }
                case HANDLER_DELETE_FILE_FAILED:{
                    // 文件删除失败
                    Bundle data = message.getData();
                    String msg = data.getString("msg");
                    new AlertDialog.Builder(FileInfoActivity.this)
                            .setTitle("删除失败")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setMessage(msg)
                            .setCancelable(false)
                            .show();
                }
                case HANDLER_GET_PREVIEW_SUCCESS:{
                    pdLoadingDownloadUrl.dismiss();
                    Bundle data = message.getData();
                    String fileId      = data.getString("fileId");
                    String downloadUrl = data.getString("downloadUrl");
                    // 判断文件类型
                    switch (FileTypeJudgeUtil.getFileSuffix(currentEntity.getName())){
                        case ".xls":
                        case ".xlsx":
                        case ".ppt":
                        case ".pptx":
                        case ".doc":
                        case ".docx":{ // 代表是OFFICE文件，跳转到Microsoft Office预览界面
                            Intent intent = new Intent(FileInfoActivity.this,OfficePreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            startActivity(intent); // 启动预览
                            break;
                        }
                        case ".png":
                        case ".jpg":
                        case ".gif":{
                            // 多媒体预览
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","image.html");
                            startActivity(intent); // 启动多媒体预览视图
                            break;
                        }
                        case ".mp3":
                        case ".flac":{
                            // 多媒体预览
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","audio.html");
                            startActivity(intent); // 启动多媒体预览视图
                            break;
                        }
                        case ".mp4":
                        case ".avi":
                        case ".mov":{
                            // 多媒体预览
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","video.html");
                            startActivity(intent); // 启动多媒体预览视图
                            break;
                        }


                    }



                    break;
                }
                case HANDLER_SHARE_FILE_SUCCESS:{
                    Toast.makeText(FileInfoActivity.this, "分享地址已经复制到系统剪贴板，分享给小伙伴吧！", Toast.LENGTH_SHORT).show();
                    break;
                }
                case HANDLER_SHARE_FILE_FAILED:{
                    Toast.makeText(FileInfoActivity.this, "分享失败，请尝试重新分享！", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_info);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.hide(); // 隐藏可用的ActionBar
        }


        initViewComponent();
        initViewData(); // 加载视图数据
        initViewEvent();
    }

    private void initViewEvent() {
        // 获取返回键单击
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 关闭视图
            }
        });
        ivInfoActionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdLoadingDownloadUrl = new ProgressDialog(FileInfoActivity.this);
                pdLoadingDownloadUrl.setMessage("获取下载链接中...");
                pdLoadingDownloadUrl.setCancelable(false);
                pdLoadingDownloadUrl.show();
                Thread getDownloadUrlThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 发送请求
                        DbSettingsService settingsService = new DbSettingsService(FileInfoActivity.this);
                        String cookies = settingsService.getSettings("authentication_cookie_token");
                        String responseUrlText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_download) + "/" + currentEntity.getId(), new HashMap<>(), cookies);
                        JSONTokener tokener = new JSONTokener(responseUrlText);
                        try {
                            JSONObject responseUrlJsonObj = (JSONObject) tokener.nextValue();
                            String downloadUrl = responseUrlJsonObj.getString("data"); // 获取到下载链接
                            Bundle bundle = new Bundle();
                            bundle.putString("downloadUrl",downloadUrl);
                            Message message = new Message();
                            message.setData(bundle);
                            message.what = HANDLER_GET_DOWNLOAD_URL_SUCCESS;
                            handler.sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                getDownloadUrlThread.start();
            }
        });
        ivInfoActionDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new  AlertDialog.Builder(FileInfoActivity.this)
                        .setTitle("确认删除文件？")
                        .setIcon(R.drawable.ic_icon_dialog_warning)
                        .setMessage("有些东西一旦销毁，就不复存在...")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // TODO：执行删除文件逻辑
                                Thread deleteFileThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String cookie = GlobalRunningConfiguration.authentication_cookie_token;
                                        // 构建请求体
                                        JSONObject requestBody = new JSONObject();

                                        try {
                                            // 由于是删除文件所以构建空的目录对象
                                            requestBody.put("dirs",new JSONArray());
                                            // 构建删除的文件对象
                                            JSONArray jsonArray = new JSONArray();
                                            jsonArray.put(currentEntity.getId()); // 传入文件ID
                                            requestBody.put("items",jsonArray); // 将Json数组添加到欲删除的items中
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // 发出删除请求命令
                                        String responseText = OKHTTPUtil.sendDelete(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_delete), new HashMap<>(), requestBody, cookie);
                                        // 解析响应的Json对象
                                        JSONTokener tokener = new JSONTokener(responseText);
                                        try {
                                            JSONObject responseJsonObj = (JSONObject) tokener.nextValue();

                                            if (responseJsonObj.getInt("code") == 0) {
                                                handler.sendEmptyMessage(HANDLER_DELETE_FILE_SUCCESS);
                                            } else {
                                                // 创建消息对象
                                                Message message = new Message();
                                                message.what = HANDLER_DELETE_FILE_FAILED;
                                                Bundle bundle = new Bundle();
                                                bundle.putString("msg",responseJsonObj.getString("msg"));
                                                message.setData(bundle);
                                                handler.sendMessage(message);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                deleteFileThread.start();
                            }
                        })
                        .setNegativeButton("再想想",null)
                        .setCancelable(false)
                        .show();
            }
        });
        ivInfoActionPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdLoadingDownloadUrl = new ProgressDialog(FileInfoActivity.this);
                pdLoadingDownloadUrl.setMessage("获取下载链接中...");
                pdLoadingDownloadUrl.setCancelable(false);
                pdLoadingDownloadUrl.show();
                Thread getDownloadUrlThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 发送请求
                        DbSettingsService settingsService = new DbSettingsService(FileInfoActivity.this);
                        String cookies = settingsService.getSettings("authentication_cookie_token");
                        String responseUrlText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_download) + "/" + currentEntity.getId(), new HashMap<>(), cookies);
                        JSONTokener tokener = new JSONTokener(responseUrlText);
                        try {
                            JSONObject responseUrlJsonObj = (JSONObject) tokener.nextValue();
                            String downloadUrl = responseUrlJsonObj.getString("data"); // 获取到下载链接
                            Bundle bundle = new Bundle();
                            bundle.putString("downloadUrl",downloadUrl);
                            Message message = new Message();
                            message.setData(bundle);
                            message.what = HANDLER_GET_PREVIEW_SUCCESS;
                            handler.sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                getDownloadUrlThread.start();
            }
        });
        ivInfoActionShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示分享对话框
                // 解析视图
                View dialogView = LayoutInflater.from(FileInfoActivity.this).inflate(R.layout.dialog_share_file, null);
                // 获取视图的组件
                CheckBox cbSharePassword = dialogView.findViewById(R.id.cb_share_password);
                CheckBox cbShareExp = dialogView.findViewById(R.id.cb_share_exp);
                CheckBox cbSharePreview = dialogView.findViewById(R.id.cb_share_allowed_preview);
                TextInputLayout tilSharePassword = dialogView.findViewById(R.id.til_share_password);
                EditText etSharePassword = dialogView.findViewById(R.id.et_share_password);
                LinearLayout llShareExp = dialogView.findViewById(R.id.ll_share_exp);
                Spinner spShareDownloadCount = dialogView.findViewById(R.id.sp_share_download_count);
                Spinner spShareTimeExp = dialogView.findViewById(R.id.sp_share_download_exp_time);
                // 设置组件的事件
                cbSharePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tilSharePassword.setVisibility(cbSharePassword.isChecked() ? View.VISIBLE : View.GONE);
                    }
                });
                cbShareExp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        llShareExp.setVisibility(cbShareExp.isChecked() ? View.VISIBLE : View.GONE);
                    }
                });
                // 加载对话框
                new AlertDialog.Builder(FileInfoActivity.this)
                        .setView(dialogView)
                        .setPositiveButton("分享", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String password;
                                int downloadExp;
                                int expireTime;
                                boolean preview;


                                password = cbSharePassword.isChecked() ? etSharePassword.getText().toString() : "";
                                downloadExp = cbShareExp.isChecked () ?  getResources().getIntArray(R.array.sp_download_count)[spShareDownloadCount.getSelectedItemPosition()]  : -1;
                                expireTime = cbShareExp.isChecked () ? (getResources().getIntArray(R.array.sp_time_exp)[spShareTimeExp.getSelectedItemPosition()]) * 86400 : 86400; // 将天转换为秒
                                preview = cbSharePreview.isChecked();
                                // 构建JSON对象
                                JSONObject requestObj = new JSONObject();
                                try {
                                    requestObj.put("downloads",downloadExp);
                                    requestObj.put("expire",expireTime);
                                    requestObj.put("id",currentEntity.getId());
                                    requestObj.put("is_dir",false);
                                    requestObj.put("password",password);
                                    requestObj.put("preview",preview);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                String cookies = GlobalRunningConfiguration.authentication_cookie_token;
                                Thread sendShareThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // TODO :发送分享请求
                                        String responseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_share_file), new HashMap<>(), requestObj, cookies);
                                        JSONTokener tokener = new JSONTokener(responseText);
                                        try {
                                            JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                            if (responseJsonObj.getInt("code") == 0){
                                                // 没有错误
                                                String sharedUrl = responseJsonObj.getString("data");
                                                String copiedTestMessage = "小伙伴给你分享了："+currentEntity.getName()+sharedUrl+"文件。点击连接，或者打开「Cloudreve ZX」自动识别下载。-----分享来自「Cloudreve ZX」APP，下载Cloudreve ZX：http://tordax.com/b8vd";
                                                ClipboardUtil.copyToSystemClipboard(FileInfoActivity.this,copiedTestMessage);
                                                handler.sendEmptyMessage(HANDLER_SHARE_FILE_SUCCESS);
                                            } else {
                                                handler.sendEmptyMessage(HANDLER_SHARE_FILE_FAILED);
                                            }
                                        } catch (JSONException e) {
                                            handler.sendEmptyMessage(HANDLER_SHARE_FILE_SUCCESS);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                sendShareThread.start();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initViewData() {
        Intent intent = getIntent(); // 获取传递的意图对象
        Bundle data = intent.getBundleExtra("entity");
        currentEntity = (FileTreePO) data.getSerializable("entity"); // 获取传递的实体类
        ivInfoFileTypeShow.setImageResource(FileTypeJudgeUtil.getImageResourceByFileName(currentEntity.getName())); // 获取文件类型图标
        tvInfoFileName.setText(currentEntity.getName());// 得到文件名称
        tvInfoImgFileSize.setVisibility(currentEntity.getPic().equals("") ? View.GONE : View.VISIBLE); // 判断是否显示图片尺寸组件
        tvInfoImgFileSize.setText("("+currentEntity.getPic().replace(",","×")+")"); // 将图片的尺寸以(--×--)显示
        // 获取时间
        Date date = new Date(); // 最后修改时间
        Date createDate = new Date(); // 创建时间
        try {
            date = TimeFormatUtil.formatTimeByString(currentEntity.getDate());
            createDate = TimeFormatUtil.formatTimeByString(currentEntity.getCreateDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String timeFormation = "yyyy年MM月dd日 HH:mm:ss";
        tvInfoFileDate.setText(TimeFormatUtil.dateToString(date,timeFormation));
        tvInfoFileCreateDate.setText(TimeFormatUtil.dateToString(createDate,timeFormation));
        Integer fileSize = currentEntity.getSize();
        FileSizePO reasonableFileSize = FileStorageUtil.getReasonableFileSize(fileSize, 0);
        // 显示获取到的合理文件大小
        tvInfoFileSize.setText(reasonableFileSize.getSize()+" "+reasonableFileSize.getUnit());
        tvInfoFilePath.setText(currentEntity.getPath());
    }

    private void initViewComponent() {
        ivBack = findViewById(R.id.iv_back);
        ivInfoFileTypeShow = findViewById(R.id.iv_file_type_show);
        tvInfoFileName = findViewById(R.id.tv_info_file_name);
        tvInfoImgFileSize = findViewById(R.id.tv_info_img_file_size);
        tvInfoFileDate = findViewById(R.id.tv_info_file_date);
        tvInfoFileCreateDate = findViewById(R.id.tv_info_file_create_date);
        tvInfoFileSize = findViewById(R.id.tv_info_file_size);
        tvInfoFilePath = findViewById(R.id.tv_info_file_path);
        ivInfoActionPreview = findViewById(R.id.iv_info_action_preview);
        ivInfoActionDelete = findViewById(R.id.iv_info_action_delete);
        ivInfoActionDownload = findViewById(R.id.iv_info_action_download);
        ivInfoActionEdit = findViewById(R.id.iv_info_action_edit);
        ivInfoActionShare = findViewById(R.id.iv_info_action_share);

    }
}