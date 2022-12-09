package com.ecsoft.cloudreve.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudDownloadService extends Service {

    // 文件任务列表
    private List<BaseDownloadTask> downloadTasks = new ArrayList<>();
    private OnDownloadProgressChangedListener onDownloadProgressChangedListener;
    private FileDownloadLargeFileListener downloadListener = new FileDownloadLargeFileListener() {
        @Override
        protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

        }

        @Override
        protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
            if (onDownloadProgressChangedListener != null) {
                onDownloadProgressChangedListener.normalProgress(task.getTag().toString(), soFarBytes, totalBytes);
            }
        }

        @Override
        protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
            if (onDownloadProgressChangedListener != null){
                onDownloadProgressChangedListener.paused(task.getTag().toString(),soFarBytes,totalBytes);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            if (onDownloadProgressChangedListener != null) {
                onDownloadProgressChangedListener.completed(task.getTag().toString());
            }

        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            if (onDownloadProgressChangedListener != null) {
                onDownloadProgressChangedListener.error(task.getTag().toString(), e);
            }
        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }
    };
    private FileDownloadQueueSet downloadQueueSet  = new FileDownloadQueueSet(downloadListener);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化下载支持库
        FileDownloader.setupOnApplicationOnCreate(getApplication());

    }

    public interface OnDownloadProgressChangedListener{
        void paused(String fileId,long downloadedByte,long totalByte);
        void error(String fileId,Throwable e);
        void normalProgress(String fileId,long downloadedByte,long totalByte);
        void completed(String fileId);
    }

    public class DownloadIBinder extends Binder {
        /**
         * 创建下载任务
         * @param downloadUrls 下载链接集合
         * @param savedPath 保存文件
         */
        public void createDownloadTask(List<String> downloadUrls,String savedPath,String fileId){

            // 创建下载任务
            BaseDownloadTask baseDownloadTask;
            for (int i = 0; i<= downloadUrls.size()-1;i++){
                baseDownloadTask = FileDownloader.getImpl()
                        .create(downloadUrls.get(i))
                        .setPath(savedPath,true)
                        .setTag(fileId);
                downloadTasks.add(baseDownloadTask);
            }

            downloadQueueSet.setCallbackProgressTimes(100);
            downloadQueueSet.setCallbackProgressMinInterval(100);
            downloadQueueSet.setAutoRetryTimes(3);
            FileDownloader.enableAvoidDropFrame(); // 开启避免掉帧设置
            // 启动序列
            downloadQueueSet.start();

        }

        public void setOnDownloadProgressChangedListener(OnDownloadProgressChangedListener listener){
            onDownloadProgressChangedListener = listener; // 绑定监听器接口实现对象
        }
    }
}
