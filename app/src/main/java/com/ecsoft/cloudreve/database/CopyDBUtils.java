package com.ecsoft.cloudreve.database;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CopyDBUtils {

    private static InputStream is;
    private static FileOutputStream fos;

    public static void copyDbFile(Context context, String dbName) {

        //databases文件夹目录
        String path = "/data/data/" + context.getPackageName();

        //后面通过SQLiteDatabase.openDatabas获取数据库
        File file = new File(path + "/" + dbName);
        try {
            //创建文件夹（如果之前没有创建过数据库，data/data/包名/下的databases目录是不存在的）
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            /*
            * mkdir() 与 mkdirs() 的区别
            * mkdirs()可以创建多级目录文件
            * 比如要在 C:\a\b\ 目录下创建一个文件test.txt  (此时不存在文件夹a和b)
            *  mkdirs()就会先创建文件夹a，文件夹b。最后创建test.txt问价
            * 但是mkdir()则创建失败,返回创建结果false
            * */
            if (file.exists())//删除已经存在的
                file.deleteOnExit();
            //创建新的文件
            if (!file.exists())
                file.createNewFile();
            // 从assets目录下复制
            is = context.getAssets().open(dbName);
            fos = new FileOutputStream(file);
            int len = 0;
            byte[] b = new byte[1024];
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
