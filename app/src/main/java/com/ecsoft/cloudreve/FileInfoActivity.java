package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
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
import com.ecsoft.cloudreve.download.entity.DownloadTaskPO;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;
import com.ecsoft.cloudreve.service.DownloadService;
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
    private static final int HANDLER_RENAME_FILE_SUCCESS      = 0x50001;
    private static final int HANDLER_RENAME_FILE_FAILED       = 0x50002;



    // ????????????
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("CLOUDREVE_ZX","?????????FileInfoActivity.class????????????....");
            // ????????????????????????????????????????????????
            downloadMessenger = new Messenger(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // ????????????
            downloadMessenger = null;
        }
    };
    private Messenger downloadMessenger;


    // ?????????????????????
    private ProgressDialog pdLoadingDownloadUrl;

    // ????????????
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
    private ImageView ivInfoActionRename;
    private ImageView ivInfoActionDownload;

    // ??????????????????
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_GET_DOWNLOAD_URL_SUCCESS:{
                    if (pdLoadingDownloadUrl != null){
                        Bundle data = message.getData();
                        String downloadUrl = data.getString("downloadUrl");


                        // ????????????===============================================????????????
                        // BATA:?????????????????????
                        // ??????????????????
                        Bundle bundle = new Bundle();
                        DownloadTaskPO taskPO = new DownloadTaskPO();
                        taskPO.setFileId(currentEntity.getId());
                        taskPO.setTotalSize(currentEntity.getSize());
                        taskPO.setDownloadUrl(downloadUrl);
                        taskPO.setDownloadedSize(0L);
                        taskPO.setFileName(currentEntity.getName());
                        bundle.putSerializable("task",taskPO);
                        Message msg = new Message();
                        msg.setData(bundle);
                        msg.what = DownloadService.HANDLER_START_DOWNLOAD_TASK;
                        try {
                            // ????????????
                            downloadMessenger.send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } finally {
                            Toast.makeText(FileInfoActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        // ????????????===============================================????????????
                         /* new AlertDialog.Builder(FileInfoActivity.this)
                                .setTitle("????????????????????")
                                .setIcon(R.drawable.ic_icon_dialog_construction)
                                .setMessage("???????????????????????????????????????????????????APP???????????????")
                                .setPositiveButton("???????????????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri uri = Uri.parse(downloadUrl);
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent); // ?????????????????????
                                    }
                                })
                                .setNegativeButton("??????????????????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager cmb = (ClipboardManager)FileInfoActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                        cmb.setText(downloadUrl.trim());
                                        Toast.makeText(FileInfoActivity.this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show(); */
                        // ?????????????????????
                        pdLoadingDownloadUrl.dismiss();
                    }
                    break;
                }
                case HANDLER_DELETE_FILE_SUCCESS:{
                    // ??????????????????
                    Toast.makeText(FileInfoActivity.this, "?????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    setResult(0x10001); // ???????????????????????????????????????
                    finish();
                    break;
                }
                case HANDLER_DELETE_FILE_FAILED:{
                    // ??????????????????
                    Bundle data = message.getData();
                    String msg = data.getString("msg");
                    new AlertDialog.Builder(FileInfoActivity.this)
                            .setTitle("????????????")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setMessage(msg)
                            .setCancelable(false)
                            .show();
                    break;
                }
                case HANDLER_GET_PREVIEW_SUCCESS:{
                    pdLoadingDownloadUrl.dismiss();
                    Bundle data = message.getData();
                    String fileId      = data.getString("fileId");
                    String downloadUrl = data.getString("downloadUrl");
                    // ??????????????????
                    switch (FileTypeJudgeUtil.getFileSuffix(currentEntity.getName())){
                        case ".xls":
                        case ".xlsx":
                        case ".ppt":
                        case ".pptx":
                        case ".doc":
                        case ".docx":{ // ?????????OFFICE??????????????????Microsoft Office????????????
                            Intent intent = new Intent(FileInfoActivity.this,OfficePreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            startActivity(intent); // ????????????
                            break;
                        }
                        case ".png":
                        case ".jpg":
                        case ".gif":{
                            // ???????????????
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","image.html");
                            startActivity(intent); // ???????????????????????????
                            break;
                        }
                        case ".mp3":
                        case ".flac":{
                            // ???????????????
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","audio.html");
                            startActivity(intent); // ???????????????????????????
                            break;
                        }
                        case ".mp4":
                        case ".avi":
                        case ".mov":{
                            // ???????????????
                            Intent intent = new Intent(FileInfoActivity.this,MultiMediaPreviewActivity.class);
                            intent.putExtra("src",downloadUrl);
                            intent.putExtra("multiType","video.html");
                            startActivity(intent); // ???????????????????????????
                            break;
                        }


                    }



                    break;
                }
                case HANDLER_SHARE_FILE_SUCCESS:{
                    Toast.makeText(FileInfoActivity.this, "?????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                }
                case HANDLER_SHARE_FILE_FAILED:{
                    Toast.makeText(FileInfoActivity.this, "???????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                }
                case HANDLER_RENAME_FILE_SUCCESS:{
                    Toast.makeText(FileInfoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
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
            supportActionBar.hide(); // ???????????????ActionBar
        }
        initViewComponent();
        initViewData(); // ??????????????????
        initViewEvent();
        initService();
        initLibrary(); // ??????????????????????????????
    }

    private void initService(){
        Intent intent = new Intent(FileInfoActivity.this, DownloadService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }
    private void initLibrary() {
    }

    private void initViewEvent() {
        // ?????????????????????
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // ????????????
            }
        });
        ivInfoActionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdLoadingDownloadUrl = new ProgressDialog(FileInfoActivity.this);
                pdLoadingDownloadUrl.setMessage("?????????????????????...");
                pdLoadingDownloadUrl.setCancelable(false);
                pdLoadingDownloadUrl.show();
                Thread getDownloadUrlThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // ????????????
                        DbSettingsService settingsService = new DbSettingsService(FileInfoActivity.this);
                        String cookies = settingsService.getSettings("authentication_cookie_token");
                        String responseUrlText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_download) + "/" + currentEntity.getId(), new HashMap<>(), cookies);
                        JSONTokener tokener = new JSONTokener(responseUrlText);
                        try {
                            JSONObject responseUrlJsonObj = (JSONObject) tokener.nextValue();
                            String downloadUrl = responseUrlJsonObj.getString("data"); // ?????????????????????
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
                        .setTitle("?????????????????????")
                        .setIcon(R.drawable.ic_icon_dialog_warning)
                        .setMessage("??????????????????????????????????????????...")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // TODO???????????????????????????
                                Thread deleteFileThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String cookie = GlobalRunningConfiguration.authentication_cookie_token;
                                        // ???????????????
                                        JSONObject requestBody = new JSONObject();

                                        try {
                                            // ???????????????????????????????????????????????????
                                            requestBody.put("dirs",new JSONArray());
                                            // ???????????????????????????
                                            JSONArray jsonArray = new JSONArray();
                                            jsonArray.put(currentEntity.getId()); // ????????????ID
                                            requestBody.put("items",jsonArray); // ???Json???????????????????????????items???
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // ????????????????????????
                                        String responseText = OKHTTPUtil.sendDelete(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_delete), new HashMap<>(), requestBody, cookie);
                                        // ???????????????Json??????
                                        JSONTokener tokener = new JSONTokener(responseText);
                                        try {
                                            JSONObject responseJsonObj = (JSONObject) tokener.nextValue();

                                            if (responseJsonObj.getInt("code") == 0) {
                                                handler.sendEmptyMessage(HANDLER_DELETE_FILE_SUCCESS);
                                            } else {
                                                // ??????????????????
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
                        .setNegativeButton("?????????",null)
                        .setCancelable(false)
                        .show();
            }
        });
        ivInfoActionPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdLoadingDownloadUrl = new ProgressDialog(FileInfoActivity.this);
                pdLoadingDownloadUrl.setMessage("?????????????????????...");
                pdLoadingDownloadUrl.setCancelable(false);
                pdLoadingDownloadUrl.show();
                Thread getDownloadUrlThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // ????????????
                        DbSettingsService settingsService = new DbSettingsService(FileInfoActivity.this);
                        String cookies = settingsService.getSettings("authentication_cookie_token");
                        String responseUrlText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_download) + "/" + currentEntity.getId(), new HashMap<>(), cookies);
                        JSONTokener tokener = new JSONTokener(responseUrlText);
                        try {
                            JSONObject responseUrlJsonObj = (JSONObject) tokener.nextValue();
                            String downloadUrl = responseUrlJsonObj.getString("data"); // ?????????????????????
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
        ivInfoActionRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????????????????
                // ???????????????????????????
                View dialogView = LayoutInflater.from(FileInfoActivity.this)
                        .inflate(R.layout.dialog_rename_file,null);
                EditText etFileNewName = dialogView.findViewById(R.id.et_file_new_name);
                // ?????????????????????
                new AlertDialog.Builder(FileInfoActivity.this)
                        .setTitle("????????????")
                        .setIcon(R.drawable.ic_dialog_icon_rename)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (etFileNewName.getText().toString().isEmpty()){
                                    Toast.makeText(FileInfoActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                                }
                                Thread renameFileThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JSONObject requestJsonObj = new JSONObject();
                                        try {
                                            requestJsonObj.put("action","rename");
                                            requestJsonObj.put("new_name",etFileNewName.getText().toString());
                                            JSONObject srcJsonObj = new JSONObject();
                                            srcJsonObj.put("dirs",new JSONArray());
                                            JSONArray items = new JSONArray();
                                            items.put(currentEntity.getId());
                                            srcJsonObj.put("items",items);
                                            String responseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_rename_file), new HashMap<>(), requestJsonObj, GlobalRunningConfiguration.authentication_cookie_token);
                                            JSONTokener tokener = new JSONTokener(responseText);
                                            JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                            if (responseJsonObj.getInt("code") == 0){
                                                // ????????????
                                                handler.sendEmptyMessage(HANDLER_RENAME_FILE_SUCCESS);
                                            } else {
                                                Message message = new Message();
                                                message.what = HANDLER_RENAME_FILE_FAILED;
                                                Bundle bundle = new Bundle();
                                                bundle.putString("msg", responseJsonObj.getString("msg"));
                                                message.setData(bundle);
                                                handler.sendMessage(message); // ???????????????????????????
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                renameFileThread.start();
                            }
                        })
                        .setNegativeButton("??????",null)
                        .show();

            }
        });
        ivInfoActionShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????????????????????
                // ????????????
                View dialogView = LayoutInflater.from(FileInfoActivity.this).inflate(R.layout.dialog_share_file, null);
                // ?????????????????????
                CheckBox cbSharePassword = dialogView.findViewById(R.id.cb_share_password);
                CheckBox cbShareExp = dialogView.findViewById(R.id.cb_share_exp);
                CheckBox cbSharePreview = dialogView.findViewById(R.id.cb_share_allowed_preview);
                TextInputLayout tilSharePassword = dialogView.findViewById(R.id.til_share_password);
                EditText etSharePassword = dialogView.findViewById(R.id.et_share_password);
                LinearLayout llShareExp = dialogView.findViewById(R.id.ll_share_exp);
                Spinner spShareDownloadCount = dialogView.findViewById(R.id.sp_share_download_count);
                Spinner spShareTimeExp = dialogView.findViewById(R.id.sp_share_download_exp_time);
                // ?????????????????????
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
                // ???????????????
                new AlertDialog.Builder(FileInfoActivity.this)
                        .setView(dialogView)
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String password;
                                int downloadExp;
                                int expireTime;
                                boolean preview;


                                password = cbSharePassword.isChecked() ? etSharePassword.getText().toString() : "";
                                downloadExp = cbShareExp.isChecked () ?  getResources().getIntArray(R.array.sp_download_count)[spShareDownloadCount.getSelectedItemPosition()]  : -1;
                                expireTime = cbShareExp.isChecked () ? (getResources().getIntArray(R.array.sp_time_exp)[spShareTimeExp.getSelectedItemPosition()]) * 86400 : 86400; // ??????????????????
                                preview = cbSharePreview.isChecked();
                                // ??????JSON??????
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
                                        // TODO :??????????????????
                                        String responseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_share_file), new HashMap<>(), requestObj, cookies);
                                        JSONTokener tokener = new JSONTokener(responseText);
                                        try {
                                            JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                            if (responseJsonObj.getInt("code") == 0){
                                                // ????????????
                                                String sharedUrl = responseJsonObj.getString("data");
                                                String copiedTestMessage = "???????????????????????????"+currentEntity.getName()+sharedUrl+"???????????????????????????????????????Cloudreve ZX????????????????????????-----???????????????Cloudreve ZX???APP?????????Cloudreve ZX???http://tordax.com/b8vd";
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
                        .setNegativeButton("??????",null)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initViewData() {
        Intent intent = getIntent(); // ???????????????????????????
        Bundle data = intent.getBundleExtra("entity");
        currentEntity = (FileTreePO) data.getSerializable("entity"); // ????????????????????????
        ivInfoFileTypeShow.setImageResource(FileTypeJudgeUtil.getImageResourceByFileName(currentEntity.getName())); // ????????????????????????
        tvInfoFileName.setText(currentEntity.getName());// ??????????????????
        tvInfoImgFileSize.setVisibility(currentEntity.getPic().equals("") ? View.GONE : View.VISIBLE); // ????????????????????????????????????
        tvInfoImgFileSize.setText("("+currentEntity.getPic().replace(",","??")+")"); // ?????????????????????(--??--)??????
        // ????????????
        Date date = new Date(); // ??????????????????
        Date createDate = new Date(); // ????????????
        try {
            date = TimeFormatUtil.formatTimeByString(currentEntity.getDate());
            createDate = TimeFormatUtil.formatTimeByString(currentEntity.getCreateDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String timeFormation = "yyyy???MM???dd??? HH:mm:ss";
        tvInfoFileDate.setText(TimeFormatUtil.dateToString(date,timeFormation));
        tvInfoFileCreateDate.setText(TimeFormatUtil.dateToString(createDate,timeFormation));
        Integer fileSize = currentEntity.getSize();
        FileSizePO reasonableFileSize = FileStorageUtil.getReasonableFileSize(fileSize, 0);
        // ????????????????????????????????????
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
        ivInfoActionRename = findViewById(R.id.iv_info_action_rename);
        ivInfoActionShare = findViewById(R.id.iv_info_action_share);
/*

127.0.0.1 download.parallels.com
127.0.0.1 update.parallels.com
127.0.0.1 desktop.parallels.com
127.0.0.1 download.parallels.com.cdn.cloudflare.net
127.0.0.1 update.parallels.com.cdn.cloudflare.net
127.0.0.1 desktop.parallels.com.cdn.cloudflare.net
127.0.0.1 www.parallels.cn
127.0.0.1 www.parallels.com
127.0.0.1 reportus.parallels.com
127.0.0.1 parallels.com
127.0.0.1 parallels.cn
127.0.0.1 pax-manager.myparallels.com
127.0.0.1 myparallels.com
127.0.0.1 my.parallels.com

 */

    }
}