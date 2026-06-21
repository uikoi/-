package com.aliyun.faceauthdemo;

/**
 * 阿里云金融级实人认证配置
 *
 * 使用前请修改以下配置：
 * 1. SCENE_ID - 在阿里云控制台创建的认证场景ID
 * 2. SERVER_URL - 你的业务服务器地址
 */
public class Config {

    /**
     * 认证场景ID
     * 在阿里云实人认证控制台创建认证场景后获取
     * https://yundun.console.aliyun.com/?p=cloudauth
     */
    public static final String SCENE_ID = "YOUR_SCENE_ID";

    /**
     * 业务服务器地址
     * 需要你搭建一个业务服务器，实现以下两个接口：
     * 1. POST /faceauth/init - 接收metaInfo，调用阿里云InitFaceVerify，返回certifyId
     * 2. POST /faceauth/query - 接收certifyId，调用阿里云DescribeFaceVerify，返回认证结果
     */
    public static final String SERVER_URL = "https://your-business-server.com/api";

    /**
     * 阿里云区域
     */
    public static final String REGION = "cn-shanghai";

    /**
     * 是否生产环境
     */
    public static final boolean IS_PRODUCTION = false;
}