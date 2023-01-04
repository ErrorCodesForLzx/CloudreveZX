package com.ecsoft.cloudreve.config;

import android.content.Context;

import com.ecsoft.cloudreve.database.DbSettingsService;

import java.lang.reflect.Field;

/**
 * 启动是加载的全局设置变量
 */
public class GlobalRunningConfiguration {


    public static String network_address = "http://192.168.0.105";
    public static String network_port    = "5212";
    public static String authentication_email        = "";
    public static String authentication_cookie_token = "";


    // 初始化类
    public static void init(Context c){
        // 把数据库存储的加载到Static RAM
        DbSettingsService  service  = new DbSettingsService(c);
        network_address             = service.getSettings("network_address");
        network_port                = service.getSettings("network_port");
        authentication_email        = service.getSettings("authentication_email");
        authentication_cookie_token = service.getSettings("authentication_cookie_token");
    }


}
