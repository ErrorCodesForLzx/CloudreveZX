package com.ecsoft.cloudreve.download.entity;

import java.io.Serializable;

public class DownloadTaskPO implements Serializable {
    private String fileId;
    private long totalSize = 0L;
    private long downloadedSize = 0L;
    // 为了计算速度添加一个上一次下载
    private long lastDownloadedSize = 0L;
    private String downloadUrl;
    private String fileName;
    /**
     * 0:停止下载 1:正在下载
     */
    public int nowStatus = 0;
    public String downloadMessage = "未开启";



    public DownloadTaskPO(String fileId, long totalSize, long downloadedSize) {
        this.fileId = fileId;
        this.totalSize = totalSize;
        this.downloadedSize = downloadedSize;

    }

    public DownloadTaskPO() {
    }

    public long getLastDownloadedSize() {
        return lastDownloadedSize;
    }

    public void setLastDownloadedSize(long lastDownloadedSize) {
        this.lastDownloadedSize = lastDownloadedSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
}
