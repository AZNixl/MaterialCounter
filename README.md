# 材料计数器 (Material Counter)

一个简单的材料计数APP，使用Google ML Kit实现本地离线条形码/二维码识别和计数功能。

## 功能特点

- ✅ **本地离线识别**：使用Google ML Kit Barcode Scanning，无需联网
- 📷 **相机实时预览**：基于CameraX实现流畅的相机体验
- 🔢 **自动计数**：识别并统计不重复的条形码/二维码数量
- 📝 **历史记录**：自动保存每次计数结果，最多保存100条
- 🎨 **Material Design**：遵循Material Design设计规范

## 技术栈

- **语言**：Kotlin
- **最低SDK**：Android 7.0 (API 24)
- **目标SDK**：Android 14 (API 34)
- **核心库**：
  - CameraX 1.3.1 (相机功能)
  - ML Kit Barcode Scanning 17.2.0 (条形码识别)
  - Gson (数据存储)
  - Material Components (UI组件)

## 项目结构

```
MaterialCounter/
├── app/
│   ├── src/main/
│   │   ├── java/com/materialcounter/app/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   ├── CameraActivity.kt        # 相机识别界面
│   │   │   └── HistoryActivity.kt       # 历史记录界面
│   │   ├── res/
│   │   │   ├── layout/                  # 布局文件
│   │   │   ├── values/                  # 资源文件
│   │   │   └── mipmap/                  # 图标资源
│   │   └── AndroidManifest.xml
│   ├── build.gradle                     # 应用级配置
│   └── proguard-rules.pro              # 混淆规则
├── build.gradle                         # 项目级配置
├── settings.gradle                      # 项目设置
└── gradle.properties                    # Gradle属性

```

## 构建说明

1. 确保安装了Android Studio和JDK 17+
2. 打开项目目录
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 运行项目

或者使用命令行：

```bash
# 在项目根目录执行
./gradlew assembleDebug

# 生成的APK位于：
# app/build/outputs/apk/debug/app-debug.apk
```

## 使用方法

1. 打开APP，点击"开始计数"
2. 授予相机权限
3. 将相机对准条形码或二维码
4. 点击"拍照识别"按钮
5. 系统会自动识别并累加不重复的码
6. 在"历史记录"中查看之前的计数结果

## 隐私说明

- 所有识别处理完全在本地完成
- 不会上传任何图像或数据到服务器
- 历史记录仅保存在手机本地

## 许可证

本项目仅供学习参考使用。

## 参考

- 原型参考："数钢管"APP的材料计数功能
- ML Kit文档：https://developers.google.com/ml-kit/vision/barcode-scanning
- CameraX文档：https://developer.android.com/training/camerax