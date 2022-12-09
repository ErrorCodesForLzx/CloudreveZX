package com.ecsoft.cloudreve.ui.fileType;

import com.ecsoft.cloudreve.R;

/**
 * 根据文件名称后缀判断文件的显示图片
 */
public class FileTypeJudgeUtil {

    /**
     *
     * 提取文件的后缀名称
     * @param fileName 文件名称可以为全文件路径
     * @return
     */
    public static String getFileSuffix(String fileName){
        if(fileName.lastIndexOf(".")==-1){
            return "";//文件没有后缀名的情况
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    public static int getImageResourceByFileName(String fileName){
        String fileSuffix = getFileSuffix(fileName);
        fileSuffix = fileSuffix.toLowerCase(); // 全部到小写
        switch (fileSuffix){
            case ".gif":{
                return R.drawable.ic_net_gif;
            }
            case ".jpg":{
                return R.drawable.ic_net_jpg;
            }
            case ".mov":{
                return R.drawable.ic_net_mov;
            }
            case ".mp4":{
                return R.drawable.ic_net_mp4;
            }
            case ".pdf":{
                return R.drawable.ic_net_pdf;
            }
            case ".png":{
                return R.drawable.ic_net_png;
            }
            case ".ppt":
            case ".pptx": {
                return R.drawable.ic_net_ppt;
            }
            case ".psd":{
                return R.drawable.ic_net_psd;
            }
            case ".rar":{
                return R.drawable.ic_net_rar;
            }
            case ".svg":{
                return R.drawable.ic_net_svg;
            }
            case ".txt":{
                return R.drawable.ic_net_txt;
            }
            case ".avi":
            case ".flv":
            case ".mpeg":
            case ".wmv":
            case ".asf": {
                return R.drawable.ic_net_video;
            }
            case ".doc":
            case ".docx": {
                return R.drawable.ic_net_word;
            }
            case ".xlsx": {
                return R.drawable.ic_net_xlsx;
            }
            case ".zip": {
                return R.drawable.ic_net_zip;
            }
            default:{
                // 都没有匹配
                return R.drawable.ic_net_unkonow;
            }

        }
    }
}
