package com.ecsoft.cloudreve.network.config;

public class NetworkTrafficUrlBuilder {

    public static String networkUrl = "";

    /**
     * 初始化网络信息
     * @param baseUrl 网址
     * @param basePort 端口
     */
    public static void init(String baseUrl,String basePort){
        // networkUrl = !requireSSL ? "http://" : "https://";
        networkUrl += baseUrl + ":" + basePort;
    }

    /**
     * 构造交通字符串
     * @param traffic 路由交通，需要使用 NetworkTrafficRouter 类型获取
     * @return 返回构建的字符串
     */
    public static String build(String traffic){
        return networkUrl + traffic;
    }
}
