package com.ecsoft.cloudreve.storage;

import com.ecsoft.cloudreve.storage.entity.FileSizePO;

/**
 * 文件储蓄工具包
 */
public class FileStorageUtil {

    // 定义字节进制
    private static String[] storageUtil = {"B","KB","MB","GB","PB"};
    /**
     * 获取合理的文件大小，以一万为单位交换阀值
     * @param byteSize 以字节为单位的文件
     * @param startStorageUnit 从什么存储单位开始
     * @return 返回文件大小实体类
     */
    public static FileSizePO getReasonableFileSize(long byteSize, int startStorageUnit){
        long formattedFileSize = byteSize; // 赋值操作
        // 递归循环结束判定
        if (formattedFileSize >= 10000L){ // 文件是否大于一万
            // 如果大于执行进位，并递归(套娃)操作
            formattedFileSize = formattedFileSize / 1024L; // 进位
            return getReasonableFileSize(formattedFileSize,startStorageUnit + 1); // 触发递归循环
        } else {
            // 文件符合一万大小标准，返回，并且返回上一个内存栈帧(POP StackFrame)
            return new FileSizePO(byteSize,storageUtil[startStorageUnit]);
        }
    }

}
