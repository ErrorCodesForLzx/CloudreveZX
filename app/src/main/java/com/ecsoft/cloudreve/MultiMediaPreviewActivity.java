package com.ecsoft.cloudreve;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.ecsoft.cloudreve.preview.MultiMediaPreviewWebHtmlUtil;

public class MultiMediaPreviewActivity extends AppCompatActivity {

    private WebView wvPreview;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_media_preview);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewEvent() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initViewData() {
        Intent intent = getIntent();
        String srcUrl = intent.getStringExtra("src");
        String multiType = intent.getStringExtra("multiType");
        String showHtml = MultiMediaPreviewWebHtmlUtil.readAssetsText(MultiMediaPreviewActivity.this, multiType);
        showHtml = showHtml.replace("{THE_APP_SRC_URL}",srcUrl); // 将占位符替换
        wvPreview.getSettings().setJavaScriptEnabled(true); // 允许JavaScript代码运行
        wvPreview.setWebViewClient(new WebViewClient()); // 设置客户
        wvPreview.loadData(showHtml,"text/html","UTF-8"); // 加载多媒体链接

    }

    private void initViewComponent() {
        wvPreview = findViewById(R.id.id_wv_preview);
        ivBack    = findViewById(R.id.iv_back);
    }
}