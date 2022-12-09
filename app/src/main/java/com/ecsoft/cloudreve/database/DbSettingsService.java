package com.ecsoft.cloudreve.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 全局设置持久化数据库服务
 */
public class DbSettingsService {
    private Context ctx;
    private SQLiteDatabase database;

    private SQLiteDatabase getDatabase(Context context){
        String path = "/data/data/" + context.getPackageName() + "/data.db";
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
    }


    public DbSettingsService(Context ctx) {
        this.ctx = ctx;
        database = getDatabase(ctx);

    }

    /**
     * 获取设置值
     * @param key 设置键
     * @return 返回设置值
     */
    @SuppressLint("Recycle")
    public String getSettings(String key){
        Cursor cursor = database.rawQuery( "select * from settings where `key`=?", new String[]{key});
        cursor.moveToFirst();
        return cursor.getString(1);
    }

    /**
     * 设置值
     * @param key 键
     * @param value 值
     */
    @SuppressLint("Recycle")
    public void setSettings(String key,String value){
        ContentValues contentValues = new ContentValues();
        contentValues.put("value",value);
        database.update("settings",contentValues,"key=?",new String[]{key});
    }
}
