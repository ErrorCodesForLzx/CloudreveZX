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

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_NO_CAPTCHA:{
                    // 不需要验证码隐藏验证码属性
                    llLoginCaptchaModule.setVisibility(View.GONE);
                    break;
                }
                case HANDLER_NO_REGISTER:{
                    // 判断是否需要注册
                    Bundle data = message.getData();
                    tvRegister.setVisibility((boolean) data.get("registerEnabled") ? View.VISIBLE : View.GONE);
                    break;
                }
                case HANDLER_CAPTCHA_CODE_REFRESH_SUCCESS:{
                    Bundle data = message.getData();
                    String captchaImageBase64 = data.getString("captcha");
                    captchaImageBase64 = captchaImageBase64.replace("data:image/png;base64,",""); // 截断Base64的MIME前缀
                    byte[] decodedString = Base64.decode(captchaImageBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivAuthCaptcha.setImageBitmap(decodedByte); // 将转码的数据加载到ImageView中

                    break;
                }
                case HANDLER_LOGIN_SUCCESS:{
                    Toast.makeText(AuthenticationLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
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
                            .setTitle("登录失败")
                            .setMessage(msg+"\n错误代码："+code)
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setPositiveButton("再试一次",null)
                            .show();
                    refreshCaptchaCode();
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
        etAuthMail             = findViewById(R.id.et_dir_name);
        etAuthPassword         = findViewById(R.id.et_auth_password);
        etAuthCaptcha          = findViewById(R.id.et_auth_captcha);
        ivAuthCaptcha          = findViewById(R.id.iv_auth_captcha);
        btnAuthenticationLogin = findViewById(R.id.btn_authentication_login);
        llLoginCaptchaModule   = findViewById(R.id.ll_login_captcha_module);
        tvRegister             = findViewById(R.id.tv_register);
        tvForgetPassword       = findViewById(R.id.tv_forget_password);

    }
    private void initViewData(){
        // 更新视图的数据信息
        Thread getConfigThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 发送config请求给服务器
                String responseConfigText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_auth_config), new HashMap<>());
                // 转换获取到的配置信息到Json对象
                JSONTokener tokener = new JSONTokener(responseConfigText);
                try {
                    JSONObject responseConfigJsonObj = (JSONObject) tokener.nextValue();
                    // 读取Json的Data对象
                    JSONObject dataJsonObj = responseConfigJsonObj.getJSONObject("data");
                    // 判断登录是否需要人机校验
                    if ((hasLoginCaptcha = dataJsonObj.getBoolean("loginCaptcha"))) {
                        refreshCaptchaCode(); // 刷新一次验证码
                    } else {
                        handler.sendEmptyMessage(HANDLER_NO_CAPTCHA);
                    }
                    // 判断是否允许注册
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
    }
    private void initViewEvent(){
        btnAuthenticationLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  TODO:执行登录权限鉴定逻辑
                // 校验输入完整性
                // 校验如果有验证码，是否输入验证码
                if (hasLoginCaptcha && etAuthCaptcha.getText().toString().equals("")){
                    new AlertDialog.Builder(AuthenticationLoginActivity.this)
                            .setTitle("提示")
                            .setMessage("服务器要求输入验证码，请完成人机验证！")
                            .setCancelable(false)
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setPositiveButton("知道了",null)
                            .show();
                    refreshCaptchaCode();
                    return;
                }
                // 判断是邮箱和密码是否输入
                if (etAuthMail.getText().toString().equals("") || etAuthPassword.getText().toString().equals("")){
                    // 输入为空
                    new AlertDialog.Builder(AuthenticationLoginActivity.this)
                            .setTitle("提示")
                            .setMessage("请输入完整的邮箱和密码！")
                            .setIcon(R.drawable.ic_icon_dialog_warning)
                            .setCancelable(false)
                            .setPositiveButton("我知道了",null)
                            .show();
                    return;
                }
                // 输入验证完毕开始发送请求
                Thread authenticationLoginThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 构造请求体对象
                        JSONObject requestBodyJsonObj = new JSONObject();
                        try {
                            requestBodyJsonObj.put("userName",etAuthMail.getText().toString());
                            requestBodyJsonObj.put("Password",etAuthPassword.getText().toString());
                            if (hasLoginCaptcha) requestBodyJsonObj.put("captchaCode",etAuthCaptcha.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String authenticationResponseText = OKHTTPUtil.sendPost(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_authentication), new HashMap<>(),requestBodyJsonObj,GlobalRunningConfiguration.authentication_cookie_token == null?"":GlobalRunningConfiguration.authentication_cookie_token,true,AuthenticationLoginActivity.this);
                        // 将获取到的文本转换为JSON对象
                        JSONTokener tokener = new JSONTokener(authenticationResponseText);
                        try {
                            JSONObject authenticationResponseJsonObj = (JSONObject) tokener.nextValue();
                            if (authenticationResponseJsonObj.getInt("code") == 0){
                                // 登录成功
                                handler.sendEmptyMessage(HANDLER_LOGIN_SUCCESS);
                            } else {
                                // 登录失败
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
    }

    /**
     * 定义刷新验证码逻辑
     */
    private void refreshCaptchaCode(){
        ivAuthCaptcha.setImageResource(R.drawable.captcha); // 替换为加载中的图片避免网络问题导致时间过久
        etAuthCaptcha.setText(""); // 将输入的验证码置空
        Thread getCaptchaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String captchaResponseText = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_get_capture), new HashMap<>(), "", true, AuthenticationLoginActivity.this);
                JSONTokener tokener = new JSONTokener(captchaResponseText);
                try {
                    JSONObject captchaResponseJsonObj = (JSONObject) tokener.nextValue();
                    String captchaImageBase64 = captchaResponseJsonObj.getString("data"); // 获取服务器传递的Base64数据
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