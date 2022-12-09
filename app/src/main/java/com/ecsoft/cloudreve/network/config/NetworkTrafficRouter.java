package com.ecsoft.cloudreve.network.config;

/**
 * 定义网络交通地址
 */
public class NetworkTrafficRouter {
    public static final String network_server_test            = "/static/img/favicon.ico";
    public static final String network_get_auth_config        = "/api/v3/site/config";
    public static final String network_get_capture            = "/api/v3/site/captcha";
    public static final String network_authentication         = "/api/v3/user/session";
    public static final String network_get_file_tree          = "/api/v3/directory";
    public static final String network_get_file_download      = "/api/v3/file/download"; // PUT METHOD
    public static final String network_get_file_delete        = "/api/v3/object"; // DELETE METHOD
    public static final String network_get_user_config        = "/api/v3/site/config";
    public static final String network_get_user_storage       = "/api/v3/user/storage";
    public static final String network_file_tree_filter_video = "/api/v3/file/search/video%2Finternal";
    public static final String network_file_tree_filter_image = "/api/v3/file/search/image%2Finternal";
    public static final String network_file_tree_filter_audio = "/api/v3/file/search/audio%2Finternal";
    public static final String network_file_tree_filter_doc   = "/api/v3/file/search/doc%2Finternal";
    public static final String network_file_create_dir        = "/api/v3/directory"; // PUT METHOD;
    public static final String network_share_file             = "/api/v3/share";


}
