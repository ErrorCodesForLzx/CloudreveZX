package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.ecsoft.cloudreve.ui.recyclerView.adapter.DownloadTaskAdapter;

public class DownloadTaskManageActivity extends AppCompatActivity {

    private ImageView    ivBack;
    private RecyclerView rvDownloadTask;

    private Thread getDownloadThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true){

                handler.sendEmptyMessage(HANDLER_GET_UNFINISHED_TASK);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private static final int HANDLER_GET_UNFINISHED_TASK = 0x10001;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_GET_UNFINISHED_TASK:{
                    DownloadTaskAdapter adapter = new DownloadTaskAdapter(DownloadTaskManageActivity.this);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DownloadTaskManageActivity.this);
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                    rvDownloadTask.setLayoutManager(linearLayoutManager);
                    rvDownloadTask.setAdapter(adapter);
                    break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_task_manage);

        if (getSupportActionBar() !=null){
            getSupportActionBar().hide();
        }

        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewEvent() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
    }

    private void initViewData() {
        // 载入视图
        refreshDownloadTask();

    }

    private void initViewComponent() {
        ivBack = findViewById(R.id.iv_back);
        rvDownloadTask = findViewById(R.id.rv_download_task);
    }
    public void refreshDownloadTask(){
        getDownloadThread.start();
    }
}