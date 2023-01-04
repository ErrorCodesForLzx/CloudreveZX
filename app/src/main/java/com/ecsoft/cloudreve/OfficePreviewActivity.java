package com.ecsoft.cloudreve;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OfficePreviewActivity extends AppCompatActivity {

    private WebView wvPreview;

    private Button    btnHasErrorNotice;
    private ImageView ivBack;

    // https://view.officeapps.live.com/op/view.aspx?src=地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_preview);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        initViewComponent();
        initViewData();
        initViewEvent();

    }

    private void initViewEvent() {
        btnHasErrorNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialog = LayoutInflater.from(OfficePreviewActivity.this)
                        .inflate(R.layout.dialog_office_preview_error_notice,null);
                new AlertDialog.Builder(OfficePreviewActivity.this)
                        .setView(dialog)
                        .setTitle("如果您发生了下面的错误：")
                        .setCancelable(false)
                        .setPositiveButton("我知道了",null)
                        .show();
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initViewData() {
        Intent intent = getIntent();
        String srcUrl = "";
        try {
            srcUrl = URLEncoder.encode(intent.getStringExtra("src"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String webUrl = "https://view.officeapps.live.com/op/view.aspx?src="+srcUrl;
        wvPreview.getSettings().setJavaScriptEnabled(true); // 开启JS
        wvPreview.setWebViewClient(new WebViewClient());
        wvPreview.loadUrl(webUrl);


    }

    private void initViewComponent() {
        wvPreview         = findViewById(R.id.id_wv_preview);
        ivBack            = findViewById(R.id.iv_back);
        btnHasErrorNotice = findViewById(R.id.btn_has_error_notice);
    }
}