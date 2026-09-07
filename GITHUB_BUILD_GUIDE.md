# GitHub上传与构建指南

本文档为PC端Agent提供详细的项目上传和构建说明。

## 项目状态

✅ **已完成**：
- 项目结构完整
- Gradle配置文件
- 核心Kotlin代码（MainActivity、CameraActivity、HistoryActivity）
- 布局文件（4个XML）
- 基础资源文件（strings、colors、themes）

⚠️ **需要补充**：
1. 部分字符串资源（strings.xml中缺失）
2. 启动图标PNG文件（mipmap目录）
3. Gradle Wrapper文件

---

## 第一步：补充缺失的字符串资源

在 `app/src/main/res/values/strings.xml` 中添加以下缺失的字符串：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">材料计数器</string>
    <string name="start_count">开始计数</string>
    <string name="history">历史记录</string>
    <string name="take_photo">拍照识别</string>
    <string name="count_result">识别结果</string>
    <string name="save">保存</string>
    <string name="cancel">取消</string>
    <string name="no_history">暂无记录</string>
    <string name="permission_camera_denied">需要相机权限才能拍照</string>
    <string name="scanning">识别中...</string>
    <string name="capture_hint">点击拍照按钮开始识别</string>
    
    <!-- 新增缺失的字符串 -->
    <string name="tap_to_scan">点击下方按钮开始扫描</string>
    <string name="save_record">保存记录</string>
</resources>
```

**操作命令**：
```bash
cat > app/src/main/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">材料计数器</string>
    <string name="start_count">开始计数</string>
    <string name="history">历史记录</string>
    <string name="take_photo">拍照识别</string>
    <string name="count_result">识别结果</string>
    <string name="save">保存</string>
    <string name="cancel">取消</string>
    <string name="no_history">暂无记录</string>
    <string name="permission_camera_denied">需要相机权限才能拍照</string>
    <string name="scanning">识别中...</string>
    <string name="capture_hint">点击拍照按钮开始识别</string>
    <string name="tap_to_scan">点击下方按钮开始扫描</string>
    <string name="save_record">保存记录</string>
</resources>
EOF
```

---

## 第二步：生成启动图标

### 方案A：使用Android Studio（推荐）

1. 在Android Studio中打开项目
2. 右键点击 `app/src/main/res` → `New` → `Image Asset`
3. 选择 `Launcher Icons (Adaptive and Legacy)`
4. 使用前景图标：`res/drawable/ic_launcher_foreground.xml`
5. 背景颜色：`#2196F3`
6. 点击 `Next` → `Finish`，自动生成所有分辨率的图标

### 方案B：使用在线工具生成

访问 https://icon.kitchen/ 或 https://romannurik.github.io/AndroidAssetStudio/

上传图标或使用Material图标，选择Camera图标，下载后解压到对应目录：
```
app/src/main/res/
├── mipmap-mdpi/ic_launcher.png (48x48)
├── mipmap-hdpi/ic_launcher.png (72x72)
├── mipmap-xhdpi/ic_launcher.png (96x96)
├── mipmap-xxhdpi/ic_launcher.png (144x144)
└── mipmap-xxxhdpi/ic_launcher.png (192x192)
```

### 方案C：使用占位图标（快速测试）

如果仅用于测试，可以暂时移除自适应图标配置：

在 `AndroidManifest.xml` 中修改：
```xml
<!-- 将 -->
android:icon="@mipmap/ic_launcher"
<!-- 改为 -->
android:icon="@drawable/ic_launcher_foreground"
```

---

## 第三步：添加Gradle Wrapper

在项目根目录执行：

```bash
# 如果已有gradle，直接生成wrapper
gradle wrapper --gradle-version 8.2

# 或者从其他Android项目复制以下文件：
# gradle/wrapper/gradle-wrapper.jar
# gradle/wrapper/gradle-wrapper.properties
# gradlew
# gradlew.bat
```

**gradle-wrapper.properties 内容示例**：
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

## 第四步：创建.gitignore文件

在项目根目录创建 `.gitignore`：

```gitignore
# Android Studio
*.iml
.gradle
.idea/
local.properties
.DS_Store

# Build outputs
/build
/app/build
/captures
.externalNativeBuild
.cxx

# Gradle
.gradle/
gradle-app.setting
!gradle-wrapper.jar
!gradle-wrapper.properties

# APK files
*.apk
*.aab
*.ap_
*.dex

# Keystore files
*.jks
*.keystore

# Log Files
*.log

# Android Profiling
*.hprof
```

**操作命令**：
```bash
cat > .gitignore << 'EOF'
*.iml
.gradle
.idea/
local.properties
.DS_Store
/build
/app/build
/captures
.externalNativeBuild
.cxx
*.apk
*.aab
*.ap_
*.dex
*.jks
*.keystore
*.log
*.hprof
EOF
```

---

## 第五步：上传到GitHub

```bash
# 1. 初始化Git仓库
cd MaterialCounter
git init

# 2. 添加所有文件
git add .

# 3. 首次提交
git commit -m "Initial commit: Material Counter app with ML Kit barcode scanning"

# 4. 添加远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/YOUR_USERNAME/MaterialCounter.git

# 5. 推送到GitHub
git branch -M main
git push -u origin main
```

---

## 第六步：配置GitHub Actions自动构建（可选）

在项目根目录创建 `.github/workflows/android-build.yml`：

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

**创建命令**：
```bash
mkdir -p .github/workflows
cat > .github/workflows/android-build.yml << 'EOF'
# (复制上面的YAML内容)
EOF
```

---

## 第七步：本地构建测试

### 使用命令行构建：

```bash
# 1. 清理项目
./gradlew clean

# 2. 构建Debug版本
./gradlew assembleDebug

# 3. 生成的APK位置
# app/build/outputs/apk/debug/app-debug.apk

# 4. 构建Release版本（需要签名配置）
./gradlew assembleRelease
```

### 使用Android Studio：

1. 打开项目：`File` → `Open` → 选择 `MaterialCounter` 目录
2. 等待Gradle同步完成
3. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
4. 构建完成后会显示APK位置链接

---

## 第八步：签名配置（生成正式版APK）

### 1. 生成密钥库

```bash
keytool -genkey -v -keystore material-counter.keystore \
  -alias material_counter \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### 2. 配置签名

在 `app/build.gradle` 中添加：

```gradle
android {
    ...
    signingConfigs {
        release {
            storeFile file("../material-counter.keystore")
            storePassword "your_password"
            keyAlias "material_counter"
            keyPassword "your_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. 构建签名版APK

```bash
./gradlew assembleRelease
# 输出：app/build/outputs/apk/release/app-release.apk
```

---

## 常见问题排查

### 问题1：Gradle同步失败

**解决方案**：
```bash
# 清理Gradle缓存
rm -rf ~/.gradle/caches/
./gradlew clean --refresh-dependencies
```

### 问题2：SDK版本不匹配

检查本地SDK版本：
```bash
# 查看已安装的SDK
$ANDROID_HOME/tools/bin/sdkmanager --list
```

安装所需SDK：
```bash
$ANDROID_HOME/tools/bin/sdkmanager "platforms;android-34"
$ANDROID_HOME/tools/bin/sdkmanager "build-tools;34.0.0"
```

### 问题3：CameraX或ML Kit依赖下载失败

在项目根目录 `build.gradle` 中添加国内镜像：
```gradle
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### 问题4：activity_camera.xml引用不存在的资源

确保strings.xml包含所有引用的字符串，参考第一步补充。

---

## 快速验证清单

在上传GitHub前，执行以下检查：

- [ ] strings.xml包含所有必需的字符串
- [ ] 启动图标已生成或配置占位方案
- [ ] Gradle Wrapper文件存在
- [ ] .gitignore已创建
- [ ] 本地构建成功 (`./gradlew assembleDebug`)
- [ ] APK可以安装到设备并运行
- [ ] 相机权限正常请求
- [ ] 条形码识别功能正常
- [ ] 历史记录保存和显示正常

---

## 完整构建脚本

将以下脚本保存为 `prepare_for_github.sh`，一键完成所有准备工作：

```bash
#!/bin/bash

echo "=== MaterialCounter GitHub准备脚本 ==="

# 1. 补充strings.xml
echo "Step 1: 更新 strings.xml..."
cat > app/src/main/res/values/strings.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">材料计数器</string>
    <string name="start_count">开始计数</string>
    <string name="history">历史记录</string>
    <string name="take_photo">拍照识别</string>
    <string name="count_result">识别结果</string>
    <string name="save">保存</string>
    <string name="cancel">取消</string>
    <string name="no_history">暂无记录</string>
    <string name="permission_camera_denied">需要相机权限才能拍照</string>
    <string name="scanning">识别中...</string>
    <string name="capture_hint">点击拍照按钮开始识别</string>
    <string name="tap_to_scan">点击下方按钮开始扫描</string>
    <string name="save_record">保存记录</string>
</resources>
EOF

# 2. 创建.gitignore
echo "Step 2: 创建 .gitignore..."
cat > .gitignore << 'EOF'
*.iml
.gradle
.idea/
local.properties
.DS_Store
/build
/app/build
/captures
.externalNativeBuild
.cxx
*.apk
*.aab
*.ap_
*.dex
*.jks
*.keystore
*.log
*.hprof
EOF

# 3. 创建GitHub Actions配置
echo "Step 3: 创建 GitHub Actions..."
mkdir -p .github/workflows
cat > .github/workflows/android-build.yml << 'EOF'
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
EOF

echo "✅ 准备完成！"
echo ""
echo "接下来执行："
echo "  1. 生成启动图标（使用Android Studio或在线工具）"
echo "  2. 添加Gradle Wrapper：gradle wrapper"
echo "  3. 初始化Git：git init"
echo "  4. 提交代码：git add . && git commit -m 'Initial commit'"
echo "  5. 推送到GitHub：git remote add origin <YOUR_REPO_URL> && git push -u origin main"
```

---

## 联系与支持

- **项目位置**：`/sdcard/Download/MaterialCounter/`
- **技术栈**：Kotlin + CameraX + ML Kit Barcode Scanning
- **参考文档**：
  - [CameraX官方文档](https://developer.android.com/training/camerax)
  - [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
  - [Android Gradle Plugin文档](https://developer.android.com/studio/build)

---

**最后检查**：确保完成第一步（补充strings.xml）和第二步（生成图标），这两项是构建成功的必要条件！