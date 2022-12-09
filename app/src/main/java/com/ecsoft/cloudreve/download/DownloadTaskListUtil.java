package com.ecsoft.cloudreve.download;

import android.content.Context;

import com.ecsoft.cloudreve.database.DbSettingsService;
import com.ecsoft.cloudreve.download.entity.DownloadTaskPO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class DownloadTaskListUtil {

    /**
     * 在数据库中获取完成的任务
     * @param context 上下文对象
     * @return 返回任务集合
     */
    public static List<DownloadTaskPO> getFinishedTask(Context context){
        DbSettingsService db = new DbSettingsService(context);
        String finishedTaskText = db.getSettings("download_finished_task");
        JSONTokener tokener = new JSONTokener(finishedTaskText);
        List<DownloadTaskPO> downloadTaskPOList = new ArrayList<>();
        try {
            JSONObject finishedTask = (JSONObject) tokener.nextValue();
            JSONArray tasks = finishedTask.getJSONArray("tasks");

            for (int i = 0 ;i <= tasks.length()-1;i++){
                JSONObject taskJson = tasks.getJSONObject(i);
                DownloadTaskPO taskPO = new DownloadTaskPO();
                taskPO.setFileId(taskJson.getString("fileId"));
                taskPO.setDownloadedByte(taskJson.getLong("downloadedByte"));
                taskPO.setTotalByte(taskJson.getLong("totalByte"));
                taskPO.setNowStatus(taskJson.getInt("nowStatus"));
                downloadTaskPOList.add(taskPO);
            }

            return downloadTaskPOList;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * 在数据库中获取完成的任务
     * @param context 上下文对象
     * @return 返回任务集合
     */
    public static List<DownloadTaskPO> getUnFinishedTask(Context context){
        DbSettingsService db = new DbSettingsService(context);
        String finishedTaskText = db.getSettings("download_unfinished_task");
        JSONTokener tokener = new JSONTokener(finishedTaskText);
        List<DownloadTaskPO> downloadTaskPOList = new ArrayList<>();
        try {
            JSONObject finishedTask = (JSONObject) tokener.nextValue();
            JSONArray tasks = finishedTask.getJSONArray("tasks");

            for (int i = 0 ;i <= tasks.length()-1;i++){
                JSONObject taskJson = tasks.getJSONObject(i);
                DownloadTaskPO taskPO = new DownloadTaskPO();
                taskPO.setFileId(taskJson.getString("fileId"));
                taskPO.setDownloadedByte(taskJson.getLong("downloadedByte"));
                taskPO.setTotalByte(taskJson.getLong("totalByte"));
                taskPO.setNowStatus(taskJson.getInt("nowStatus"));
                downloadTaskPOList.add(taskPO);
            }

            return downloadTaskPOList;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void setFinishedTaskList(Context context,List<DownloadTaskPO> tasks){
        DbSettingsService db = new DbSettingsService(context);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i=0;i<=tasks.size()-1;i++){
                jsonArray.put(tasks.get(i));
            }
            jsonObject.put("tasks",jsonArray);
            db.setSettings("download_finished_task",jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void setUnfinishedTaskList(Context context,List<DownloadTaskPO> tasks){
        DbSettingsService db = new DbSettingsService(context);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i=0;i<=tasks.size()-1;i++){
                jsonArray.put(tasks.get(i));
            }
            jsonObject.put("tasks",jsonArray);
            db.setSettings("download_unfinished_task",jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 插入任务到完成列表
     * @param context
     * @param task 需要插入的任务
     */
    public static void insertFinishedTask(Context context,DownloadTaskPO task){
        List<DownloadTaskPO> finishedTask = getFinishedTask(context);
        if (!judgeRepetitiveTask(finishedTask,task)){ // 查重
            finishedTask.add(task); // 插入任务
            setFinishedTaskList(context,finishedTask); // 添加到数据库
        }
    }
    /**
     * 插入任务到完成列表
     * @param context
     * @param task 需要插入的任务
     */
    public static void insertUnfinishedTask(Context context,DownloadTaskPO task){
        List<DownloadTaskPO> unfinishedTask = getFinishedTask(context);
        if (!judgeRepetitiveTask(unfinishedTask,task)){ // 查重
            unfinishedTask.add(task); // 插入任务
            setUnfinishedTaskList(context,unfinishedTask); // 添加到数据库
        }
    }

    /**
     * 是否具有重复项目
     * @param list
     * @param task
     * @return
     */
    public static boolean judgeRepetitiveTask(List<DownloadTaskPO> list,DownloadTaskPO task){
        for (int i = 0;i <= list.size()-1;i++){
            DownloadTaskPO taskPO = list.get(i);
            if (taskPO.getFileId().equals(task.getFileId())) return true;
        }
        return false;
    }
}
