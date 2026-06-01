# 阿里云金融级实人认证 iOS Demo

本项目提供阿里云金融级实人认证服务的iOS端集成示例代码。

## 功能特性

- 完整的iOS原生项目结构
- SDK初始化封装
- 认证流程管理
- 网络请求处理
- UI交互实现

## 项目结构

```
AliyunFaceAuthDemo/
├── Sources/
│   ├── App/
│   │   ├── AppDelegate.swift
│   │   ├── SceneDelegate.swift
│   │   └── Info.plist
│   ├── Controllers/
│   │   └── MainViewController.swift
│   ├── Managers/
│   │   ├── AuthManager.swift
│   │   └── NetworkManager.swift
│   ├── Utils/
│   │   └── Config.swift
│   └── Resources/
│       ├── Assets.xcassets/
│       └── LaunchScreen.storyboard
├── project.yml
├── Podfile
└── README.md
```

## 快速开始

### 1. 环境要求

- macOS 10.15+
- Xcode 15.0+
- iOS 9.0+ 设备或模拟器（注意：SDK不支持模拟器调试）
- CocoaPods

### 2. 获取阿里云SDK

1. 登录[阿里云实人认证控制台](https://yundun.console.aliyun.com/?p=cloudauth)
2. 创建认证场景，获取SceneId
3. 获取AccessKey ID和AccessKey Secret
4. 下载iOS SDK（需联系商务经理获取下载链接）
5. 当前推荐版本：2.3.45 (xcframework)

### 3. 配置项目

#### 3.1 修改Config.swift

打开`Sources/Utils/Config.swift`，填入你的配置信息：

```swift
struct Config {
    static let sceneId = "YOUR_SCENE_ID"           // 从控制台获取
    static let accessKeyId = "YOUR_ACCESS_KEY_ID"  // 阿里云AccessKey
    static let accessKeySecret = "YOUR_ACCESS_KEY_SECRET"
    static let aliyunRegion = "cn-shanghai"
    static let isProduction = false
}
```

#### 3.2 配置服务端

你需要搭建一个业务服务器，实现以下两个接口：

**接口1：发起认证**
- 接收客户端的metaInfo
- 调用阿里云InitFaceVerify接口
- 返回certifyId给客户端

**接口2：查询认证结果**
- 接收客户端的certifyId
- 调用阿里云DescribeFaceVerify接口
- 返回认证结果

修改`NetworkManager.swift`中的服务器地址：
```swift
private static let serverBaseURL = "https://your-business-server.com/api"
```

### 4. 集成阿里云SDK

#### 4.1 手动集成

1. 解压SDK包：
```bash
for i in $(ls *.tgz);do tar xvf $i;done
```

2. 将所有framework文件添加到项目中

3. 添加系统依赖库：
```
CoreGraphics.framework
Accelerate.framework
SystemConfiguration.framework
AssetsLibrary.framework
CoreTelephony.framework
QuartzCore.framework
CoreFoundation.framework
CoreLocation.framework
ImageIO.framework
CoreMedia.framework
CoreMotion.framework
AVFoundation.framework
WebKit.framework
libresolv.tbd
libz.tbd
libc++.tbd
AudioToolbox.framework
CFNetwork.framework
MobileCoreServices.framework
AdSupport.framework
ReplayKit.framework
```

4. 添加Bundle资源：
- APBToygerFacade.bundle
- ToygerService.bundle
- OCRDetectSDKForTech.bundle
- BioAuthEngine.bundle
- MultiFactorFacade.bundle
- APBToygerFacadeSuitable.bundle

5. 配置Other Linker Flags：`-ObjC`

#### 4.2 CocoaPods集成（推荐）

```bash
cd AliyunFaceAuthDemo
pod install
```

### 5. 编译运行

1. 使用Xcode打开`.xcworkspace`文件
2. 连接真机设备（模拟器不支持SDK）
3. 选择目标设备和证书
4. 点击运行

## 认证流程

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   iOS App    │────▶│  业务服务器   │────▶│   阿里云     │────▶│   认证SDK    │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
      │                    │                                         │
      │  1. 获取metaInfo   │                                         │
      │───────────────────▶│                                         │
      │                    │  2. 调用InitFaceVerify                  │
      │                    │────────────────────────────────────────▶│
      │                    │◀─────────────────────────────────────────│
      │                    │  3. 返回certifyId                       │
      │◀───────────────────│                                         │
      │  4. 使用certifyId  │                                         │
      │──────────────────────────────────────────────────────────────▶│
      │                                                               │
      │  5. 认证完成 (SDK回调)                                        │
      │◀───────────────────────────────────────────────────────────────│
      │                                                               │
      │  6. 查询认证结果                                              │
      │──────────────────────────────────────────────────────────────▶│
```

## 注意事项

1. **真机调试**：SDK不支持模拟器，必须使用真机进行测试和调试

2. **权限配置**：确保在Info.plist中配置了相机和麦克风权限

3. **网络要求**：设备需要能够访问公网，以便与阿里云服务通信

4. **安全合规**：按照相关法律法规要求，在隐私政策中披露人脸信息使用情况

5. **结果校验**：客户端SDK返回的结果仅供参考，最终认证结果应以服务端查询接口为准

## 常见问题

### Q: SDK初始化失败怎么办？
A: 检查网络连接、SDK包完整性、证书配置等

### Q: 认证失败如何排查？
A: 查看阿里云控制台的认证记录，获取详细的错误信息

### Q: 如何获取更多帮助？
A: 访问[阿里云实人认证文档](https://help.aliyun.com/zh/id-verification/financial-grade-id-verification/)

## License

本示例代码仅供学习参考，实际使用请遵守阿里云服务条款和相关法律法规。
