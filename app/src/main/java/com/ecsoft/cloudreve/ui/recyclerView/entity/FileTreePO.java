package com.ecsoft.cloudreve.ui.recyclerView.entity;

import java.io.Serializable;

public class FileTreePO implements Serializable {
    private String  id;
    private String  name;
    private String  path;
    private String  pic;
    private Integer size;
    private String  type;
    private String  date;
    private String  createDate;
    private Boolean sourceEnabled;


    // GET和SET


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Boolean getSourceEnabled() {
        return sourceEnabled;
    }

    public void setSourceEnabled(Boolean sourceEnabled) {
        this.sourceEnabled = sourceEnabled;
    }

    public FileTreePO() {
    }

    public FileTreePO(String id, String name, String path, String pic, Integer size, String type,
                      String date, String createDate, Boolean sourceEnabled) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.pic = pic;
        this.size = size;
        this.type = type;
        this.date = date;
        this.createDate = createDate;
        this.sourceEnabled = sourceEnabled;
    }
}
