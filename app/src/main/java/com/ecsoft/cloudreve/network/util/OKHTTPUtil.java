package com.ecsoft.cloudreve.network.util;

import android.content.Context;

import com.ecsoft.cloudreve.config.GlobalRunningConfiguration;
import com.ecsoft.cloudreve.database.DbSettingsService;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Okhttp工具包
 */
public class OKHTTPUtil {
    /**
     * 发送GET请求
     * @param url 请求地址
     * @param params 参数列表
     * @return
     */
    public static String sendGet(String url, Map<String,String> params){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .get()
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }
    /**
     * 发送带Cookie的GET请求
     * @param url 请求地址
     * @param params 参数列表
     * @param cookies 携带的cookie
     * @return
     */
    public static String sendGet(String url, Map<String,String> params,String cookies){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        // 判断cookie属性是否为null，如果为null者赋值空字符串
        cookies = cookies == null ? "" : cookies;

        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .get()
                    .header("Cookie",cookies)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Cookie",cookies)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }

    /**
     * 发送带Cookie的GET请求
     * @param url 请求地址
     * @param params 参数列表
     * @param cookies 携带的cookie
     * @return
     */
    public static String sendGet(String url, Map<String,String> params, String cookies, boolean saveResponseCookies, Context context){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        // 判断cookie属性是否为null，如果为null者赋值空字符串
        cookies = cookies == null ? "" : cookies;

        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .get()
                    .header("Cookie",cookies)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Cookie",cookies)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            if (saveResponseCookies){
                String responseCookie = response.header("Set-Cookie");
                GlobalRunningConfiguration.authentication_cookie_token = responseCookie;
                DbSettingsService service = new DbSettingsService(context);
                service.setSettings("authentication_cookie_token",responseCookie);
            }

            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }


    /**
     * 带有JSON的POST请求
     * @param url 请求地址
     * @param params url参数
     * @param postParams post参数
     * @return 返回响应文本
     */
    public static String sendPost(String url, Map<String,String> params, JSONObject postParams){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),postParams.toString());
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .post(requestBody)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }

    /**
     * 带有JSON和Cookie的POST请求
     * @param url 请求地址
     * @param params url参数
     * @param postParams post参数
     * @param cookie 提交的Cookie
     * @return 返回响应文本
     */
    public static String sendPost(String url, Map<String,String> params, JSONObject postParams,String cookie){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),postParams.toString());
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .post(requestBody)
                    .header("Cookie",cookie)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("Cookie",cookie)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }
    /**
     * 带有JSON和Cookie的POST请求
     * @param url 请求地址
     * @param params url参数
     * @param postParams post参数
     * @param cookie 提交的Cookie
     * @return 返回响应文本
     */
    public static String sendPost(String url, Map<String,String> params, JSONObject postParams,String cookie,
                                  Boolean saveResponseCookie,Context context){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),postParams.toString());
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .post(requestBody)
                    .header("Cookie",cookie)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("Cookie",cookie)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            if (saveResponseCookie){
                // 将获取到的Cookie数据写出到数据库和全局变量中
                DbSettingsService service = new DbSettingsService(context);
                String responseCookie = response.header("Set-Cookie");
                service.setSettings("authentication_cookie_token",responseCookie);
                GlobalRunningConfiguration.authentication_cookie_token = responseCookie;
            }
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }


    /**
     * 发送带Cookie的GET请求
     * @param url 请求地址
     * @param params 参数列表
     * @param cookies 携带的cookie
     * @return
     */
    public static String sendPut(String url, Map<String,String> params,String cookies){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(null, new byte[]{});
        // 判断cookie属性是否为null，如果为null者赋值空字符串
        cookies = cookies == null ? "" : cookies;

        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .put(requestBody)
                    .header("Cookie",cookies)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .header("Cookie",cookies)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }

    /**
     * 带有JSON和Cookie的PUT请求
     * @param url 请求地址
     * @param params url参数
     * @param postParams PUT参数
     * @param cookie 提交的Cookie
     * @return 返回响应文本
     */
    public static String sendPut(String url, Map<String,String> params, JSONObject postParams,String cookie){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),postParams.toString());
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .put(requestBody)
                    .header("Cookie",cookie)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .header("Cookie",cookie)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }

    /**
     * 带有JSON和Cookie的DELETE请求
     * @param url 请求地址
     * @param params url参数
     * @param deleteParams DELETE参数
     * @param cookie 提交的Cookie
     * @return 返回响应文本
     */
    public static String sendDelete(String url, Map<String,String> params, JSONObject deleteParams,String cookie){
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request;
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),deleteParams.toString());
        if (params.size() > 0) {
            request = new Request.Builder()
                    .url(url+"?"+mapToParamString(params))
                    .delete(requestBody)
                    .header("Cookie",cookie)
                    .build();
        } else  {
            request = new Request.Builder()
                    .url(url)
                    .delete(requestBody)
                    .header("Cookie",cookie)
                    .build();
        }
        Call call =  client.newCall(request);
        String responseStr = "";
        try {
            Response response = call.execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStr;

    }

    private static String mapToParamString(Map<String,String> params){
        StringBuilder generatedString = new StringBuilder();
        // 遍历MAP集合
        for (Map.Entry<String, String> item : params.entrySet()) {
            generatedString.append(item.getKey()).append("=").append(item.getValue()).append("&");
        }
        generatedString.replace(generatedString.length()-1,generatedString.length(),"");
        return generatedString.toString();
    }



}
