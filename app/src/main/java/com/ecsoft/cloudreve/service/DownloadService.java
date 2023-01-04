package com.ecsoft.cloudreve.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.ecsoft.cloudreve.download.entity.DownloadTaskPO;
import com.ecsoft.cloudreve.download.entity.DownloadTasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {

    // 创建一个消息对象
    private Messenger messenger = new Messenger(new downloadHandler());

    public static final int HANDLER_START_DOWNLOAD_TASK = 0xF0001;

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("CLOUDREVE_ZX","服务启动成功！！！");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("CLOUDREVE_ZX","服务端被客户端连接onBing()Executed");
        return messenger.getBinder();
    }

    class downloadHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case HANDLER_START_DOWNLOAD_TASK:{
                    // 开启任务消息
                    Bundle data = msg.getData();
                    DownloadTaskPO currentEntity = (DownloadTaskPO) data.getSerializable("task");
                    DownloadTasks.addTask(currentEntity);
                    Log.e("CLOUDREVE_ZX","下载连接已经添加到服务....");
                    startDownloadTaskThread(currentEntity); // 开启任务
                    Log.e("CLOUDREVE_ZX","下载任务已经开始...");
                }
            }
            super.handleMessage(msg);
        }
    }

    private void startDownloadTaskThread(DownloadTaskPO task){
        // 开启线程
        Thread startDownlaodThread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(task.getDownloadUrl());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("CLOUDREVR_ZX","文件下载失败，状态码!=200...");

                        return;
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();


                    File downloadDir = new File("/sdcard/Download/CloudreveZX");
                    if (!downloadDir.exists()){
                        downloadDir.mkdir();
                    }

                    // 文件是否存在
                    File downloadFile = new File("/sdcard/Download/CloudreveZX/"+task.getFileName());
                    if (!downloadFile.exists()) {
                        boolean b = downloadFile.createNewFile();

                        Log.e("CLOUDREVE_ZX","文件创建："+b);
                    }


                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream("/sdcard/Download/CloudreveZX/"+task.getFileName());

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    // set file download status;
                    DownloadTasks.modifyStatus(task.getFileId(),"下载中");

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) { // only if total length is known
                            // 发送进度
                            // Log.e("CLOUDREVE_ZX", "文件"+task.getFileName()+"下载！！！！" );
                            DownloadTasks.modifyProgress(task.getFileId(), total);
                            // publishProgress((int) (total * 100 / fileLength)); // 原作者写的
                            output.write(data, 0, count);

                        }
                    }
                } catch (Exception e) {
                    Log.e("CLOUDREVE_ZX","下载途中发生异常....");
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();

                        // 任务完成
                        DownloadTasks.finishTask(task.getFileId());
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }

            }
        });
        startDownlaodThread.start();

    }


}
