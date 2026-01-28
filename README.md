# Kaiqiu Android

这是 [kaiqiu-app-uni](https://github.com/xiaojia001/kaiqiu-app-uni) 项目的 Android 原生实现版本。

## 关于

本项目是将原 uni-app (Vue 3) 项目迁移到 Android 平台的原生应用，采用 **Kotlin + Jetpack Compose** 技术栈开发。

本项目完全使用 MiniMax 2.1 从零开发，包含项目初始化、架构设计、代码编写、调试优化等全部流程。

## 致谢

非常感谢 [kaiqiu-app-uni](https://github.com/xiaojia001/kaiqiu-app-uni) 项目提供的思路和参考，本项目的很多设计、API 对接、页面逻辑都参考了该开源项目。

原项目是一个优秀的乒乓球约球赛事平台，功能丰富，架构清晰。在此特别感谢作者的开源分享。

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **网络**: Retrofit + OkHttp
- **图片**: Coil
- **状态管理**: Compose State + DataStore
- **导航**: Compose Navigation

## 编译

```bash
./gradlew assembleDebug    # Debug 构建
./gradlew assembleRelease  # Release 构建
```

## License

MIT License

Copyright (c) 2024

