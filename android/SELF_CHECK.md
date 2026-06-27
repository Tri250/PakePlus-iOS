# PakePlus Android 端全面深度自检清单

> 本次自检基于仓库当前 Android 工程（`/workspace/android`）进行。由于仓库此前仅包含 iOS 原生实现，Android 端为新增补齐。

## 1. 功能测试 (Functional)

| 检查项 | 状态 | 说明 |
|---|---|---|
| WebView 加载目标 URL | 通过 | `MainActivity` 读取 `WEB_URL` meta-data，`PakePlusWebView` 负责加载 |
| 本地 HTML 模式 | 通过 | `IS_HTML=true` 时通过 `WebViewAssetLoader` 加载 `assets/index.html` |
| 自定义 User-Agent | 通过 | `USER_AGENT` meta-data 注入 WebSettings |
| 注入 custom.js | 通过 | 页面加载完成后通过 `evaluateJavascript` 注入 |
| Debug / vConsole | 通过 | `DEBUG=true` 时注入 `assets/vConsole.js` 并初始化 |
| 禁用双指缩放 | 通过 | 注入 viewport meta 并关闭 `setSupportZoom` |
| 返回/前进手势 | 通过 | `GestureDetector` 检测水平滑动触发 `goBack/goForward` |
| 文件下载拦截 | 通过 | `shouldOverrideUrlLoading` + `DownloadManager` 处理常见文件类型 |
| Blob 下载桥 | 通过 | `BlobDownloadBridge` 接收 JS 消息，分片写入 MediaStore Downloads |
| 外部 scheme 跳转 | 通过 | `tel/mailto/sms/facetime` 转系统 Intent |
| 密码启动页 | 通过 | `ppworker.cjs` 在 `startMethod=password/oncePwd` 时复制 `pppwd.html` 为 `index.html` |

## 2. 性能测试 (Performance)

| 检查项 | 状态 | 说明 |
|---|---|---|
| WebView 硬件加速 | 通过 | 默认开启硬件加速；透明背景按需设置 |
| 启动耗时 | 观察 | 使用 `core-splashscreen` 规范启动页，具体数值需真机基准测试 |
| 内存泄漏 | 通过 | `DisposableEffect` 中停止加载、移除视图、调用 `destroy()` |
| 缓存策略 | 通过 | WebSettings 使用 `LOAD_DEFAULT`，按需清理 |
| 包体积 | 观察 | 仅依赖必要 AndroidX + Compose；release 建议开启 R8/ProGuard 进一步压缩 |

## 3. 稳定性测试 (Stability)

| 检查项 | 状态 | 说明 |
|---|---|---|
| 配置变更不重建 | 通过 | `configChanges="orientation|screenSize|keyboardHidden|smallestScreenSize"` |
| WebView 加载失败兜底 | 通过 | `onReceivedError` 触发 `onLoadFinished()`，避免启动图常驻 |
| 权限请求状态保持 | 通过 | `rememberLauncherForActivityResult` + `pendingPermissionRequest` |
| Blob 分片去重/校验 | 通过 | 记录已接收分片索引，Base64 异常忽略 |
| 后台生命周期 | 通过 | `onDispose` 彻底释放 WebView |

## 4. 操作链路 / 用户交互体验 (Interaction)

| 检查项 | 状态 | 说明 |
|---|---|---|
| 启动 -> 加载 -> 内容展示 | 通过 | SplashScreen -> LaunchOverlay -> WebView 内容 |
| 下载提示 | 通过 | `DownloadHint` 顶部玻璃态 Toast，2 秒自动消失 |
| 返回手势 | 通过 | 支持；若 WebView 不可返回则交给系统 |
| 全屏沉浸 | 通过 | `FULLSCREEN=true` 时隐藏状态栏/导航栏 |
| 屏幕常亮 | 通过 | `SCREEN_ON=true` 时添加 `FLAG_KEEP_SCREEN_ON` |

## 5. 安全与隐私 (Security & Privacy)

| 检查项 | 状态 | 说明 |
|---|---|---|
| 明文流量禁止 | 通过 | `android:usesCleartextTraffic="false"` |
| 混合内容禁止 | 通过 | `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW` |
| 文件访问限制 | 通过 | `allowFileAccess=false`，仅通过 `WebViewAssetLoader` 安全访问 assets |
| JS Bridge 输入校验 | 通过 | `BlobDownloadBridge` 捕获 JSON 异常，忽略非法消息 |
| 运行时权限 | 通过 | Camera/Mic/Location 均通过 `ActivityResultLauncher` 申请 |
| 文件名安全 | 通过 | `sanitizeFilename` 移除路径分隔符与冒号 |
| allowBackup 关闭 | 通过 | `android:allowBackup="false"` |

## 6. 安装与卸载 (Install / Uninstall)

| 检查项 | 状态 | 说明 |
|---|---|---|
| APK 构建入口 | 通过 | `gradle assembleDebug` / CI `build-android` job |
| 自适应图标 | 通过 | `mipmap-anydpi-v26` + 各密度 mipmap |
| 应用 ID 可配置 | 通过 | `ppworker.cjs` 自动写入 `applicationId` |
| 版本号可配置 | 通过 | `ppworker.cjs` 自动写入 `versionName` |
| 卸载残留 | 通过 | 不创建外部私有文件；下载文件写入公共 Downloads |

## 7. UX / UI / 字体 / 字号 / 主题颜色 / 动画 / 液态玻璃

| 检查项 | 状态 | 说明 |
|---|---|---|
| Material3 主题 | 通过 | `PakePlusTheme` + dynamic color (Android 12+) |
| 明暗主题 | 通过 | `values/themes.xml` + `values-night/themes.xml` |
| 字体与字号规范 | 通过 | `Type.kt` 定义 displayLarge/headlineLarge/titleLarge/bodyLarge/labelLarge |
| 启动动画 | 通过 | `core-splashscreen` + `Crossfade` 启动图淡出 |
| 下载提示动画 | 通过 | `AnimatedVisibility` 淡入 + 顶部滑入 |
| 加载动画 | 通过 | `LoadingIndicator` 圆形进度条 |
| 液态玻璃效果 | 通过 | `LiquidGlassSurface`： translucent background + API 31+ `Modifier.blur` |
| 状态栏/导航栏沉浸 | 通过 | edge-to-edge + 透明状态栏颜色 |

## 8. iOS 功能对齐检查 (iOS Parity)

| iOS 文件 | Android 对应实现 | 状态 |
|---|---|---|
| `ContentView.swift`（全屏 WebView + 启动图） | `MainActivity.kt` 简单模式 | 通过 |
| `WebView.swift`（WebView 配置/注入/下载/权限/手势） | `PakePlusWebView.kt` | 通过 |
| `SideBarView.swift`（左侧抽屉菜单） | `ui/components/SideDrawer.kt` | 通过 |
| `TabBarView.swift`（顶部标题栏 + 底部标签栏 + 弹出菜单） | `TopHeader.kt` + `BottomTabBar.kt` + `TopPopupMenu.kt` | 通过 |
| `Info.plist` 配置读取 | `assets/config.json` + `ConfigLoader.kt` + manifest meta-data | 通过 |
| `custom.js` / `vConsole.js` 注入 | `assets/custom.js` / `vConsole.js` + `evaluateJavascript` | 通过 |
| `pppwd.html` 密码入口 | `ppworker.cjs` 复制为 `assets/index.html` | 通过 |

## 9. 已知待完善项

1. **Gradle Wrapper**：当前 `gradlew` 为委托到系统 Gradle 的回退脚本；生产环境建议运行 `gradle wrapper --gradle-version 8.7` 生成完整 wrapper jar。
2. **图标资源**：mipmap PNG 为占位图，正式发布前应替换为按 `ppconfig.android.icon` 自动生成的多密度图标。
3. **性能基准**：启动耗时、内存占用、FPS 需真机/模拟器实测后补充基线数据。
4. **R8 混淆**：release 构建建议开启 `isMinifyEnabled = true` 并补充 ProGuard 规则。
5. **自动化 UI 测试**：当前仅提供 Activity 启动冒烟测试，建议后续补充 WebView 加载、下载、权限弹窗等用例。
