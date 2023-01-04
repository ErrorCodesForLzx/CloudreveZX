package com.ecsoft.cloudreve.network;

import com.ecsoft.cloudreve.network.config.NetworkTrafficRouter;
import com.ecsoft.cloudreve.network.config.NetworkTrafficUrlBuilder;
import com.ecsoft.cloudreve.network.util.OKHTTPUtil;

import java.util.HashMap;
import java.util.Objects;

/**
 * 检测服务器网络连通性
 */
public class CheckNetworkConnection {
    /**
     * 检测网络连通性
     * @return 返回连通性
     */
    public static boolean check(){
        // http://192.168.0.107:5212/login 测试地址
        try{
            String s = OKHTTPUtil.sendGet(NetworkTrafficUrlBuilder.build(NetworkTrafficRouter.network_server_test), new HashMap<>());
            if (Objects.equals(s, "")){
                // 如果返回空也带表服务器寄
                return false;
            }
            return true;
        } catch (Exception e){
            // 发生异常服务器寄
            return false;
        }

    }
    public static boolean check(String url){
        try{
            String s = OKHTTPUtil.sendGet(url, new HashMap<>());
            if (Objects.equals(s, "")){
                // 如果返回空也带表服务器寄
                return false;
            }
            return true;
        } catch (Exception e){
            // 发生异常服务器寄
            return false;
        }
    }
}
