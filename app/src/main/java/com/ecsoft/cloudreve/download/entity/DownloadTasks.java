package com.ecsoft.cloudreve.download.entity;

import java.util.ArrayList;
import java.util.List;

public class DownloadTasks {
    // 定义完成和未完成的下载任务
    public static List<DownloadTaskPO> unfinishedTaskList = new ArrayList<>();
    public static List<DownloadTaskPO> finishedTaskList = new ArrayList<>();

    /**
     * 在未完成的任务列表中查询
     * @param fileId 文件ID
     * @return 返回查询到的实体
     */
    public static DownloadTaskPO queryUnfinishedList(String fileId){
        for (int i=0;i<=unfinishedTaskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = unfinishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)) return downloadTaskPO;
        }
        // 没有搜寻到返回空对象
        return  null;
    }
    /**
     * 在完成的任务列表中查询
     * @param fileId 文件ID
     * @return 返回查询到的实体
     */
    public static DownloadTaskPO queryFinishedList(String fileId){
        for (int i=0;i<=finishedTaskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = finishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)) return downloadTaskPO;
        }
        // 没有搜寻到返回空对象
        return  null;
    }
    /**
     * 在传入的集合中判断是否有相同的元素
     * @param taskList 传入的集合
     * @param fileId 文件ID
     * @return 返回是否有相同的元素
     */
    public static boolean hasSameTask(List<DownloadTaskPO> taskList,String fileId){
        for (int i=0;i<=taskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = taskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)) return true;
        }
        // 没有搜寻到返回空对象
        return false;
    }
    /**
     * 添加任务
     * @param downloadTaskPO 任务实体对象
     */
    public static void addTask(DownloadTaskPO downloadTaskPO){
        unfinishedTaskList.add(downloadTaskPO);
    }

    /**
     *  完成任务
     * @param fileId 文件ID
     */
    public static void finishTask(String fileId){
        // 遍历循环所有集合
        for (int i=0;i<=unfinishedTaskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = unfinishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)){
                unfinishedTaskList.remove(i);  // 从未完成的集合中移除对象
                finishedTaskList.add(downloadTaskPO); // 将移除的对象添加到已经完成的集合中
                return; // 结束循环的运行
            }
        }
    }

    public static void modifyStatus(String fileId,String statusMsg){
        for (int i=0;i<=unfinishedTaskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = unfinishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)){
                downloadTaskPO.downloadMessage = statusMsg;
                unfinishedTaskList.set(i,downloadTaskPO);
                return; // 结束循环的运行
            }
        }
    }

    public static void modifyProgress(String fileId,long downloadedSize){
        for (int i=0;i<=unfinishedTaskList.size()-1;i++){
            DownloadTaskPO downloadTaskPO = unfinishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)){
                downloadTaskPO.setLastDownloadedSize(downloadTaskPO.getDownloadedSize());
                downloadTaskPO.setDownloadedSize(downloadedSize);
                unfinishedTaskList.set(i,downloadTaskPO); // 应用修改
            }
        }
    }

    /**
     * 移除未完成的列表中的项目
     * @param fileId 文件ID
     */
    public static void removeUnfinishedTask(String fileId){
        for (int i=0;i<=unfinishedTaskList.size()-1;i++) {
            DownloadTaskPO downloadTaskPO = unfinishedTaskList.get(i);
            if (downloadTaskPO.getFileId().equals(fileId)){
                unfinishedTaskList.remove(i);
                return;
            }
        }
    }
    /**
     * 清空所有已完成列表
     */
    public static void clearFinishedTask(){
        // 设置为空集
        finishedTaskList.clear();
    }

}
