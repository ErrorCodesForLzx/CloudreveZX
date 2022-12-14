package com.ecsoft.cloudreve;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
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

public class RegisterActivity extends AppCompatActivity {

    private boolean hasRegCaptcha = false;

    private EditText etAuthEmail;
    private EditText etAuthPassword;
    private EditText etAuthRepeat;
    private EditText etAuthCaptcha;
    private ImageView ivAuthCaptcha;
    private EditText etAuthMailKey;
    private TextView tvSendMailKey;
    private TextView tvLogin;
    private Button btnAuthenticationRegister;
    private LinearLayout llRegisterCaptchaModule;

    private String email;
    private String password;
    private String repeatPwd;
    private String captcha;

    private static final int HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS = 0x10001;
    private static final int HANDLER_NO_CAPTCHA = 0x20001;
    private static final int HANDLER_REG_SUCCESS = 0x30001;
    private static final int HANDLER_REG_MAIL_AUTH = 0x30002;
    private static final int HANDLER_REFRESH_CAPTCHA = 0x40001;
    private static final int HANDLER_REG_FAILED     = 0x30003;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case HANDLER_NO_CAPTCHA: {
                    // ?????????????????????
                    llRegisterCaptchaModule.setVisibility(View.GONE);
                    break;
                }
                case HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS: {
                    // ?????????????????????
                    Bundle data = message.getData();
                    String captchaImageBase64 = data.getString("captcha");
                    captchaImageBase64 = captchaImageBase64.replace("data:image/png;base64,", ""); // ??????Base64???MIME??????
                    byte[] decodedString = Base64.decode(captchaImageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivAuthCaptcha.setImageBitmap(decodedByte); // ???????????????????????????ImageView???
                    break;
                }
                case HANDLER_REG_SUCCESS:{
                    // ??????????????????????????????????????????
                    Toast.makeText(RegisterActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this,AuthenticationLoginActivity.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                    finish();
                    break;
                }
                case HANDLER_REG_FAILED:{
                    Bundle data = message.getData();
                    String errorMsg = data.getString("msg");
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("????????????")
                            .setMessage("???????????????????????????"+errorMsg+"\n???????????????"+HANDLER_REG_FAILED)
                            .setPositiveButton("?????????",null)
                            .setCancelable(false)
                            .show();
                    break;
                }
                case HANDLER_REG_MAIL_AUTH:{
                    // ??????????????????
                    // ??????????????????
                    View dialogView = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.dialog_reg_email_auth,null);
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setView(dialogView)
                            .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // ??????????????????????????????????????????
                                    Intent intent = new Intent(RegisterActivity.this,AuthenticationLoginActivity.class);
                                    intent.putExtra("email",email);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                case HANDLER_REFRESH_CAPTCHA:{
                    refreshCaptchaCode();
                    break;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViewComponent();
        initViewData();
        initViewEvent();
    }

    private void initViewData() {
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
                    if ((hasRegCaptcha = dataJsonObj.getBoolean("regCaptcha"))) {
                        handler.sendEmptyMessage(HANDLER_REFRESH_CAPTCHA);
                    } else {
                        handler.sendEmptyMessage(HANDLER_NO_CAPTCHA);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        getConfigThread.start();
    }

    private void initViewEvent() {
        btnAuthenticationRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????????????????????????????
                // ????????????????????????
                email = etAuthEmail.getText().toString();
                password = etAuthPassword.getText().toString();
                repeatPwd = etAuthRepeat.getText().toString();
                captcha = etAuthCaptcha.getText().toString();
                if (hasRegCaptcha) {
                    if (captcha.isEmpty()) {
                        // ???????????????
                        Toast.makeText(RegisterActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                        refreshCaptchaCode(); // ???????????????
                        return;
                    }
                }
                if (!email.equals("") && !password.equals("") && !repeatPwd.equals("")) {

                    if (password.equals(repeatPwd)) {
                        // ????????????
                        Thread registerUser = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // ????????????Json??????
                                JSONObject requestJsonObj = new JSONObject();
                                try {

                                    requestJsonObj.put("Password", password);
                                    requestJsonObj.put("userName", email);
                                    // ???????????????????????????????????????????????????
                                    if (hasRegCaptcha) requestJsonObj.put("captchaCode", captcha);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // ??????HTTP
                                String responseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_reg_user), new HashMap<>(), requestJsonObj, GlobalRunningConfiguration.authentication_cookie_token);
                                JSONTokener tokener = new JSONTokener(responseText);
                                try {
                                    JSONObject responseJsonObj = (JSONObject) tokener.nextValue();
                                    // ??????code
                                    int code = responseJsonObj.getInt("code");
                                    switch (code){
                                        case 203:{
                                            // ??????????????????????????????
                                            handler.sendEmptyMessage(HANDLER_REG_MAIL_AUTH);
                                            break;
                                        }
                                        case 0:{
                                            // ??????????????????
                                            handler.sendEmptyMessage(HANDLER_REG_SUCCESS);
                                            break;
                                        }
                                        default:{
                                            // ???????????????
                                            Bundle data = new Bundle();
                                            data.putString("msg",responseJsonObj.getString("msg"));
                                            handler.sendEmptyMessage(HANDLER_REG_FAILED);
                                            break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        registerUser.start();
                    } else {
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle("??????")
                                .setIcon(R.drawable.ic_icon_dialog_warning)
                                .setMessage("??????????????????????????????????????????")
                                .setPositiveButton("?????????", null)
                                .setCancelable(false)
                                .show();
                    }

                } else {
                    new AlertDialog.Builder(RegisterActivity.this)
                            .setTitle("??????")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setMessage("????????????????????????")
                            .setPositiveButton("?????????", null)
                            .setCancelable(false)
                            .show();
                    return;
                }
            }
        });
        ivAuthCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.sendEmptyMessage(HANDLER_REFRESH_CAPTCHA);
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,AuthenticationLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViewComponent() {
        etAuthEmail = findViewById(R.id.et_auth_email);
        etAuthPassword = findViewById(R.id.et_auth_password);
        etAuthRepeat = findViewById(R.id.et_auth_repeat);
        etAuthCaptcha = findViewById(R.id.et_auth_captcha);
        ivAuthCaptcha = findViewById(R.id.iv_auth_captcha);
        tvLogin = findViewById(R.id.tv_login);
        llRegisterCaptchaModule = findViewById(R.id.ll_forget_captcha_module);
        btnAuthenticationRegister = findViewById(R.id.btn_authentication_register);
    }

    /**
     * ???????????????????????????
     */
    private void refreshCaptchaCode() {
        ivAuthCaptcha.setImageResource(R.drawable.captcha); // ???????????????????????????????????????????????????????????????
        etAuthCaptcha.setText(""); // ???????????????????????????
        Thread getCaptchaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String captchaResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_capture), new HashMap<>(), "", true, RegisterActivity.this);
                JSONTokener tokener = new JSONTokener(captchaResponseText);
                try {
                    JSONObject captchaResponseJsonObj = (JSONObject) tokener.nextValue();
                    String captchaImageBase64 = captchaResponseJsonObj.getString("data"); // ????????????????????????Base64??????
                    Bundle data = new Bundle();
                    data.putString("captcha", captchaImageBase64);
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