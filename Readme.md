# WaterWater 🐱💧

一个可爱又有个性的喝水提醒应用，让猫咪陪你一起养成喝水好习惯！

## ✨ 特色功能

### 🐈 可爱的猫咪陪伴
- **互动猫咪**: 界面上有可拖拽的猫咪，会根据你的提醒设置显示不同心情
- **多种心情**: 开心😸、困困😴、饿了🍖、想玩😺
- **个性定制**: 可以调整猫咪的位置和大小

### 📜 吉卜力风格设计
- **卷轴界面**: 古风卷轴样式的提醒列表
- **温暖配色**: 采用吉卜力动画风格的棕色系配色
- **流畅动画**: 平滑的展开/收起动画效果

### ⏰ 智能提醒系统
- **灵活重复**: 支持分钟、小时、天、周、月等重复模式
- **时间段控制**: 可以设置提醒的活跃时段
- **精确闹钟**: 使用系统闹钟确保提醒准时到达

### 🔒 秘密课桌
- **隐藏功能**: 点击可拖拽的课桌图标查看隐藏内容
- **隐私保护**: 存放私密的提醒事项

## 🚀 快速开始

### 系统要求
- **Android版本**: API 24+ (Android 7.0)
- **编译版本**: API 36 (Android 16)
- **Java版本**: 11

### 构建项目

1. **克隆项目**
   ```bash
   git clone [repository-url]
   cd WaterWater
   ```

2. **配置环境**
   - 确保已安装 Android Studio
   - 配置好 Android SDK (API 24+)

3. **构建APK**
   ```bash
   ./gradlew assembleRelease
   ```
   构建好的APK位于 `app/release/app-release.apk`

### 安装使用

1. 将 `app/release/app-release.apk` 安装到手机
2. 首次运行时授予通知权限
3. 开始添加你的第一个喝水提醒！

## 🛠️ 技术架构

### 核心技术栈
- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material Design 3
- **数据库**: Room
- **云存储**: LeanCloud
- **动画**: Lottie
- **架构模式**: MVVM

### 项目结构
```
app/src/main/java/com/example/waterwater/
├── alarm/           # 闹钟调度相关
├── data/            # 数据层 (数据库、仓库)
├── model/           # 数据模型
├── ui/              # UI层
│   ├── components/  # 可复用组件
│   ├── screens/     # 屏幕页面
│   └── theme/       # 主题配置
├── utils/           # 工具类
└── viewmodel/       # ViewModel层
```

### 主要依赖
- **AndroidX**: 核心Android组件
- **Compose BOM**: 2024.09.00
- **Room**: 2.6.1 (数据库)
- **LeanCloud**: 8.2.19 (云存储)
- **Lottie**: 6.3.0 (动画)
- **Kotlin Coroutines**: 1.7.3 (异步处理)

## 📱 使用指南

### 添加提醒
1. 点击右下角的 "+" 按钮
2. 设置提醒标题和描述
3. 选择提醒时间
4. 配置重复模式和时间段
5. 选择猫咪的心情
6. 保存提醒

### 管理提醒
- **滑动卡片**: 查看提醒详情
- **开关切换**: 启用/禁用提醒
- **长按编辑**: 修改提醒设置
- **滑动删除**: 删除不需要的提醒

### 互动玩法
- **拖拽猫咪**: 移动猫咪到喜欢的位置
- **点击课桌**: 发现隐藏的秘密功能
- **滚动卷轴**: 浏览你的提醒列表

## 🔧 开发说明

### 开发环境设置
1. 使用 Android Studio 最新稳定版
2. 配置 Kotlin 插件
3. 启用 Compose 预览功能

### 调试技巧
- 使用 Compose 预览快速查看UI
- 利用 Android Studio 的 Layout Inspector
- 查看设备日志了解闹钟触发情况

### 自定义扩展
- **添加新猫咪**: 在 `CatInstance` 中添加新品种
- **自定义主题**: 修改 `ui/theme/` 下的配色
- **扩展提醒类型**: 在 `RepeatType` 枚举中添加新类型

## 📄 许可证

本项目仅供学习和个人使用，请遵守相关开源协议。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 🙏 致谢

感谢所有为开源社区做出贡献的开发者们，特别是：
- Jetpack Compose 团队
- Material Design 团队
- LeanCloud 平台

---

**保持 hydrated, 让猫咪陪你一起喝水！** 🐱💧