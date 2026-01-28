# GitHub Actions CI/CD 配置说明

## 触发时机（业界通常做法）

| 触发条件 | 构建类型 | 产物 |
|---------|---------|------|
| Push 到 `main` 分支 | Development Build | Debug APK (保留 7 天) |
| Push 标签 `v*` (如 v1.0.0) | Version Build | Debug + Release APK + 自动创建 Release |
| 发布 Release | Production Build | Debug + Release APK + 更新 Release |

## Secrets 配置

在 GitHub 仓库 Settings → Secrets and variables → Actions 中添加以下 Secrets：

### 1. 签名密钥 (Required for Release Build)

```bash
# 生成 Base64 编码的 keystore
base64 app/kaiqiu.keystore | tr -d '\n'
```

添加以下 4 个 Secrets：

| Secret Name | Value | 说明 |
|-------------|-------|------|
| `KEYSTORE_BASE64` | keystore 文件的 Base64 编码 | 签名密钥文件 |
| `KEYSTORE_PASSWORD` | `kaiqiu2024` | 密钥库密码 |
| `KEY_ALIAS` | `kaiqiu` | 密钥别名 |
| `KEY_PASSWORD` | `kaiqiu2024` | 密钥密码 |

### 2. 生成步骤

```bash
# 1. 进入项目目录
cd /path/to/Kaiqiu

# 2. 生成 Base64 编码
base64 app/kaiqiu.keystore | tr -d '\n' > keystore_base64.txt

# 3. 查看并复制内容
cat keystore_base64.txt

# 4. 在 GitHub 中添加 KEYSTORE_BASE64 Secret
```

## GitHub Actions 功能

### 工作流 `.github/workflows/build-sign-release.yml` 实现：

1. **代码检出** - 检出项目代码
2. **环境设置** - Java 17 + Android SDK API 36
3. **版本检测** - 自动从 build.gradle.kts 读取版本信息
4. **Changelog 生成** - 自动从上一次 tag 到现在生成更新日志
5. **密钥还原** - 从 Secrets 还原签名密钥
6. **Debug 构建** - 生成调试版 APK
7. **Release 构建** - 生成签名版 APK
8. **自动发布** - 创建 GitHub Release 并上传 APK

## 版本号规范

遵循 [语义化版本](https://semver.org/)：

```
vMAJOR.MINOR.PATCH
如: v1.0.0, v1.0.1, v2.1.0
```

## 本地测试

```bash
# 构建 Debug
./gradlew assembleDebug

# 构建 Release (需要签名配置)
./gradlew assembleRelease

# 清理构建
./gradlew clean
```

## 注意事项

1. **首次发布**：如果没有上一个 tag，changelog 将从首次提交开始
2. **密钥安全**：不要将 keystore 文件提交到版本控制
3. **权限确认**：确保 GitHub Actions 有权限创建 Release
4. **Tag 格式**：只有 `v*` 格式的 tag 才会触发 Release 构建
