package com.ecsoft.cloudreve.ui.recyclerView.entity;

import com.ecsoft.cloudreve.R;

public class SystemSettingPO {
    private int iconDrawable    = R.drawable.ic_icon_settings; // 设置项目图标
    private String settingTitle = "系统设置"; // 设置项目名称
    private boolean canGoto     = true; // 是否为能跳转界面的设置
    private Class  gotoPage     ; // 设置跳转界面
    private String rightLabel   = "V1.0.0"; // 设置右边的提示语，空表示不设置

    public SystemSettingPO(int iconDrawable, String settingTitle, boolean canGoto, Class gotoPage, String rightLabel) {
        this.iconDrawable = iconDrawable;
        this.settingTitle = settingTitle;
        this.canGoto = canGoto;
        this.gotoPage = gotoPage;
        this.rightLabel = rightLabel;
    }

    public SystemSettingPO() {
    }

    public int getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(int iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public String getSettingTitle() {
        return settingTitle;
    }

    public void setSettingTitle(String settingTitle) {
        this.settingTitle = settingTitle;
    }

    public boolean isCanGoto() {
        return canGoto;
    }

    public void setCanGoto(boolean canGoto) {
        this.canGoto = canGoto;
    }

    public Class getGotoPage() {
        return gotoPage;
    }

    public void setGotoPage(Class gotoPage) {
        this.gotoPage = gotoPage;
    }

    public String getRightLabel() {
        return rightLabel;
    }

    public void setRightLabel(String rightLabel) {
        this.rightLabel = rightLabel;
    }
}
