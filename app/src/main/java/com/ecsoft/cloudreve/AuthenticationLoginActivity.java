package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;

public class AuthenticationLoginActivity extends AppCompatActivity {

    private Boolean   hasLoginCaptcha               = true;
    private Boolean   hasRegisterPermission         = true;
    private Boolean   hasRememberPassWordPermission = true;

    private EditText     etAuthMail;
    private EditText     etAuthPassword;
    private LinearLayout llLoginCaptchaModule;
    private EditText     etAuthCaptcha;
    private ImageView    ivAuthCaptcha;
    private Button       btnAuthenticationLogin;
    private TextView     tvRegister;
    private TextView     tvForgetPassword;

    private static final int HANDLER_NO_CAPTCHA                   = 0x10001;
    private static final int HANDLER_NO_REGISTER                  = 0x20001;
    private static final int HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS = 0x30001;
    private static final int HANDLER_LOGIN_SUCCESS                = 0x40001;
    private static final int HANDLER_LOGIN_FAILED                 = 0x40002;
    private static final int HANDLER_REFRESH_CAPTCHA              = 0x50001;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_NO_CAPTCHA:{
                    // ???????????????????????????????????????
                    llLoginCaptchaModule.setVisibility(View.GONE);
                    break;
                }
                case HANDLER_NO_REGISTER:{
                    // ????????????????????????
                    Bundle data = message.getData();
                    tvRegister.setVisibility((boolean) data.get("registerEnabled") ? View.VISIBLE : View.GONE);
                    break;
                }
                case HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS:{
                    Bundle data = message.getData();
                    String captchaImageBase64 = data.getString("captcha");
                    captchaImageBase64 = captchaImageBase64.replace("data:image/png;base64,",""); // ??????Base64???MIME??????
                    byte[] decodedString = Base64.decode(captchaImageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivAuthCaptcha.setImageBitmap(decodedByte); // ???????????????????????????ImageView???

                    break;
                }
                case HANDLER_LOGIN_SUCCESS:{

                    Toast.makeText(AuthenticationLoginActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AuthenticationLoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
                case HANDLER_LOGIN_FAILED:{
                    Bundle data = message.getData();
                    String msg   = data.getString("msg");
                    Integer code = data.getInt("code");
                    new AlertDialog.Builder(AuthenticationLoginActivity.this)
                            .setTitle("????????????")
                            .setMessage(msg+"\n???????????????"+code)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setPositiveButton("????????????",null)
                            .show();
                    refreshCaptchaCode();
                }
                case HANDLER_REFRESH_CAPTCHA:{
                    refreshCaptchaCode(); // ?????????????????????
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_login);


        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewComponent(){
        etAuthMail             = findViewById(R.id.et_auth_email);
        etAuthPassword         = findViewById(R.id.et_auth_password);
        etAuthCaptcha          = findViewById(R.id.et_auth_captcha);
        ivAuthCaptcha          = findViewById(R.id.iv_auth_captcha);
        btnAuthenticationLogin = findViewById(R.id.btn_send_reset_email);
        llLoginCaptchaModule   = findViewById(R.id.ll_forget_captcha_module);
        tvRegister             = findViewById(R.id.tv_login);
        tvForgetPassword       = findViewById(R.id.tv_forget_password);

    }
    private void initViewData(){
        // ???????????????????????????
        Thread getConfigThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // ??????config??????????????????
                String responseConfigText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new HashMap<>());
                // ?????????????????????????????????Json??????
                JSONTokener tokener = new JSONTokener(responseConfigText);
                try {
                    JSONObject responseConfigJsonObj = (JSONObject) tokener.nextValue();
                    // ??????Json???Data??????
                    JSONObject dataJsonObj = responseConfigJsonObj.getJSONObject("data");
                    // ????????????????????????????????????
                    if ((hasLoginCaptcha = dataJsonObj.getBoolean("loginCaptcha"))) {
                        // ???????????????
                        handler.sendEmptyMessage(HANDLER_REFRESH_CAPTCHA);
                    } else {
                        handler.sendEmptyMessage(HANDLER_NO_CAPTCHA);
                    }
                    // ????????????????????????
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("registerEnabled",dataJsonObj.getBoolean("registerEnabled"));
                    message.setData(bundle);
                    message.what = HANDLER_NO_REGISTER;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        getConfigThread.start();
        // ??????????????????????????????????????? email??????
        if (getIntent().hasExtra("email")) {
            etAuthMail.setText(getIntent().getStringExtra("email"));
        }
    }
    private void initViewEvent(){
        btnAuthenticationLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  TODO:??????????????????????????????
                // ?????????????????????
                // ????????????????????????????????????????????????
                if (hasLoginCaptcha && etAuthCaptcha.getText().toString().equals("")){
                    new AlertDialog.Builder(AuthenticationLoginActivity.this)
                            .setTitle("??????")
                            .setMessage("?????????????????????????????????????????????????????????")
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setPositiveButton("?????????",null)
                            .show();
                    refreshCaptchaCode();
                    return;
                }
                // ????????????????????????????????????
                if (etAuthMail.getText().toString().equals("") || etAuthPassword.getText().toString().equals("")){
                    // ????????????
                    new AlertDialog.Builder(AuthenticationLoginActivity.this)
                            .setTitle("??????")
                            .setMessage("????????????????????????????????????")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setCancelable(false)
                            .setPositiveButton("????????????",null)
                            .show();
                    return;
                }
                // ????????????????????????????????????
                Thread authenticationLoginThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // ?????????????????????
                        JSONObject requestBodyJsonObj = new JSONObject();
                        try {
                            requestBodyJsonObj.put("userName",etAuthMail.getText().toString());
                            requestBodyJsonObj.put("Password",etAuthPassword.getText().toString());
                            if (hasLoginCaptcha) requestBodyJsonObj.put("captchaCode",etAuthCaptcha.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String authenticationResponseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_authentication), new HashMap<>(),requestBodyJsonObj,GlobalRunningConfiguration.authentication_cookie_token == null?"":GlobalRunningConfiguration.authentication_cookie_token,true,AuthenticationLoginActivity.this);
                        // ??????????????????????????????JSON??????
                        JSONTokener tokener = new JSONTokener(authenticationResponseText);
                        try {
                            JSONObject authenticationResponseJsonObj = (JSONObject) tokener.nextValue();
                            if (authenticationResponseJsonObj.getInt("code") == 0){
                                // ????????????
                                handler.sendEmptyMessage(HANDLER_LOGIN_SUCCESS);
                            } else {
                                // ????????????
                                Bundle data = new Bundle();
                                data.putInt("code",authenticationResponseJsonObj.getInt("code"));
                                data.putString("msg",authenticationResponseJsonObj.getString("msg"));
                                Message message = new Message();
                                message.what = HANDLER_LOGIN_FAILED;
                                message.setData(data);

                                handler.sendMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                authenticationLoginThread.start();
            }
        });
        ivAuthCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshCaptchaCode();
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AuthenticationLoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AuthenticationLoginActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * ???????????????????????????
     */
    private void refreshCaptchaCode(){
        ivAuthCaptcha.setImageResource(R.drawable.captcha); // ???????????????????????????????????????????????????????????????
        etAuthCaptcha.setText(""); // ???????????????????????????
        Thread getCaptchaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String captchaResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_capture), new HashMap<>(), "", true, AuthenticationLoginActivity.this);
                JSONTokener tokener = new JSONTokener(captchaResponseText);
                try {
                    JSONObject captchaResponseJsonObj = (JSONObject) tokener.nextValue();
                    String captchaImageBase64 = captchaResponseJsonObj.getString("data"); // ????????????????????????Base64??????
                    Bundle data = new Bundle();
                    data.putString("captcha",captchaImageBase64);
                    Message message = new Message();
                    message.what = HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS;
                    message.setData(data);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getCaptchaThread.start();
    }
}