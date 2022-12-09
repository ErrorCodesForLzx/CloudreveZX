package com.ecsoft.cloudreve.download.entity;

public class DownloadTaskPO {
    public static final int STATUS_DOWNLOADING = 0;
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_ERROR = -1;

    private String fileId;
    private long downloadedByte;
    private long totalByte;
    private int nowStatus;

    public DownloadTaskPO(String fileId, long downloadedByte, long totalByte, int nowStatus) {
        this.fileId = fileId;
        this.downloadedByte = downloadedByte;
        this.totalByte = totalByte;
        this.nowStatus = nowStatus;
    }

    public DownloadTaskPO() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getDownloadedByte() {
        return downloadedByte;
    }

    public void setDownloadedByte(long downloadedByte) {
        this.downloadedByte = downloadedByte;
    }

    public long getTotalByte() {
        return totalByte;
    }

    public void setTotalByte(long totalByte) {
        this.totalByte = totalByte;
    }

    public int getNowStatus() {
        return nowStatus;
    }

    public void setNowStatus(int nowStatus) {
        this.nowStatus = nowStatus;
    }
}
