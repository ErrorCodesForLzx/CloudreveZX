package com.ecsoft.cloudreve.time;

import java.util.Date;

/**
 * 问候词工具类
 */
public class GreetWordUtil {
    public static String getGreetWord(){
        Date now = new Date();
        int hours = now.getHours();
        if (hours >= 9 && hours <= 11){
            return "上午好";
        } else if (hours >= 12 && hours <= 13){
            return "中午好";
        } else if (hours >= 13 && hours <= 19){
            return "下午好";
        } else {
            return "晚上好";
        }

    }
}
