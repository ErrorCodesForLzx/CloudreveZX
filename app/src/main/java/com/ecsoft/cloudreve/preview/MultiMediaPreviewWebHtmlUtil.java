package com.ecsoft.cloudreve.preview;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MultiMediaPreviewWebHtmlUtil {


    private static String parseBytesToString(byte[] buf) {
        byte[] vsnFileByte = new byte[buf.length];
        System.arraycopy(buf, 0, vsnFileByte, 0, buf.length);
        return  new String(vsnFileByte);
    }
    //
    public static String readAssetsText(Context context, String assetsName){
        try {
            InputStream is = context.getAssets().open(assetsName);
            int allBytesSize = is.available();
            byte[] readiedBytes = new byte[allBytesSize];
            is.read(readiedBytes);
            return parseBytesToString(readiedBytes) ;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String readImageHtml(Context context){
        return readAssetsText(context,"image.html");
    }
    public static String readAudioHtml(Context context){
        return readAssetsText(context,"audio.html");
    }
}
