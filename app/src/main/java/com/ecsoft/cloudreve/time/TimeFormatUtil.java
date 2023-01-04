package com.ecsoft.cloudreve.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间加工格式类
 */
public class TimeFormatUtil {
    /**
     * 将字符串加工为时间对象
     * @param time 字符串时间
     * @return 返回Date对象
     */
    public static Date formatTimeByString(String time) throws ParseException {
        // Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date parse = dateFormat.parse(time);
        return parse;
    }
    /**
     * 将字符串加工为时间对象
     * @param time 字符串时间
     * @return 返回Date对象
     */
    public static Date formatTimeByStringHasMS(String time) throws ParseException {
        // Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ");
        Date parse = dateFormat.parse(time);
        return parse;
    }

    /**
     * Date对象到字符串
     * @param time Date对象
     * @param format 格式化限定字段
     * @return 返回格式化字符串
     */
    public static String dateToString(Date time,String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(time);
    }
}
