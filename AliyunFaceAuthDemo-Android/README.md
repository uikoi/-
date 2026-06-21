# 阿里云金融级实人认证 Android Demo

## 项目结构

```
AliyunFaceAuthDemo-Android/
├── build.gradle                   # 根构建配置
├── settings.gradle                # 项目设置
├── gradle.properties              # Gradle属性
└── app/
    ├── build.gradle               # 模块构建配置
    ├── proguard-rules.pro         # 混淆规则
    └── src/main/
        ├── AndroidManifest.xml    # 清单文件
        ├── java/com/aliyun/faceauthdemo/
        │   ├── MainActivity.java  # 主界面 & 认证逻辑
        │   └── Config.java        # 配置文件
        ├── res/
        │   ├── layout/
        │   │   └── activity_main.xml
        │   └── values/
        │       ├── strings.xml
        │       ├── colors.xml
        │       └── themes.xml
        └── libs/                  # ← 把SDK的aar文件放这里
```

## 快速开始

### 1. 环境要求

| 工具 | 版本要求 |
|------|---------|
| Android Studio | 2022.1+ (Hedgehog) |
| JDK | 8 或 11 |
| Gradle | 7.4+ |
| Android SDK | API 33+ |
| 真机 | Android 5.0+ |

> **注意：SDK不支持模拟器调试，必须使用真机！**

### 2. 获取阿里云SDK

1. 登录[阿里云实人认证控制台](https://yundun.console.aliyun.com/?p=cloudauth)
2. 创建认证场景，获取 **SceneId**
3. 下载 Android SDK（联系商务经理获取下载链接）
4. 解压SDK，将里面的 **所有 .aar 文件** 复制到 `app/libs/` 目录

### 3. 修改配置

打开 `app/src/main/java/com/aliyun/faceauthdemo/Config.java`：

```java
public class Config {
    // 改成你的场景ID
    public static final String SCENE_ID = "1000000xxx";
    
    // 改成你的业务服务器地址
    public static final String SERVER_URL = "https://your-server.com/api";
}
```

### 4. 搭建业务服务器

你需要一个后端服务，实现两个接口：

**接口1：POST /faceauth/init**
```
请求: { "metaInfo": "...", "sceneId": "..." }
响应: { "certifyId": "xxx" }
```
后端调用阿里云 `InitFaceVerify` 接口获取 `certifyId` 并返回。

**接口2：POST /faceauth/query**
```
请求: { "certifyId": "..." }
响应: { "passed": true/false }
```
后端调用阿里云 `DescribeFaceVerify` 接口查询结果并返回。

### 5. 导入并运行

1. 打开 Android Studio
2. File → Open → 选择本项目文件夹
3. 等待 Gradle 同步完成
4. 连接真机
5. 点击 Run

## 认证流程

```
┌──────────┐      ┌──────────────┐      ┌──────────┐      ┌──────────┐
│  Android  │      │  业务服务器   │      │  阿里云   │      │  认证SDK  │
│   App     │      │              │      │          │      │          │
└─────┬─────┘      └──────┬───────┘      └────┬─────┘      └────┬─────┘
      │                   │                   │                  │
      │ 1. 初始化SDK      │                   │                  │
      │─────────────────────────────────────────────────────────▶│
      │                   │                   │                  │
      │ 2. 获取metaInfo   │                   │                  │
      │─────────────────────────────────────────────────────────▶│
      │                   │                   │                  │
      │ 3. 发送metaInfo   │                   │                  │
      │──────────────────▶│                   │                  │
      │                   │ 4. InitFaceVerify │                  │
      │                   │──────────────────▶│                  │
      │                   │◀──────────────────│                  │
      │                   │ 5. 返回certifyId  │                  │
      │◀──────────────────│                   │                  │
      │                   │                   │                  │
      │ 6. 使用certifyId发起认证              │                  │
      │─────────────────────────────────────────────────────────▶│
      │                   │                   │                  │
      │ 7. 认证结果回调   │                   │                  │
      │◀──────────────────────────────────────────────────────────│
      │                   │                   │                  │
      │ 8. 查询服务端结果 │                   │                  │
      │──────────────────▶│                   │                  │
      │                   │ 9. DescribeFaceVerify               │
      │                   │──────────────────▶│                  │
      │                   │◀──────────────────│                  │
      │◀──────────────────│                   │                  │
```

## 错误码说明

| 错误码 | 是否计费 | 说明 |
|--------|---------|------|
| 1000 | 是 | 认证成功 |
| 1003 | 否 | 用户退出 |
| 2002 | 否 | 网络错误 |
| 2003 | 否 | 设备时间错误 |
| 2006 | 是 | 认证失败 |

## 注意事项

1. **真机测试**：SDK不支持模拟器，必须真机调试
2. **CertifyId有效期**：每个CertifyId仅30分钟有效，且只能使用一次
3. **结果校验**：客户端SDK返回的结果仅供参考，以服务端查询结果为准
4. **权限获取**：Android 6.0+需要动态申请相机和录音权限
5. **安全合规**：在隐私政策中披露人脸信息使用情况，获得用户同意后再初始化SDK

## 官方文档

- [金融级实人认证产品概述](https://help.aliyun.com/zh/id-verification/financial-grade-id-verification/)
- [Android接入文档](https://help.aliyun.com/zh/id-verification/financial-grade-id-verification/integration-by-using-android-intent-verification)
- [SDK发布记录](https://help.aliyun.com/zh/id-verification/financial-grade-id-verification/product-overview/sdk-release-notes)