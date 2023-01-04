package com.ecsoft.cloudreve.storage;

public class FileNameUtil {
    /**
     * 获取合理文件名称，避免文件名过长
     * @param fileName
     * @return
     */
    public static String getReasonableFileName(String fileName){
        if (fileName.length() >=16){
            // 如果文件名称大于16的话
            String fileSuffix = getFileSuffix(fileName);
            fileName = fileName.replace(fileSuffix,"");
            String substring = fileName.substring(0, 5);
            return substring+"..."+fileSuffix;
        }
        return fileName;
    }

    /**
     *
     * 提取文件的后缀名称
     * @param fileName 文件名称可以为全文件路径
     * @return
     */
    private static String getFileSuffix(String fileName){
        if(fileName.lastIndexOf(".")==-1){
            return "";//文件没有后缀名的情况
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}
