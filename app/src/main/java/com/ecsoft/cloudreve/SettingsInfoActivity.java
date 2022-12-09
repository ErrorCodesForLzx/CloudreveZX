package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;
import com.ecsoft.cloudreve.system.ClipboardUtil;
import com.ecsoft.cloudreve.time.TimeFormatUtil;
import com.ecsoft.cloudreve.ui.recyclerView.adapter.SystemSettingsAdapter;
import com.ecsoft.cloudreve.ui.recyclerView.entity.SystemSettingPO;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SettingsInfoActivity extends AppCompatActivity {

    private List<SystemSettingPO> settings;
    private String currentUserConfigUID;
    private String currentUserConfigNickName;
    private String currentUserConfigGroup;
    private String currentUserConfigCreateTime;

    private static final int HANDLER_GET_USER_CONFIG_SUCCESS = 0x10001;

    private RecyclerView rvSettingsView;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case HANDLER_GET_USER_CONFIG_SUCCESS:{
                    // 创建适配器对象
                    SystemSettingsAdapter systemSettingsAdapter = new SystemSettingsAdapter(SettingsInfoActivity.this,settings);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SettingsInfoActivity.this);
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                    // 设置项目单击监听器
                    systemSettingsAdapter.setOnSystemItemClickedListener(new SystemSettingsAdapter.OnSystemItemClickedListener() {
                        @Override
                        public void onItemClickedListener(SystemSettingPO clickedEntity, int position) {
                            if (!clickedEntity.isCanGoto()){
                                // 不能跳转，单击则复制文本
                                ClipboardUtil.copyToSystemClipboard(SettingsInfoActivity.this,clickedEntity.getRightLabel());
                                Toast.makeText(SettingsInfoActivity.this, "已复制 "+clickedEntity.getSettingTitle(), Toast.LENGTH_SHORT).show();
                            } else {
                                // 能跳转，创建意图对象
                                Intent intent = new Intent(SettingsInfoActivity.this,clickedEntity.getGotoPage());
                                startActivity(intent);
                            }
                        }
                    });
                    rvSettingsView.setLayoutManager(linearLayoutManager);
                    rvSettingsView.setAdapter(systemSettingsAdapter);
                    break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_info);

        // 加载视图
        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewData() {
        // 获取数据
        Thread getUserConfig = new Thread(new Runnable() {
            @Override
            public void run() {
                String responseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_user_config), new HashMap<>(), GlobalRunningConfiguration.authentication_cookie_token);
                JSONTokener tokener = new JSONTokener(responseText);
                try {
                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                    JSONObject userConfig = responseJsonObj.getJSONObject("data").getJSONObject("user");
                    currentUserConfigUID        = userConfig.getString("id");
                    currentUserConfigNickName   = userConfig.getString("nickname");
                    currentUserConfigGroup      = userConfig.getJSONObject("group").getString("name");
                    Date created_at = TimeFormatUtil.formatTimeByStringHasMS(userConfig.getString("created_at"));
                    currentUserConfigCreateTime = TimeFormatUtil.dateToString(created_at,"yyyy年MM月dd日 HH:mm:ss");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    // 2022-12-02T14:31:17.772566842+08:00
                    // 2022-12-01T18:24:01.26+08:00
                    e.printStackTrace();
                }
                // UID展示
                SystemSettingPO uidSettings        = new SystemSettingPO(R.drawable.ic_icon_settings_uid,"UID",false,null,currentUserConfigUID);
                SystemSettingPO nickNameSettings   = new SystemSettingPO(R.drawable.ic_icon_settings_nickname,"昵称",false,null,currentUserConfigNickName);
                SystemSettingPO groupSettings      = new SystemSettingPO(R.drawable.ic_icon_settings_group,"用户组",false,null,currentUserConfigGroup);
                SystemSettingPO createTimeSettings = new SystemSettingPO(R.drawable.ic_icon_settings_create_time,"创建时间",false,null,currentUserConfigCreateTime);
                SystemSettingPO networkSettings    = new SystemSettingPO(R.drawable.ic_icon_settings_network,"网络设置",true,NetworkSettingActivity.class,"");
                settings = new ArrayList<>();
                settings.add(uidSettings);
                settings.add(nickNameSettings);
                settings.add(groupSettings);
                settings.add(createTimeSettings);
                settings.add(networkSettings);
                // 发送成功消息到消息队列
                handler.sendEmptyMessage(HANDLER_GET_USER_CONFIG_SUCCESS);
            }
        });
        getUserConfig.start();
    }

    private void initViewEvent() {

    }

    private void initViewComponent() {
        rvSettingsView = findViewById(R.id.rv_settings_view);
    }
}