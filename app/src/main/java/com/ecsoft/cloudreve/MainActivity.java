package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.DbSettingsService;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;
import com.ecsoft.cloudreve.service.DownloadService;
import com.ecsoft.cloudreve.storage.FileStorageUtil;
import com.ecsoft.cloudreve.storage.entity.FileSizePO;
import com.ecsoft.cloudreve.time.GreetWordUtil;
import com.ecsoft.cloudreve.ui.recyclerView.adapter.FileTreeAdapter;
import com.ecsoft.cloudreve.ui.recyclerView.entity.FileTreePO;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /**
     * ????????????????????????????????????????????????????????????????????????
     */
    private ArrayList<String> currentFileTreePath;
    private ArrayList<FileTreePO> currentFileTree;
    // ???????????? all video image audio doc
    private String filterType = "all";
    private static final int HANDLER_FILE_TREE_GET_SUCCESS = 0x10001;
    private static final int HANDLER_GOOD_SENTENCE_SUCCESS = 0x20001;
    private static final int HANDLER_GOOD_NICKNAME_SUCCESS = 0x30001;
    private static final int HANDLER_GET_STORAGE_SUCCESS = 0x40001;
    private static final int HANDLER_GET_STORAGE_FAILED = 0x40002;
    private static final int HANDLER_CREATE_DIR_SUCCESS = 0x50001;
    private static final int HANDLER_CREATE_DIR_FAILED  = 0x50002;


    private ImageView ivUserInfo; // ????????????
    private SwipeRefreshLayout srRefreshFileTree;
    private RecyclerView rvFileTree;
    private TextView tvDirPath;
    private ImageView ivBackPath;
    private TextView dialog_tvGoodSentence;
    private TextView dialog_tvTitle;
    private TextView dialog_tvUserStorage;
    private ImageView dialog_ivSettings;
    private Button dialog_btnLogout;
    private ProgressBar dialog_pbStorage;
    private ConstraintLayout clAboutMe;
    private ImageView ivDownloadTask;
    // ???????????????
    private TabLayout tlFilterChoose;
    private TabItem tbFilterAll;
    private TabItem tbFilterVideo;
    private TabItem tbFilterImage;
    private TabItem tbFilterAudio;
    private TabItem tbFilterDocument;
    private LinearLayout llPathAction;
    private LinearLayout llAppTitle;
    private LinearLayout llPath;
    private FloatingActionButton fabMenuCreateDir;
    private FloatingActionButton fabMenuUploadFile;


    private Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case HANDLER_FILE_TREE_GET_SUCCESS: {
                    // ??????????????????????????????
                    Bundle data = message.getData();
                    String responseFileTreeText = data.getString("responseFileTreeText");
                    // ???????????????Json????????????JsonObj
                    JSONTokener tokener = new JSONTokener(responseFileTreeText);
                    try {
                        JSONObject responseFileTreeJsonObj = (JSONObject) tokener.nextValue();
                        JSONArray fileTreeJsonArray = responseFileTreeJsonObj.getJSONObject("data").getJSONArray("objects");
                        currentFileTree.clear(); // ???????????????????????????
                        // ????????????????????????
                        for (int i = 0; i <= fileTreeJsonArray.length() - 1; i++) {
                            // ?????????????????????????????????JsonObj
                            JSONObject itemJsonObj = fileTreeJsonArray.getJSONObject(i);
                            FileTreePO itemEntityObj = new FileTreePO();
                            // ??????JsonObj??????????????????????????????
                            itemEntityObj.setId(itemJsonObj.getString("id"));
                            itemEntityObj.setName(itemJsonObj.getString("name"));
                            itemEntityObj.setPath(itemJsonObj.getString("path"));
                            itemEntityObj.setPic(itemJsonObj.getString("pic"));
                            itemEntityObj.setSize(itemJsonObj.getInt("size"));
                            itemEntityObj.setType(itemJsonObj.getString("type"));
                            itemEntityObj.setDate(itemJsonObj.getString("date"));
                            itemEntityObj.setCreateDate(itemJsonObj.getString("create_date"));
                            itemEntityObj.setSourceEnabled(itemJsonObj.getBoolean("source_enabled"));
                            // ??????????????????????????????
                            currentFileTree.add(itemEntityObj);
                        }
                        // ??????Adapter
                        FileTreeAdapter fileTreeAdapter = new FileTreeAdapter(currentFileTree, MainActivity.this);
                        // ?????????????????????
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); // ??????????????????????????????????????????
                        rvFileTree.setLayoutManager(layoutManager);
                        // ????????????????????????????????????
                        fileTreeAdapter.setOnFileClickListener(new FileTreeAdapter.OnFileClickListener() {
                            @Override
                            public void onClick(Integer position, FileTreePO nowFileFree) {
                                // ???????????????????????????????????????
                                if (nowFileFree.getType().equals("dir")) {
                                    // ????????????
                                    // ?????????????????????????????????????????????
                                    currentFileTreePath.add(nowFileFree.getName());
                                    refreshFileTree(); // ????????????

                                } else {
                                    //  ???????????????????????????
                                    Bundle data = new Bundle();
                                    data.putSerializable("entity", nowFileFree);
                                    Intent intent = new Intent(MainActivity.this, FileInfoActivity.class);
                                    intent.putExtra("entity", data);
                                    startActivityForResult(intent, 0x10001); // ????????????????????????
                                }
                            }
                        });
                        rvFileTree.setAdapter(fileTreeAdapter); // ??????Adapter
                        srRefreshFileTree.setRefreshing(false); // ??????????????????
                        // ????????????
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case HANDLER_GOOD_SENTENCE_SUCCESS: {
                    Bundle data = message.getData();
                    dialog_tvGoodSentence.setText(data.getString("text"));
                    break;
                }
                case HANDLER_GOOD_NICKNAME_SUCCESS: {
                    Bundle data = message.getData();
                    dialog_tvTitle.setText("Hi???" + data.getString("nickName") + "???" + GreetWordUtil.getGreetWord() + "???");
                    break;
                }
                case HANDLER_GET_STORAGE_SUCCESS: {
                    Bundle data = message.getData();
                    long usedStorage = data.getLong("usedStorage");
                    long totalStorage = data.getLong("totalStorage");
                    FileSizePO reasonableFileSizeUsed = FileStorageUtil.getReasonableFileSize(usedStorage, 0);
                    FileSizePO reasonableFileSizeTotal = FileStorageUtil.getReasonableFileSize(totalStorage, 0);
                    dialog_tvUserStorage.setText("???????????????" + reasonableFileSizeUsed.getSize() + reasonableFileSizeUsed.getUnit() + "/"
                            + reasonableFileSizeTotal.getSize() + reasonableFileSizeTotal.getUnit());
                    int usedPercent = 0; // ??????????????????????????????
                    if (totalStorage != 0L) {
                        // ???????????????  ??? ????????? / ????????? ???* 100
                        usedPercent = (int) ((usedStorage / totalStorage) * 100);
                    }
                    dialog_pbStorage.setMax(100);
                    dialog_pbStorage.setProgress(usedPercent);
                    break;
                }
                case HANDLER_CREATE_DIR_FAILED:{
                    Bundle data = message.getData();
                    Toast.makeText(MainActivity.this, data.getString("msg"), Toast.LENGTH_SHORT).show();
                    break;
                }
                case HANDLER_CREATE_DIR_SUCCESS:{
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    setFiler(filterType);
                }
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initViewComponent();
        initViewData();
        initViewEvent();
        initService();
    }

    private void initService() {
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        //
        startService(intent);// ??????????????????
    }

    private long mExitTime;

    //????????????????????????

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        // ?????????????????????????????????
        if (currentFileTreePath.size() > 0) {
            currentFileTreePath.remove(currentFileTreePath.size() - 1); // ?????????????????????????????????
            // ??????????????????
            refreshFileTree();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "???????????????????????? Cloudreve", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);

            }
        }


    }

    private void initViewComponent() {
        ivUserInfo = findViewById(R.id.iv_user_info);
        srRefreshFileTree = findViewById(R.id.sr_refresh_file_tree);
        rvFileTree = findViewById(R.id.rv_file_tree);
        tvDirPath = findViewById(R.id.tv_dir_path);
        ivBackPath = findViewById(R.id.iv_back_path);
        tlFilterChoose = findViewById(R.id.tl_filter_choose);
        tbFilterAll = findViewById(R.id.ti_filter_all);
        tbFilterVideo = findViewById(R.id.ti_filter_video);
        tbFilterImage = findViewById(R.id.ti_filter_image);
        tbFilterAudio = findViewById(R.id.ti_filter_audio);
        llPathAction = findViewById(R.id.ll_path_action);
        llAppTitle = findViewById(R.id.ll_app_title);
        llPath = findViewById(R.id.ll_path);
        fabMenuCreateDir = findViewById(R.id.fab_menu_create_dir);
        fabMenuUploadFile = findViewById(R.id.fab_menu_upload_file);
        ivDownloadTask = findViewById(R.id.iv_download_manage);

    }

    private void initViewData() {
        // ???????????????????????????
        currentFileTreePath = new ArrayList<>();
        currentFileTree = new ArrayList<>();
        refreshFileTree();
    }

    private void initViewEvent() {
        srRefreshFileTree.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setFiler(filterType); // ??????????????????
            }
        });
        // ??????????????????????????????
        ivBackPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????????????????????????????????????????????????????
                if (currentFileTreePath.size() != 0) {
                    // ?????????????????????
                    currentFileTreePath.remove(currentFileTreePath.size() - 1); // ?????????????????????????????????
                    // ??????????????????
                    refreshFileTree();
                }
            }
        });
        ivUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserInfo(); // ????????????
            }
        });
        ivDownloadTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DownloadTaskManageActivity.class);
                startActivity(intent);
            }
        });
        /*
        tbFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFiler("all");
            }
        });
        tbFilterVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFiler("video");
            }
        });
        tbFilterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFiler("image");
            }
        });
        tbFilterAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFiler("audio");
            }
        });*/
        tlFilterChoose.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TabLayout.TabView view = tab.view;
                int position = tab.getPosition();
                switch (position) {
                    case 0: {
                        setFiler("all");
                        break;
                    }
                    case 1: {
                        setFiler("video");
                        break;
                    }
                    case 2: {
                        setFiler("image");
                        break;
                    }
                    case 3: {
                        setFiler("audio");
                        break;
                    }
                    case 4: {
                        setFiler("doc");
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fabMenuCreateDir.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_create_dir, null);

                TextView tvCreateAt = dialogView.findViewById(R.id.tv_create_at);
                tvCreateAt.setText("????????????"+getDirStringByListNoneCoding());

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle("????????????").setIcon(R.drawable.ic_dialog_icon_action_create_dir).setView(dialogView)
                        .setNegativeButton("??????", null);
                builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etDirName = dialogView.findViewById(R.id.et_auth_email);
                        String dirName = String.valueOf(etDirName.getText());
                        if (!dirName.isEmpty()){
                            String cookies = GlobalRunningConfiguration.authentication_cookie_token;
                            Thread createDirThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // ??????????????????Json??????
                                    String createAt = getDirStringByListNoneCoding() +"/"+dirName;
                                    JSONObject requestObj = new JSONObject();
                                    try {
                                        requestObj.put("path",createAt);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    String responseText = OKHTTPUtil.sendPut(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_file_create_dir), new HashMap<>(), requestObj, cookies);
                                    JSONTokener tokener = new JSONTokener(responseText);
                                    try {
                                        JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                        if (responseJsonObj.getInt("code") == 0) {
                                            handler.sendEmptyMessage(HANDLER_CREATE_DIR_SUCCESS);
                                        } else {
                                            // ????????????
                                            Message message = new Message();
                                            message.what = HANDLER_CREATE_DIR_FAILED;
                                            Bundle data = new Bundle();
                                            data.putString("msg",responseJsonObj.getString("msg"));
                                            message.setData(data);
                                            handler.sendMessage(message);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            createDirThread.start();
                        } else {
                            Toast.makeText(MainActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

                builder.show();
            }
        });
        fabMenuUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "??????????????????????????????????????????...", Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "???????????????BATA??????????????????????????????", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "??????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,UploadActivity.class);
                intent.putExtra("uploadDir",getDirStringByListNoneCoding());
                startActivityForResult(intent,0x10001);

            }
        });
    }


    private void setFiler(String type) {
        filterType = type; // ??????
        refreshFileTree(); // ??????
        switch (type) {
            case "all": {
                tvDirPath.setText(getDirStringByListNoneCoding());
                // UI??????
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_all_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_all_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_all);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_all);
                break;
            }
            case "video": {
                tvDirPath.setText("???????????????????????????");
                // UI??????
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_video_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_video_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_video);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_video);
                break;
            }
            case "image": {
                tvDirPath.setText("???????????????????????????");
                // UI??????
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_image_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_image_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_image);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_image);
                break;
            }
            case "audio": {
                tvDirPath.setText("???????????????????????????");
                // UI??????
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_audio_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_audio_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_audio);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_audio);
                break;
            }
            case "doc": {
                tvDirPath.setText("???????????????????????????");
                // UI??????
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_doc_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_doc_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_doc);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_doc);
                break;
            }
        }

    }

    /**
     * ???????????????????????????
     */
    private void showUserInfo() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.UserInfoDialog);
        View inflatedView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_user_info, null);
        //  TODO: ?????????????????????????????????
        dialog_tvGoodSentence = inflatedView.findViewById(R.id.tv_good_sentence);
        dialog_tvTitle = inflatedView.findViewById(R.id.dialog_tv_title);
        dialog_tvUserStorage = inflatedView.findViewById(R.id.tv_user_storage);
        dialog_pbStorage = inflatedView.findViewById(R.id.pb_storage);
        clAboutMe = inflatedView.findViewById(R.id.cl_about_me);
        dialog_ivSettings = inflatedView.findViewById(R.id.dialog_iv_settings);
        dialog_btnLogout = inflatedView.findViewById(R.id.dialog_btn_logout);

        View vTouchCloseTip = inflatedView.findViewById(R.id.v_touch_close_tip); // ??????????????????
        dialog.setContentView(inflatedView); // ??????????????????????????????
        Window dialogWindow = dialog.getWindow(); // ??????????????????????????????
        dialogWindow.setGravity(Gravity.BOTTOM); // ?????????????????????????????????????????????????????????
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // ????????????????????????
        lp.y = 0; // ??????????????????????????????
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // ????????????????????????
        // ??????????????????
        final float[] startY = new float[1];
        final float[] moveY = {0};
        View decorView = dialogWindow.getDecorView();
        vTouchCloseTip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        startY[0] = ev.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moveY[0] = ev.getY() - startY[0];
                        if (moveY[0] > 0) { //????????????????????????????????????????????????????????????
                            decorView.scrollBy(0, -(int) moveY[0]);
                            startY[0] = ev.getY();
                        }
                        if (decorView.getScrollY() > 0) { //??????????????????
                            decorView.scrollTo(0, 0);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (decorView.getScrollY() < -decorView.getHeight()
                                / 5 && moveY[0] > 0) { // ?????????????????????????????????5????????? ?????????Dialog
                            dialog.dismiss();
                        }
                        decorView.scrollTo(0, 0);
                        break;
                }
                return true;

            }
        });


        dialogWindow.setAttributes(lp); // ???????????????????????????????????????
        dialog.show(); // ????????????
        // TODO: ??????????????????????????????
        clAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AboutMeActivity.class);
                startActivity(intent);
            }
        });
        dialog_ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingsInfoActivity.class);
                startActivity(intent);
            }
        });
        dialog_btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("??????")
                        .setMessage("????????????????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DbSettingsService service = new DbSettingsService(MainActivity.this);
                                service.setSettings("authentication_cookie_token","");
                                GlobalRunningConfiguration.authentication_cookie_token = "";
                                Intent intent = new Intent(MainActivity.this,AuthenticationLoginActivity.class);
                                startActivity(intent);
                                // ?????????????????????
                                Toast.makeText(MainActivity.this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("??????",null)
                        .setCancelable(false)
                        .show();
            }
        });
        // TODO: ????????????????????????
        Thread getUserInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //??????config???????????????????????????
                String configResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new ArrayMap<>(), GlobalRunningConfiguration.authentication_cookie_token, false, MainActivity.this);
                // ????????????????????????????????????JSON??????
                JSONTokener tokener = new JSONTokener(configResponseText);
                try {
                    JSONObject configResponseJsonObj = (JSONObject) tokener.nextValue();
                    // ??????????????????
                    JSONObject responseUserInfo = configResponseJsonObj.getJSONObject("data").getJSONObject("user");
                    String nickname = responseUserInfo.getString("nickname");
                    // ???????????????????????????????????????
                    Message message = new Message();
                    message.what = HANDLER_GOOD_NICKNAME_SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putString("nickName", nickname);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getUserInfoThread.start();
        Thread getGoodSentenceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // ????????????
                String responseJsonText = OKHTTPUtil.sendGet("https://v1.hitokoto.cn/", new HashMap<>());
                JSONTokener tokener = new JSONTokener(responseJsonText);
                try {
                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                    String hitokoto = responseJsonObj.getString("hitokoto");
                    Message message = new Message();
                    message.what = HANDLER_GOOD_SENTENCE_SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putString("text", hitokoto);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getGoodSentenceThread.start();
        Thread getStorage = new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_user_storage), new HashMap<>(), GlobalRunningConfiguration.authentication_cookie_token);
                JSONTokener tokener = new JSONTokener(responseText);
                try {
                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                    JSONObject data = responseJsonObj.getJSONObject("data");
                    long usedStorage = data.getLong("used");
                    long totalStorage = data.getLong("total");
                    Bundle bundle = new Bundle();
                    bundle.putLong("usedStorage", usedStorage);
                    bundle.putLong("totalStorage", totalStorage);
                    Message message = new Message();
                    message.what = HANDLER_GET_STORAGE_SUCCESS;
                    message.setData(bundle);
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    handler.sendEmptyMessage(HANDLER_GET_STORAGE_FAILED);
                    e.printStackTrace();
                }


            }
        });
        getStorage.start();
    }


    /**
     * ????????????????????????
     */
    private void refreshFileTree() {
        srRefreshFileTree.setRefreshing(true); // ?????????????????????
        // ???????????????????????????????????????????????????
        // ???????????????
        if (!filterType.equals("all")) {
            currentFileTreePath.clear();
            currentFileTree.clear();
        }
        String dirPathString = getDirStringByList();
        // ???Base64??????
        dirPathString = dirPathString.replace("%2F", "/");
        tvDirPath.setText(dirPathString);

        Thread getFileTreeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // ????????????????????????????????????????????????Token
                DbSettingsService service = new DbSettingsService(MainActivity.this);
                String authentication_cookie_token = service.getSettings("authentication_cookie_token");
                String responseFileTreeText = "";
                // ????????????????????????????????????????????????
                switch (filterType) {
                    case "all": {
                        responseFileTreeText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_file_tree) + getDirStringByList(), new HashMap<>(), authentication_cookie_token);
                        break;
                    }
                    case "video": {
                        responseFileTreeText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_file_tree_filter_video), new HashMap<>(), authentication_cookie_token);
                        break;
                    }
                    case "image": {
                        responseFileTreeText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_file_tree_filter_image), new HashMap<>(), authentication_cookie_token);
                        break;
                    }
                    case "audio": {
                        responseFileTreeText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_file_tree_filter_audio), new HashMap<>(), authentication_cookie_token);
                        break;
                    }
                    case "doc": {
                        responseFileTreeText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_file_tree_filter_doc), new HashMap<>(), authentication_cookie_token);
                        break;
                    }
                }

                // ???????????????????????????????????????????????????
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("responseFileTreeText", responseFileTreeText);
                message.setData(bundle);
                message.what = HANDLER_FILE_TREE_GET_SUCCESS;
                handler.sendMessage(message);
            }
        });
        getFileTreeThread.start(); // ???????????????
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ?????????????????????????????????
        switch (requestCode) {
            case 0x10001: {
                if (resultCode == 0x10001) {
                    // ???????????????????????????????????????????????????
                    refreshFileTree();
                }
                break;
            }
        }
    }

    /**
     * ??????
     * @return ??????URL???????????????
     */
    private String getDirStringByList() {
        StringBuilder convertedPathString = new StringBuilder("%2F");
        for (String dirName : currentFileTreePath) {
            convertedPathString.append(dirName).append("%2F");
        }
        if (currentFileTreePath.size() != 0)
            convertedPathString = new StringBuilder(convertedPathString.substring(0, convertedPathString.length() - 3));
        return convertedPathString.toString();
    }

    /**
     * ?????????
     * @return ????????????????????????
     */
    private String getDirStringByListNoneCoding() {
        StringBuilder convertedPathString = new StringBuilder("/");
        for (String dirName : currentFileTreePath) {
            convertedPathString.append(dirName).append("/");
        }
        if (currentFileTreePath.size() != 0)
            convertedPathString = new StringBuilder(convertedPathString.substring(0, convertedPathString.length() - 1));
        return convertedPathString.toString();
    }
}