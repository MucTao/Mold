# Mold — Kotlin Multiplatform SDK

## SDK 基本信息

| 项目 | 内容 |
| --- | --- |
| 名称 | Mold |
| 用途 | 面向 Android / iOS / Desktop 的 Kotlin Multiplatform 基础 SDK：事件总线、键值存储、网络状态封装、Compose UI 组件库与工具集 |
| 语言 / 平台 | Kotlin 2.4.10 · Kotlin Multiplatform + Compose Multiplatform 1.11.1（Android / iOS / JVM） |
| 包管理器与安装命令 | Gradle（JitPack / Maven），见[安装](#4-安装) |
| 当前版本 | `1.1.22` |
| License | MIT <!-- TODO: 待确认 —— 仓库内暂未发现 LICENSE 文件，发布前请补充并核对许可证文本 --> |
| 目标读者 | 初次接入 Mold 的 Android / KMP 应用工程师（前端 UI、数据层、基建方向） |

---

## 目录

1. [简介与特性](#1-简介与特性)
2. [模块地图](#2-模块地图)
3. [核心概念](#3-核心概念)
4. [环境要求](#4-环境要求兼容性矩阵表格)
5. [安装](#5-安装)
6. [配置说明](#6-配置说明)
7. [快速开始](#7-快速开始含最小可运行示例--预期输出)
8. [错误码表](#8-错误码表)
9. [常见场景示例](#9-常见场景示例)
10. [示例应用](#10-示例应用)
11. [构建与发布](#11-构建与发布)
12. [常见问题 FAQ](#12-常见问题-faq)
13. [版本与变更](#13-版本与变更)
14. [License](#14-license)

---

## 1. 简介与特性

**Mold** 是一套生产导向的 Kotlin Multiplatform 基础 SDK，由 5 个独立发布的核心模块组成，可整体接入或按需引入：

| 模块 | 定位 | 一句话描述 |
| --- | --- | --- |
| [`core/eventBus`](./core/eventBus/README.md) | 事件总线 | 基于 Kotlin Flow 的生产级事件总线（优先级、熔断、缓存、对象池、统计） |
| [`core/dataKV`](./core/dataKV/README.md) | 键值存储 | 类型安全、可过期、跨应用共享的属性委托 KV 存储 |
| [`core/dataRequest`](./core/dataRequest/README.md) | 网络封装 | 统一请求状态模型 + 自动重试 / 可刷新 + cURL 调试插件 |
| [`core/ui`](./core/ui/README.md) | UI 组件库 | Compose Multiplatform 主题体系与通用组件族 |
| [`core/utils`](./core/utils/README.md) | 工具集 | 跨平台时间工具 + Android 系统级工具类 |

特性亮点：

- **多平台一致**：一套 commonMain 代码覆盖 Android / iOS / Desktop；平台差异通过 `expect/actual` 隔离（相机、截图、图片加载、DataStore 路径等）。
- **按需取用**：5 个模块独立坐标、独立发布，可单独依赖任一模块，避免引入无关能力。
- **版本统一**：所有模块共享同一版本号（`1.1.22`），由 `gradle/libs.versions.toml` 统一管理。
- **生产增强**：事件熔断、存储过期、请求自动重试、崩溃捕获等生产级能力开箱即用。
- **调试友好**：`curlLoggerPlugin` 输出可复现的 cURL 命令，配合 `InMemoryHistoryStore` 回溯请求。

---

## 2. 模块地图

```text
Mold（KMP 根工程）
│
├── core/                         ← 可独立发布的 SDK 模块
│   ├── eventBus    org.muc.eventbus      事件总线
│   ├── dataKV      org.muc.datakv        键值存储（DataStore + ContentProvider）
│   ├── dataRequest org.muc.network       网络状态/重试/调试（基于 Ktor）
│   ├── ui          org.muc.ui            Compose UI 组件库
│   └── utils       org.muc.mold.utils    工具类（TimeUtils + Android Utils）
│
├── shared/                        ← 示例共享代码（Compose UI），依赖 eventBus/dataKV/ui/utils
├── androidApp/                    ← Android 示例应用（org.muc.mold）
├── desktopApp/                    ← Desktop(JVM) 示例应用
└── iosApp/                        ← iOS 示例工程（SwiftUI 入口）
```

**依赖关系**：

| 模块 | 对外依赖（核心库） | 内部依赖 |
| --- | --- | --- |
| `eventBus` | compose-runtime、kotlin-logging、lifecycle-runtime-compose、atomicfu | — |
| `dataKV` | DataStore、kotlinx-serialization、kotlinx-datetime、coroutines、atomicfu | — |
| `dataRequest` | Ktor client-core（引擎按平台）、kotlinx-serialization、kotlinx-datetime、compose-ui（ColorSerializer） | — |
| `ui` | Compose Multiplatform（material3/icons）、Navigation3、Coil、material-kolor、CameraX（Android） | — |
| `utils` | kotlinx-datetime、DataStore、lifecycle-runtime、coroutines | — |
| `shared` | Compose Multiplatform | eventBus、dataKV、ui、utils |
| `androidApp` | activity-compose | shared |

> 说明：`shared` 为示例聚合层，未依赖 `dataRequest`；正式接入时可自行组合任意模块。

---

## 3. 核心概念

### 3.1 模块分层设计

Mold 遵循"**数据能力下沉、UI 能力独立、工具横向支撑**"的分层：

- **数据层**：`dataKV`（本地/跨应用存储）+ `dataRequest`（网络状态与重试）—— 负责"数据从哪来、存到哪去"；
- **通信层**：`eventBus` —— 负责模块间 / 页面间解耦通信，配合 `dataKV` 的粘性缓存实现状态恢复；
- **表现层**：`ui` —— 统一主题与组件，消费 `dataRequest` 的 `DataFlowResult` 状态机（Loading/Empty/Fail/Success）；
- **支撑层**：`utils` —— 无业务语义的通用工具，被任意层引用。

### 3.2 跨平台策略

- **commonMain 共享业务逻辑**：状态模型（`DataResult`）、存储委托（`DataKVDelegate`）、时间工具（`TimeUtils`）等全部在 commonMain 实现；
- **expect/actual 隔离平台差异**：`Factory.android/ios/jvm`（存储路径与引擎）、`CameraManager`、`MoldImage`、`CaptureController`、`dynamicColor` 等；
- **平台能力边界**：`dataCross`（ContentProvider 跨应用共享）与 Android 系统工具类仅 Android 可用，iOS / JVM 使用本地能力。

### 3.3 状态驱动 UI 的推荐链路

```text
ViewModel / Repository
   │  request / refreshableRequest（dataRequest）
   ▼
Flow<DataFlowResult<T>>   （Loading → Success / Empty / Fail）
   │  collectAsState
   ▼
Compose UI（ui 组件：LoadingView / EmptyView / ErrorView / 业务列表）
   │  操作事件
   ▼
eventBus.send / subscribe（eventBus）→ 跨页面联动
```

### 3.4 统一异常与容错哲学

- 状态模型（`DataFail` / `DataFlowFail`）携带 `Throwable`，UI 层按类型/文案展示，**不向上抛出**；
- 存储层读取失败回退默认值，写入失败返回原值；
- 时间解析失败返回 `Result<T>`，由调用方决定兜底；
- 唯一建议主动抛出的场景：配置错误（如未初始化 `Utils.app`、`DataContentKVDelegate` 无引擎）。

---

## 4. 环境要求（兼容性矩阵表格）

| 项目 | 要求 |
| --- | --- |
| JDK | 17（JitPack 构建）/ 21（shared jvmToolchain） |
| Gradle | 9.6.1（wrapper；JitPack 亦使用 9.6.1） |
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1（material3 1.11.0-alpha07） |
| Android compileSdk / targetSdk | 37 |
| Android minSdk（应用） | 23（示例应用）/ 21（SDK 模块） |
| AGP | 9.3.1 |
| Ktor | 3.5.2 |
| DataStore | 1.2.1 |
| kotlinx-serialization / datetime | 1.11.0 / 0.8.0 |

**平台支持矩阵**：

| 目标 | Android | iOS（arm64/simArm64） | Desktop（JVM） | Web（Wasm/JS） |
| --- | --- | --- | --- | --- |
| core 各模块 | ✅ | ✅（构建默认启用） | ✅（构建默认启用） | ❌ |
| shared | ✅ | ✅（静态 framework） | ✅ | ❌ |
| 示例应用 | ✅ | ✅（Xcode 运行） | ✅ | ❌ |

> `isOnlyAndroid=true`（gradle.properties）或 `JITPACK=true`（环境变量）时只构建 Android 目标，跳过 iOS/JVM。

---

## 5. 安装

### 5.1 配置仓库

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }          // JitPack 发布
        // 可选：内部 Maven 仓库
        // maven { url = uri("F:/Android/WorkSpace/repo") }
    }
}
```

### 5.2 声明依赖（按需）

```kotlin
dependencies {
    implementation("com.github.MucTao.Mold:eventBus:1.1.22")
    implementation("com.github.MucTao.Mold:dataKV:1.1.22")
    implementation("com.github.MucTao.Mold:dataRequest:1.1.22")
    implementation("com.github.MucTao.Mold:ui:1.1.22")
    implementation("com.github.MucTao.Mold:utils:1.1.22")
}
```

### 5.3 Android 特殊配置（dataKV 跨应用共享）

使用 `dataKV` 的 `dataCross()` 时需在 `AndroidManifest.xml` 注册 Provider：

```xml
<provider
    android:name="org.muc.datakv.ShareProvider"
    android:authorities="org.muc.content.provider.ShareProvider"
    android:exported="true"
    android:process=":MoldShareProvider" />
```

并在 `Application.onCreate()` 中：

```kotlin
DataKV.init(this, authority = "org.muc.content.provider.ShareProvider", table = "MoldCP")
```

---

## 6. 配置说明

### 6.1 统一版本控制（`gradle/libs.versions.toml`）

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `mold-version` | String | `1.1.22` | 全部 core 模块的发布版本，升级 SDK 只改此一处 |
| `groupId` | String | `com.github.MucTao` | 发布 groupId 前缀（实际为 `com.github.MucTao.Mold`） |
| `isOnlyAndroid` | Boolean | `true` | 是否只构建 Android 目标（true 时跳过 iOS/JVM） |
| `android-compileSdk` | Int | `37` | 模块 compileSdk |
| `android-mold-minSdk` | Int | `21` | core 模块 minSdk |
| `android-minSdk` | Int | `23` | 示例应用 minSdk |

### 6.2 模块级配置项

各模块的构造参数 / 初始化配置（默认值）一览，详见各模块 README：

| 模块 | 关键配置（默认值） |
| --- | --- |
| `eventBus` | `Priority.NORMAL`、`usePool=false`、`cacheable=false`、熔断阈值 `5`、缓冲 `1024` |
| `dataKV` | `authority="org.muc.content.provider.ShareProvider"`、`table="MoldCP"`、`NO_EXPIRATION=0L` |
| `dataRequest` | 自动重试 `≤10` 次、退避 `1~5s`、`InMemoryHistoryStore(capacity=99)` |
| `ui` | `seedColor=MoldBlue`、`isDynamic=null`、Toast 对齐 `BiasAlignment(0f,.7f)` |
| `utils` | `Utils.init(app)` 必调；`TimeUtils` 默认系统时区 |

---

## 7. 快速开始（含最小可运行示例 + 预期输出）

以"事件驱动 + KV 存储"的最小组合为例（Android）：

```kotlin
import org.muc.datakv.IDataKVOwner
import org.muc.datakv.datakv
import org.muc.eventbus.event.AppEvent
import org.muc.eventbus.event.core.EventBus
import kotlinx.coroutines.launch

// 1) 定义事件
sealed class AppEvent {
    data class LoginSuccess(val uid: String) : AppEvent()
}

// 2) 定义存储宿主
class SessionStore : IDataKVOwner {
    var uid by datakv("")
}

// 3) 组合使用
class App(private val eventBus: EventBus, private val session: SessionStore) {
    suspend fun login(uid: String) {
        session.uid.setValue { uid }          // 持久化
        eventBus.send(AppEvent.LoginSuccess(uid), cacheable = true)  // 广播
    }

    fun observeLogin(scope: kotlinx.coroutines.CoroutineScope) {
        eventBus.subscribe<AppEvent.LoginSuccess>(scope) { event ->
            println("用户 ${event.uid} 登录成功")
        }
    }
}
```

预期输出：

```text
用户 10086 登录成功
```

完整用法请分别阅读各模块 README：

- [core/eventBus/README.md](./core/eventBus/README.md)
- [core/dataKV/README.md](./core/dataKV/README.md)
- [core/dataRequest/README.md](./core/dataRequest/README.md)
- [core/ui/README.md](./core/ui/README.md)
- [core/utils/README.md](./core/utils/README.md)

---

## 8. 错误码表

**无全局自定义错误码。** 各模块错误约定如下：

| 模块 | 错误约定 |
| --- | --- |
| `eventBus` | `send` 返回 `false`（熔断/异常）；无错误码 |
| `dataKV` | 读取失败回退默认值；未初始化引擎抛 `IllegalStateException`；无错误码 |
| `dataRequest` | `DataFail / DataFlowFail` 携带 `Throwable`；`HttpHistoryEntry.httpCode` 记录 HTTP 状态码；无业务错误码 |
| `ui` | 缺失 `LocalNavigator` 抛 `IllegalStateException("No LocalNavigator found!")`；相机错误走 `onFail` 回调 |
| `utils` | `Utils.app` 反射失败抛 `NullPointerException`；解析失败返回 `Result.failure` |

---

## 9. 常见场景示例

### 9.1 认证：登录态跨模块联动（eventBus + dataKV）

```kotlin
// 登录成功：写存储 + 发事件（缓存）
suspend fun login(uid: String) {
    session.uid.setValue { uid }
    eventBus.send(AppEvent.LoginSuccess(uid), cacheable = true)
}

// 其他模块订阅：UI 刷新、请求携带 Token
eventBus.subscribeSticky<AppEvent.LoginSuccess>(viewModelScope) { goHome() }
```

### 9.2 分页 / 批量：列表页完整链路（dataRequest + ui）

```kotlin
// Repository：可刷新分页流
val ordersFlow = RetryRequestImpl.refreshableRequest(
    request = { api.fetchOrders(page) },
    trigger = trigger,
)

// UI：状态驱动
when (val state = ordersState) {
    is DataFlowLoading -> LoadingView()
    is DataFlowEmpty -> EmptyView("暂无订单")
    is DataFlowFail -> ErrorView(state.error?.message ?: "加载失败", onRetry = { trigger.retry() })
    is DataFlowSuccess -> OrderList(state())
}
```

### 9.3 错误处理：全局崩溃捕获 + 请求失败兜底

```kotlin
// 崩溃上报（utils）
CrashUtils.init(crashDir, onCrashListener = { upload(it.throwable) })

// 请求失败兜底（dataRequest + dataKV）
val result = runCatchingAsDataResult { api.fetchConfig() }
result
    .onFail { useLocalCache() }   // 网络失败读缓存
    .onSuccess { saveLocalCache(it) }
```

---

## 10. 示例应用

仓库包含三个示例应用，用于验证与演示 SDK：

### 10.1 Android App（`androidApp`）

```bash
./gradlew :androidApp:assembleDebug
```

- 入口：`org.muc.mold.MainActivity`
- 演示：`shared` 共享 UI、`dataKV` 跨应用 Provider（`ShareProvider` 注册于 Manifest）

### 10.2 Desktop App（`desktopApp`）

```bash
# 标准运行
./gradlew :desktopApp:run

# 热重载
./gradlew :desktopApp:hotRun --auto
```

- 入口：`org.muc.mold.main.kt`

### 10.3 iOS App（`iosApp`）

用 Xcode 打开 `iosApp` 目录运行；`shared` 构建为静态 framework（`baseName = "Shared"`）。

---

## 11. 构建与发布

### 11.1 本地构建

```bash
./gradlew :core:eventBus:build
./gradlew :core:dataKV:build
./gradlew :core:dataRequest:build
./gradlew :core:ui:build
./gradlew :core:utils:build
```

### 11.2 发布

**JitPack**（CI）：根目录 `jitpack.yml` 已配置 JDK 17 + Gradle 9.6.1，执行：

```bash
./gradlew clean publishToMavenLocal -no-daemon --refresh-dependencies -x test -x lint
```

**本地 Maven 仓库**：各模块 `publishing.repositories.maven` 指向 `F:/Android/WorkSpace/repo`，执行：

```bash
./gradlew publishAllCore
```

该任务聚合所有 `maven-publish` 子模块的 `publishAllPublicationsToMavenRepository`。

**发布坐标总览**：

| 模块 | groupId | artifactId | version |
| --- | --- | --- | --- |
| eventBus | `com.github.MucTao.Mold` | `eventBus` | `1.1.22` |
| dataKV | `com.github.MucTao.Mold` | `dataKV` | `1.1.22` |
| dataRequest | `com.github.MucTao.Mold` | `dataRequest` | `1.1.22` |
| ui | `com.github.MucTao.Mold` | `ui` | `1.1.22` |
| utils | `com.github.MucTao.Mold` | `utils` | `1.1.22` |

> 说明：`groupId` 由 `libs.versions.toml` 的 `groupId = "com.github.MucTao"` 拼 `".Mold"` 构成；版本由 `mold-version = "1.1.22"` 统一控制。

---

## 12. 常见问题 FAQ

**Q1：只想用其中一个模块，需要引入整个 SDK 吗？**

不需要。5 个模块是独立 Maven 坐标，按需声明依赖即可，例如只需事件总线就只加 `com.github.MucTao.Mold:eventBus:1.1.22`。

**Q2：iOS / 桌面目标如何启用或关闭？**

默认构建 iOS（arm64 + simulatorArm64）与 JVM 目标。设置 `gradle.properties` 中 `isOnlyAndroid=true`，或在 CI 环境变量设 `JITPACK=true`，则只构建 Android 目标。

**Q3：`dataCross` 在 iOS / 桌面能用吗？**

不能。跨应用共享依赖 Android `ContentProvider`，仅 Android 可用；iOS / JVM 上 `datakv()`（本地 DataStore）正常可用。

**Q4：版本号在哪里统一修改？**

`gradle/libs.versions.toml` 的 `mold-version`。所有模块的 `publishing` 与依赖声明都引用它，修改后重新发布即可全局生效。

**Q5：示例应用与 SDK 的关系？**

`shared` / `androidApp` / `desktopApp` / `iosApp` 是验证 SDK 的示例层，不作为可发布 SDK 的一部分。正式接入请依赖 `core/*` 各模块坐标。

**Q6：如何获取各模块的详细 API？**

每个模块根目录都有完整 README：`core/eventBus/README.md`、`core/dataKV/README.md`、`core/dataRequest/README.md`、`core/ui/README.md`、`core/utils/README.md`（含 API 参考、配置、FAQ）。

---

## 13. 版本与变更

| 版本 | 说明 |
| --- | --- |
| `1.1.22` | 当前版本（5 个 core 模块统一版本） |

<!-- TODO: 待确认 —— 仓库中未发现 CHANGELOG，历史版本与变更记录请补充 -->

---

## 14. License

本项目基于 **MIT License** 发布。

<!-- TODO: 待确认 —— 仓库内暂未包含 LICENSE 文件，发布前请补充 LICENSE 文件并核对版权归属。 -->
