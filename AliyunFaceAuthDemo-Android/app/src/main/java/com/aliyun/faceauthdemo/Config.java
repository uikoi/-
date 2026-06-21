package com.aliyun.faceauthdemo;

/**
 * 阿里云金融级实人认证配置
 */
public class Config {

    /**
     * 认证场景ID
     */
    public static final String SCENE_ID = "1000018873";

    /**
     * 业务服务器地址（ngrok穿透）
     * 接口：
     *   POST /faceauth/init  → 接收metaInfo，返回certifyId
     *   POST /faceauth/query → 接收certifyId，返回认证结果
     */
    public static final String SERVER_URL = "https://blend-twelve-clamor.ngrok-free.dev";

    /**
     * 阿里云区域
     */
    public static final String REGION = "cn-shanghai";

    /**
     * 是否生产环境
     */
    public static final boolean IS_PRODUCTION = false;
}