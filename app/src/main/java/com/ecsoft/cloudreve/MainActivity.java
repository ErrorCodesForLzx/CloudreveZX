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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
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
     * 当前文件路径，集合里面的每一个元素都代表一节目录
     */
    private ArrayList<String> currentFileTreePath;
    private ArrayList<FileTreePO> currentFileTree;
    // 筛选类型 all video image audio doc
    private String filterType = "all";
    private static final int HANDLER_FILE_TREE_GET_SUCCESS = 0x10001;
    private static final int HANDLER_GOOD_SENTENCE_SUCCESS = 0x20001;
    private static final int HANDLER_GOOD_NICKNAME_SUCCESS = 0x30001;
    private static final int HANDLER_GET_STORAGE_SUCCESS = 0x40001;
    private static final int HANDLER_GET_STORAGE_FAILED = 0x40002;
    private static final int HANDLER_CREATE_DIR_SUCCESS = 0x50001;
    private static final int HANDLER_CREATE_DIR_FAILED  = 0x50002;


    private ImageView ivUserInfo; // 用户信息
    private SwipeRefreshLayout srRefreshFileTree;
    private RecyclerView rvFileTree;
    private TextView tvDirPath;
    private ImageView ivBackPath;
    private ImageView ivGlobalSettings;
    private TextView dialog_tvGoodSentence;
    private TextView dialog_tvTitle;
    private TextView dialog_tvUserStorage;
    private ImageView dialog_ivSettings;
    private Button dialog_btnLogout;
    private ProgressBar dialog_pbStorage;
    private ConstraintLayout clAboutMe;
    // 选项卡定义
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
                    // 获取传递到的响应文本
                    Bundle data = message.getData();
                    String responseFileTreeText = data.getString("responseFileTreeText");
                    // 将获取到的Json文本转为JsonObj
                    JSONTokener tokener = new JSONTokener(responseFileTreeText);
                    try {
                        JSONObject responseFileTreeJsonObj = (JSONObject) tokener.nextValue();
                        JSONArray fileTreeJsonArray = responseFileTreeJsonObj.getJSONObject("data").getJSONArray("objects");
                        currentFileTree.clear(); // 清空集合的所有元素
                        // 遍历获取到的数组
                        for (int i = 0; i <= fileTreeJsonArray.length() - 1; i++) {
                            // 获取数组中没一个元素的JsonObj
                            JSONObject itemJsonObj = fileTreeJsonArray.getJSONObject(i);
                            FileTreePO itemEntityObj = new FileTreePO();
                            // 获取JsonObj的所有对象到实体类中
                            itemEntityObj.setId(itemJsonObj.getString("id"));
                            itemEntityObj.setName(itemJsonObj.getString("name"));
                            itemEntityObj.setPath(itemJsonObj.getString("path"));
                            itemEntityObj.setPic(itemJsonObj.getString("pic"));
                            itemEntityObj.setSize(itemJsonObj.getInt("size"));
                            itemEntityObj.setType(itemJsonObj.getString("type"));
                            itemEntityObj.setDate(itemJsonObj.getString("date"));
                            itemEntityObj.setCreateDate(itemJsonObj.getString("create_date"));
                            itemEntityObj.setSourceEnabled(itemJsonObj.getBoolean("source_enabled"));
                            // 将实体类加入到集合中
                            currentFileTree.add(itemEntityObj);
                        }
                        // 创建Adapter
                        FileTreeAdapter fileTreeAdapter = new FileTreeAdapter(currentFileTree, MainActivity.this);
                        // 获取线性视图器
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); // 将获取到的线性设置为垂直线性
                        rvFileTree.setLayoutManager(layoutManager);
                        // 给适配器绑定项目单击事件
                        fileTreeAdapter.setOnFileClickListener(new FileTreeAdapter.OnFileClickListener() {
                            @Override
                            public void onClick(Integer position, FileTreePO nowFileFree) {
                                // 判断当前单击的是否为文件夹
                                if (nowFileFree.getType().equals("dir")) {
                                    // 是文件夹
                                    // 将目录路径新添加一个文件夹名称
                                    currentFileTreePath.add(nowFileFree.getName());
                                    refreshFileTree(); // 重新刷新

                                } else {
                                    //  否则就是单击的文件
                                    Bundle data = new Bundle();
                                    data.putSerializable("entity", nowFileFree);
                                    Intent intent = new Intent(MainActivity.this, FileInfoActivity.class);
                                    intent.putExtra("entity", data);
                                    startActivityForResult(intent, 0x10001); // 启动文件详情页面
                                }
                            }
                        });
                        rvFileTree.setAdapter(fileTreeAdapter); // 设置Adapter
                        srRefreshFileTree.setRefreshing(false); // 设置刷新成功
                        // 加载动画
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
                    dialog_tvTitle.setText("Hi！" + data.getString("nickName") + "，" + GreetWordUtil.getGreetWord() + "！");
                    break;
                }
                case HANDLER_GET_STORAGE_SUCCESS: {
                    Bundle data = message.getData();
                    long usedStorage = data.getLong("usedStorage");
                    long totalStorage = data.getLong("totalStorage");
                    FileSizePO reasonableFileSizeUsed = FileStorageUtil.getReasonableFileSize(usedStorage, 0);
                    FileSizePO reasonableFileSizeTotal = FileStorageUtil.getReasonableFileSize(totalStorage, 0);
                    dialog_tvUserStorage.setText("您的空间：" + reasonableFileSizeUsed.getSize() + reasonableFileSizeUsed.getUnit() + "/"
                            + reasonableFileSizeTotal.getSize() + reasonableFileSizeTotal.getUnit());
                    int usedPercent = 0; // 已经用过的空间百分比
                    if (totalStorage != 0L) {
                        // 计算公式：  （ 用过的 / 总共的 ）* 100
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
                    Toast.makeText(MainActivity.this, "目录创建成功", Toast.LENGTH_SHORT).show();
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

    }

    private long mExitTime;

    //对返回键进行监听

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        // 判断是否有目录可以返回
        if (currentFileTreePath.size() > 0) {
            currentFileTreePath.remove(currentFileTreePath.size() - 1); // 移除集合中最后一个元素
            // 重新刷新显示
            refreshFileTree();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再返回一次，退出 Cloudreve", Toast.LENGTH_SHORT).show();
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
        ivGlobalSettings = findViewById(R.id.iv_global_settings);
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

    }

    private void initViewData() {
        // 初始化目录路径集合
        currentFileTreePath = new ArrayList<>();
        currentFileTree = new ArrayList<>();
        refreshFileTree();
    }

    private void initViewEvent() {
        srRefreshFileTree.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setFiler(filterType); // 执行刷新操作
            }
        });
        // 设置目录返回按钮单击
        ivBackPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 判断当前是否为根目录，即目录集合为空
                if (currentFileTreePath.size() != 0) {
                    // 代表不是根目录
                    currentFileTreePath.remove(currentFileTreePath.size() - 1); // 移除集合中最后一个元素
                    // 重新刷新显示
                    refreshFileTree();
                }
            }
        });
        ivUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserInfo(); // 显示窗口
            }
        });
        ivGlobalSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsInfoActivity.class);
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
                tvCreateAt.setText("创建在："+getDirStringByListNoneCoding());

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle("创建目录").setIcon(R.drawable.ic_dialog_icon_action_create_dir).setView(dialogView)
                        .setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etDirName = dialogView.findViewById(R.id.et_auth_email);
                        String dirName = String.valueOf(etDirName.getText());
                        if (!dirName.isEmpty()){
                            String cookies = GlobalRunningConfiguration.authentication_cookie_token;
                            Thread createDirThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 将目录封装为Json对象
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
                                            // 发生错误
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
                            Toast.makeText(MainActivity.this, "非法目录，无法创建", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

                builder.show();
            }
        });
        fabMenuUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "对不起，作者正在极力兼容库中...", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setFiler(String type) {
        filterType = type; // 赋值
        refreshFileTree(); // 刷新
        switch (type) {
            case "all": {
                tvDirPath.setText(getDirStringByListNoneCoding());
                // UI颜色
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_all_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_all_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_all);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_all);
                break;
            }
            case "video": {
                tvDirPath.setText("已应用筛选器，视频");
                // UI颜色
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_video_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_video_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_video);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_video);
                break;
            }
            case "image": {
                tvDirPath.setText("已应用筛选器，图片");
                // UI颜色
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_image_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_image_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_image);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_image);
                break;
            }
            case "audio": {
                tvDirPath.setText("已应用筛选器，音频");
                // UI颜色
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_audio_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_audio_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_audio);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_audio);
                break;
            }
            case "doc": {
                tvDirPath.setText("已应用筛选器，文档");
                // UI颜色
                llAppTitle.setBackgroundResource(R.color.theme_file_filter_doc_title);
                tlFilterChoose.setBackgroundResource(R.color.theme_file_filter_doc_tab);
                llPathAction.setBackgroundResource(R.drawable.shape_bk_main_card_doc);
                llPath.setBackgroundResource(R.drawable.shape_bk_show_dir_doc);
                break;
            }
        }

    }

    /**
     * 弹出用户信息对话框
     */
    private void showUserInfo() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.UserInfoDialog);
        View inflatedView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_user_info, null);
        //  TODO: 这里执行获取组件的操作
        dialog_tvGoodSentence = inflatedView.findViewById(R.id.tv_good_sentence);
        dialog_tvTitle = inflatedView.findViewById(R.id.dialog_tv_title);
        dialog_tvUserStorage = inflatedView.findViewById(R.id.tv_user_storage);
        dialog_pbStorage = inflatedView.findViewById(R.id.pb_storage);
        clAboutMe = inflatedView.findViewById(R.id.cl_about_me);
        dialog_ivSettings = inflatedView.findViewById(R.id.dialog_iv_settings);
        dialog_btnLogout = inflatedView.findViewById(R.id.dialog_btn_logout);

        View vTouchCloseTip = inflatedView.findViewById(R.id.v_touch_close_tip); // 对话框关闭条
        dialog.setContentView(inflatedView); // 将对话框样式添加视图
        Window dialogWindow = dialog.getWindow(); // 获取对话框的窗口对象
        dialogWindow.setGravity(Gravity.BOTTOM); // 设置对话框窗口的对其方式，为靠底部对其
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取窗口属性对象
        lp.y = 0; // 设置窗口在底部的距离
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 设置和父容器相同
        // 设置滑动关闭
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
                        if (moveY[0] > 0) { //避免坐标抖动，滑动过程中闪烁不连贯的问题
                            decorView.scrollBy(0, -(int) moveY[0]);
                            startY[0] = ev.getY();
                        }
                        if (decorView.getScrollY() > 0) { //避免向上拖动
                            decorView.scrollTo(0, 0);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (decorView.getScrollY() < -decorView.getHeight()
                                / 5 && moveY[0] > 0) { // 如果滑动距离达到高度的5分之一 就关闭Dialog
                            dialog.dismiss();
                        }
                        decorView.scrollTo(0, 0);
                        break;
                }
                return true;

            }
        });


        dialogWindow.setAttributes(lp); // 将新设置的属性应用到窗口上
        dialog.show(); // 显示窗口
        // TODO: 这里执行绑定事件操作
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
                        .setTitle("提示")
                        .setMessage("您确定要安全退出登录吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DbSettingsService service = new DbSettingsService(MainActivity.this);
                                service.setSettings("authentication_cookie_token","");
                                GlobalRunningConfiguration.authentication_cookie_token = "";
                                Intent intent = new Intent(MainActivity.this,AuthenticationLoginActivity.class);
                                startActivity(intent);
                                // 跳转到登录界面
                                Toast.makeText(MainActivity.this, "用户已经安全退出登录！", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .setCancelable(false)
                        .show();
            }
        });
        // TODO: 这里执行数据获取
        Thread getUserInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //发送config请求判断当前用户组
                String configResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new ArrayMap<>(), GlobalRunningConfiguration.authentication_cookie_token, false, MainActivity.this);
                // 将获取到的响应文本转换为JSON对象
                JSONTokener tokener = new JSONTokener(configResponseText);
                try {
                    JSONObject configResponseJsonObj = (JSONObject) tokener.nextValue();
                    // 获取用户信息
                    JSONObject responseUserInfo = configResponseJsonObj.getJSONObject("data").getJSONObject("user");
                    String nickname = responseUserInfo.getString("nickname");
                    // 发送获取到的昵称到消息队列
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
                // 获取美句
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
     * 刷新当前的文件树
     */
    private void refreshFileTree() {
        srRefreshFileTree.setRefreshing(true); // 设置为正在刷新
        // 并且将标题栏的文件路径提示标题修改
        // 判断筛选器
        if (!filterType.equals("all")) {
            currentFileTreePath.clear();
            currentFileTree.clear();
        }
        String dirPathString = getDirStringByList();
        // 将Base64转码
        dirPathString = dirPathString.replace("%2F", "/");
        tvDirPath.setText(dirPathString);

        Thread getFileTreeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 通过获取数据库对象来获取登录令牌Token
                DbSettingsService service = new DbSettingsService(MainActivity.this);
                String authentication_cookie_token = service.getSettings("authentication_cookie_token");
                String responseFileTreeText = "";
                // 向服务器发送一个获取文件树的请求
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

                // 将获取到的响应文本传递到消息队列中
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("responseFileTreeText", responseFileTreeText);
                message.setData(bundle);
                message.what = HANDLER_FILE_TREE_GET_SUCCESS;
                handler.sendMessage(message);
            }
        });
        getFileTreeThread.start(); // 开启本线程
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 当受托视图正常返回响应
        switch (requestCode) {
            case 0x10001: {
                if (resultCode == 0x10001) {
                    // 表示文件被操作了，需要重新刷新列表
                    refreshFileTree();
                }
                break;
            }
        }
    }

    /**
     * 编码
     * @return 返回URL编码字符串
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
     * 非编码
     * @return 返回非编码字符串
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