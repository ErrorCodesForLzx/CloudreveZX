package com.ecsoft.cloudreve;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.daimajia.androidanimations.library.YoYo;

public class WelcomeActivity extends AppCompatActivity {


    private Button btnStartBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initViewComponent();
        initViewEvent();

    }


    public void initViewComponent(){
        btnStartBuild = findViewById(R.id.btn_start_build);
    }

    public void initViewEvent(){
        btnStartBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,BuildActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}