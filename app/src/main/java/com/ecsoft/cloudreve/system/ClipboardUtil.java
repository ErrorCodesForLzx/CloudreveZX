package com.ecsoft.cloudreve.system;

import android.content.ClipboardManager;
import android.content.Context;

import com.ecsoft.cloudreve.FileInfoActivity;

public class ClipboardUtil {
    public static void copyToSystemClipboard(Context context,String text){
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(text);

    }
}
