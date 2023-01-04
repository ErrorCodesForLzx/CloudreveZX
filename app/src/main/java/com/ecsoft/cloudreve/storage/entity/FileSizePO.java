package com.ecsoft.cloudreve.storage.entity;

public class FileSizePO {
    private long size;
    private String unit;

    public FileSizePO(long size, String unit) {
        this.size = size;
        this.unit = unit;
    }

    public FileSizePO() {
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
