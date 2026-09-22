# Mold DataRequest

## SDK 基本信息

| 项目 | 内容 |
| --- | --- |
| 名称 | Mold DataRequest（`dataRequest`） |
| 用途 | 网络请求状态封装与重试 SDK：以 `DataResult` / `DataFlowResult` 统一成功/空/失败状态，内置自动重试、下拉刷新触发器与 cURL 调试插件 |
| 语言 / 平台 | Kotlin Multiplatform（Android / iOS / Desktop-JVM）+ Compose |
| 包管理器与安装命令 | Gradle（Maven / JitPack），详见[安装](#3-安装) |
| 当前版本 | `1.1.22` |
| License | MIT <!-- TODO: 待确认 —— 仓库内暂未发现 LICENSE 文件，发布前请补充并核对许可证文本 --> |
| 目标读者 | 初次接入网络请求封装、需要状态模型与重试机制的 KMP 应用开发者 / 后端对接工程师 |

> 坐标：`com.github.MucTao.Mold:dataRequest:1.1.22`

---

## 目录

1. [简介与特性](#1-简介与特性)
2. [环境要求](#2-环境要求兼容性矩阵表格)
3. [安装](#3-安装)
4. [快速开始](#4-快速开始含最小可运行示例--预期输出)
5. [核心概念](#5-核心概念)
6. [配置说明](#6-配置说明表格字段类型必填默认值说明)
7. [API 参考](#7-api-参考)
8. [错误码表](#8-错误码表)
9. [常见场景示例](#9-常见场景示例至少-3-个认证分页批量错误处理)
10. [常见问题 FAQ](#10-常见问题-faq至少-5-条)
11. [版本与变更](#11-版本与变更)
12. [License](#12-license)

---

## 1. 简介与特性

**Mold DataRequest** 不替代 Ktor Client 本身，而是围绕"请求 → 状态 → 重试 → 调试"提供一套**与 Ktor 协作的封装层**：统一的响应状态模型、声明式重试、可响应式刷新的触发器，以及一行接入的 cURL 调试插件。

主要特性：

- **统一状态模型**：
  - `DataResult<T>`：一次性结果（`DataSuccess` / `DataEmpty` / `DataFail`）；
  - `DataFlowResult<T>`：流式状态（`Uninitialized` / `DataFlowLoading` / `DataFlowSuccess` / `DataFlowEmpty` / `DataFlowFail`）。
- **空值语义化**：`null` / 空列表自动映射为 `DataEmpty`，UI 层无需再判空。
- **自动重试**：`request {}` 默认对非序列化异常自动重试（最多 10 次，随机退避 1~5s）。
- **响应式重试（Trigger）**：`Trigger` 触发流 + `refreshableRequest`，下拉刷新 / 点击重试只需调用 `trigger.retry()`。
- **cURL 调试插件**：Ktor `ClientPlugin`，可打印/记录每个请求的等效 curl 命令、状态码、耗时、响应体摘要，内置内存历史存储（`HistoryStore`）。
- **自定义 Serializer 全家桶**：布尔（0/1、Y/N）、Compose `Color`（HEX）、`LocalDate` / `LocalDateTime`（字符串与时间戳两种格式）。
- **Ktor 3.x 原生适配**：基于 `io.ktor:ktor-client-core` 3.5.2 构建。

---

## 2. 环境要求（兼容性矩阵表格）

| 项目 | 要求 |
| --- | --- |
| Kotlin | ≥ 2.4.10 |
| Android compileSdk | 37 |
| Android minSdk | 21 |
| Ktor | 3.5.2（client-core；引擎：Android OkHttp / JVM CIO / iOS Darwin） |
| kotlinx-serialization | 1.11.0 |
| kotlinx-datetime | 0.8.0 |
| Compose UI | 1.11.1（`ColorSerializer` / `DataFlowResult` 依赖 Compose UI 可选） |
| 协程 | kotlinx-coroutines 1.11.0 |

**平台目标矩阵**：

| 平台 | 支持 | 引擎依赖 |
| --- | --- | --- |
| Android | ✅ | `ktor-client-okhttp` |
| iOS（iosArm64 / iosSimulatorArm64） | ✅ | `ktor-client-darwin` |
| Desktop（JVM） | ✅ | `ktor-client-cio` |
| Web（Wasm/JS） | ❌ | 当前未配置 |

> 引擎依赖已由模块传递提供，使用方无需重复声明；若需要显式指定引擎请自行添加对应 `ktor-client-*` 依赖。

---

## 3. 安装

### 3.1 配置仓库

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}
```

### 3.2 声明依赖

```kotlin
dependencies {
    implementation("com.github.MucTao.Mold:dataRequest:1.1.22")
}
```

### 3.3 本地仓库（可选）

```kotlin
repositories {
    maven { url = uri("F:/Android/WorkSpace/repo") }
}
```

> 提示：模块传递依赖 `ktor-client-core` 与各平台引擎，请确保你的 Ktor 版本与其兼容（3.5.x）。

---

## 4. 快速开始（含最小可运行示例 + 预期输出）

### 4.1 一次性请求（DataResult）

```kotlin
import org.muc.network.DataResult
import org.muc.network.runCatchingAsDataResult
import org.muc.network.onSuccess
import org.muc.network.onFail

data class User(val id: Int, val name: String)

// 模拟请求：返回 User 或抛异常
fun fetchUser(): User {
    return User(1, "Alice") // 可替换为真实网络调用
}

// 包装为 DataResult
val result: DataResult<User> = runCatchingAsDataResult { fetchUser() }

// 消费
result
    .onSuccess { user -> println("用户: ${user.name}") }
    .onFail { cause -> println("失败: ${cause?.message}") }
```

### 4.2 流式请求 + 重试（DataFlowResult + Trigger）

```kotlin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect
import org.muc.network.retry.RetryRequestImpl
import org.muc.network.retry.Trigger
import org.muc.network.status.DataFlowResult
import org.muc.network.status.DataFlowSuccess
import org.muc.network.status.DataFlowFail
import org.muc.network.status.DataFlowLoading

// 1) 创建触发器（自动触发首次请求）
val trigger = Trigger()

// 2) 请求函数：返回 Flow<T>
suspend fun requestUsers(): Flow<List<User>> = flowOf(listOf(User(1, "Alice")))

// 3) 组装可刷新请求
val usersFlow: Flow<DataFlowResult<List<User>>> = RetryRequestImpl.refreshableRequest(
    request = { requestUsers() },
    trigger = trigger,
)

// 4) 消费：自动经历 Loading → Success/Fail
suspend fun collect() {
    usersFlow.collect { state ->
        when (state) {
            is DataFlowLoading -> println("加载中...")
            is DataFlowSuccess -> println("成功: ${state()}")
            is DataFlowFail -> println("失败: ${state.error?.message}")
            else -> {}
        }
    }
}

// 5) 需要重试/刷新时：
trigger.retry()
```

### 4.3 预期输出

```text
加载中...
成功: [User(id=1, name=Alice)]
```

---

## 5. 核心概念

### 5.1 状态模型

#### `DataResult<T>`（一次性结果）

```text
sealed class DataResult<out T>(
    val shouldLoad: Boolean,   // 是否需要展示加载态
    open val trigger: Trigger?,
    private val value: T?,
)
```

| 子类 | `shouldLoad` | 说明 |
| --- | --- | --- |
| `DataSuccess<T>(value)` | `false` | 有数据，`invoke()` 返回强类型值 |
| `DataEmpty<T>()` | `false` | 无数据（null / 空列表） |
| `DataFail<T>(error)` | `true` | 失败，携带 `Throwable?` |

#### `DataFlowResult<T>`（流式状态）

```text
sealed class DataFlowResult<out T>(
    val complete: Boolean,    // 是否终态（非 Loading）
    val shouldLoad: Boolean,
    open val trigger: Trigger?,
    private val value: T?,
)
```

| 子类 | `complete` | `shouldLoad` | 说明 |
| --- | --- | --- | --- |
| `Uninitialized` | `false` | `true` | 初始未加载（object 单例） |
| `DataFlowLoading` | `false` | `false` | 加载中 |
| `DataFlowSuccess<T>` | `true` | `false` | 成功 |
| `DataFlowEmpty<T>` | `true` | `false` | 空数据 |
| `DataFlowFail<T>` | `true` | `true` | 失败，携带 `Throwable?` |

### 5.2 空值语义

`null`、空 `List` 会被转换为 `DataEmpty` / `DataFlowEmpty`，因此"空列表 ≠ 失败"、"空列表 ≠ 数据"，UI 层可以精确区分三种展示（数据 / 空布局 / 错误）。

### 5.3 重试体系

- **`Trigger`**：内部 `MutableSharedFlow<Unit>`（`replay=0, extraBufferCapacity=1`）。`getRetryFlow()` 会先发射一次 `Unit`（自动触发首次请求），`retry()` 触发新一轮请求。
- **`RetryRequestImpl.refreshableRequest`**：`trigger.getRetryFlow().flatMapLatest { ... }`，每次触发都会执行一次请求，并自动包上 `DataFlowLoading` / `DataFlowEmpty` / `DataFlowFail`。
- **`ApiRequestImpl.request`**：`flow { emit(request()) }.retry()`，失败自动重试（最多 10 次，随机 1~5s 退避，`JsonConvertException` 不重试）。
- **`ApiRequestImpl.reRequest`**：组合两者 —— 自动重试 + 可响应式刷新。

### 5.4 cURL 插件

`curlLoggerPlugin` 是一个 Ktor `ClientPlugin`：

- `enableLog`：开关日志打印（curl 命令 + 响应状态/耗时/body）。
- `onServerTime`：利用响应头 `Date` 估算服务器时间（取响应耗时最短的一次），可用于时钟同步。
- `historyStore`：非空时把每次请求记录进 `HistoryStore`（默认 `InMemoryHistoryStore(capacity=99)`，仅内存）。

### 5.5 自动重试规则

```kotlin
fun <T> Flow<T>.retry() = this.retryWhen { cause, attempt ->
    if (attempt <= 10 && cause !is JsonConvertException) {
        delay((1000L..5000).random().milliseconds) // 随机退避
        true
    } else false
}
```

- 重试上限：10 次；
- 退避策略：每次 1~5 秒随机；
- 不重试：`JsonConvertException`（解析失败重试无意义）。

---

## 6. 配置说明（表格：字段/类型/必填/默认值/说明）

### 6.1 `Trigger`

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `tryFlow` | `MutableSharedFlow<Unit>` | — | `replay=0, extraBufferCapacity=1` | 内部触发流 |

### 6.2 `curlLoggerPlugin`

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `enableLog` | `() -> Boolean` | 否 | `{ false }` | 是否打印 curl 与响应日志 |
| `onServerTime` | `((String, Long) -> Unit)?` | 否 | `null` | 服务器时间回调（Date 头, 耗时 ms） |
| `historyStore` | `HistoryStore?` | 否 | `null` | 请求历史存储，非空则记录 |

### 6.3 `InMemoryHistoryStore`

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `capacity` | `Int` | 否 | `99` | 历史条目上限，超出丢弃最旧（FIFO） |

### 6.4 `retry()` 扩展（自动重试规则）

| 项目 | 值 | 说明 |
| --- | --- | --- |
| 最大重试次数 | 10 | `attempt <= 10` |
| 退避间隔 | 1000~5000ms | 随机 |
| 不重试异常 | `JsonConvertException` | 反序列化失败不重试 |

### 6.5 Serializer 配置项

| Serializer | 可配置项 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `StringAsBooleanSerializer` | `trueString` / `falseString` | `"Y"` / `"N"` | 布尔 ↔ 字符串映射（忽略大小写） |
| `ColorSerializer` | — | — | Compose Color ↔ HEX 字符串 |
| `LocalDateStringSerializer` | `formatter` | `yyyy-MM-dd` | 日期字符串 |
| `LocalDateLongSerializer` | `DEFAULT_TIME_ZONE` | 系统时区 | 日期 ↔ 当天 0 点毫秒时间戳（兼容秒级） |
| `LocalDateTimeStringSerializer` | `format` | `yyyy-MM-dd HH:mm:ss` | 日期时间字符串 |
| `LocalDateTimeLongSerializer` | `DEFAULT_TIME_ZONE` | 系统时区 | 日期时间 ↔ 毫秒时间戳（兼容秒级） |

---

## 7. API 参考

包结构：`org.muc.network`

- 根包：`DataResult` 家族、`runCatching*AsDataResult` 系列、`asDataResult`
- `org.muc.network.status`：`DataFlowResult` 家族、`asDataStatusResult` 系列
- `org.muc.network.retry`：`RetryRequest` / `RetryRequestImpl`、`ApiRequest` / `ApiRequestImpl`、`Trigger`、`retry()`
- `org.muc.network.curlplugin`：`curlLoggerPlugin`
- `org.muc.network.curlplugin.history`：`HistoryStore`、`InMemoryHistoryStore`、`HistoryEntry`、`HttpHistoryEntry`
- `org.muc.network.serializer`：6 个自定义 Serializer
- `org.muc.network.di`：`json`（宽松 Json 实例）、`ioScope`、`nowMillis()`

### 7.1 `DataResult` 家族

```kotlin
sealed class DataResult<out T>(val shouldLoad: Boolean, open val trigger: Trigger?, private val value: T?)
```

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `invoke` | `open operator fun invoke(): T?` | 取值；`DataSuccess` 返回强类型 `T`，其余返回 `null` |
| `retry` | `suspend fun retry()` | 触发重试（`trigger?.retry()`） |
| `retry(scope)` | `fun retry(scope: CoroutineScope)` | 在指定作用域触发重试 |

**子类构造**：

```kotlin
data class DataSuccess<out T>(private val value: T, override val trigger: Trigger? = null)
data class DataEmpty<out T>(override val trigger: Trigger? = null)
data class DataFail<out T>(val error: Throwable? = null, override val trigger: Trigger? = null) {
    constructor(msg: String, trigger: Trigger? = null)  // 便捷：msg -> IllegalStateException(msg)
}
```

**扩展函数**：

| 函数 | 签名 | 说明 |
| --- | --- | --- |
| `map` | `inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R>` | 变换成功值，状态透传 |
| `onSuccess` | `inline fun <reified T> DataResult<T>.onSuccess(elseBlock: () -> Unit = {}, action: (T) -> Unit): DataResult<T>` | 成功回调 |
| `onEmpty` | `inline fun <reified T> DataResult<T>.onEmpty(action: () -> Unit): DataResult<T>` | 空回调 |
| `onSuccessOrEmpty` | `inline fun <reified T> DataResult<T>.onSuccessOrEmpty(action: (T?) -> Unit): DataResult<T>` | 成功或空统一回调 |
| `onFail` | `inline fun <reified T> DataResult<T>.onFail(action: DataResult<T>.(cause: Throwable?) -> Unit): DataResult<T>` | 失败回调（接收者内可访问 `error`） |
| `asDataResult` | `fun <T> T?.asDataResult(): DataResult<T>` | 值 → 状态（null/空列表→Empty） |

**构造辅助**：

```kotlin
suspend inline fun <T> runCatchingSuspendAsDataResult(block: suspend () -> T): DataResult<T>
suspend inline fun <T, R> runCatchingSuspendAsDataResult(block: suspend () -> T, v2r: suspend (T) -> DataResult<R>): DataResult<R>
inline fun <T> runCatchingAsDataResult(block: () -> T): DataResult<T>
inline fun <T, R> runCatchingAsDataResult(block: () -> T, v2r: (T) -> DataResult<R>): DataResult<R>
```

> 说明：`v2r` 重载允许把"取原始响应 + 业务状态转换"两步合并，统一异常捕获。执行在 `Dispatchers.IO`（suspend 版）。

### 7.2 `DataFlowResult` 家族（`org.muc.network.status`）

```kotlin
sealed class DataFlowResult<out T>(val complete: Boolean, val shouldLoad: Boolean, open val trigger: Trigger?, private val value: T?)
```

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `invoke` | `open operator fun invoke(): T?` | 取值 |
| `retry` | `suspend fun retry()` | 触发重试 |
| `retry(scope)` | `fun retry(scope: CoroutineScope)` | 作用域内重试 |

**状态对象**：

```kotlin
object Uninitialized : DataFlowResult<Nothing>(...)
data class DataFlowLoading<out T>(override val trigger: Trigger? = null)
data class DataFlowSuccess<out T>(private val value: T, override val trigger: Trigger? = null)
data class DataFlowEmpty<out T>(override val trigger: Trigger? = null)
data class DataFlowFail<out T>(val error: Throwable? = null, override val trigger: Trigger? = null) {
    constructor(msg: String, trigger: Trigger? = null)
}
```

**扩展函数**：

| 函数 | 签名 | 说明 |
| --- | --- | --- |
| `map` | `inline fun <T, R> DataFlowResult<T>.map(transform: (T) -> R): DataFlowResult<R>` | 变换 |
| `asResult` | `inline fun <T, R> DataFlowResult<T>.asResult(transform: (T) -> R): DataResult<R>` | 转一次性结果 |
| `onSuccess` / `onEmpty` / `onSuccessOrEmpty` / `onFail` | 同 DataResult 语义 | 回调 |
| `onLoading` | `inline fun <reified T> DataFlowResult<T>.onLoading(action: () -> Unit)` | 加载回调（数据为 null 时） |
| `onComplete` | `inline fun <reified T> DataFlowResult<T>.onComplete(action: () -> Unit)` | 终态回调（complete=true） |
| `data` | `val <T> DataFlowResult<T>.data get()` | 等价 `invoke()` |

**Flow 转换**：

```kotlin
fun <T> Flow<T?>.asDataStatusResult(value: T? = null): Flow<DataFlowResult<T>>
suspend fun <T> Flow<T?>.asDataStatusResult(getValue: suspend () -> T?): Flow<DataFlowResult<T>>
fun <T> Flow<Pair<Trigger, T?>>.asDataStatusResult(tigger: Trigger? = null): Flow<DataFlowResult<T>>
fun <T> T?.asDataFlowResult(trigger: Trigger): DataFlowResult<T>
```

> `asDataStatusResult` 会在 `onStart` 发射 `DataFlowLoading`，`catch` 中将 `NullPointerException` 转为 `DataFlowEmpty`，其余异常转为 `DataFlowFail`。

### 7.3 重试（`org.muc.network.retry`）

#### `Trigger`

```kotlin
class Trigger {
    suspend fun retry()
    internal fun getRetryFlow()
}
```

| 成员 | 说明 |
| --- | --- |
| `retry()` | 触发一次新的请求流（发射 `Unit`） |
| `getRetryFlow()` | 内部使用：首次订阅自动发射一次 |

**示例**：

```kotlin
val trigger = Trigger()
// 下拉刷新
pullRefresh { trigger.retry() }
```

#### `RetryRequest` / `RetryRequestImpl`

```kotlin
interface RetryRequest {
    fun <T> refreshableRequest(
        request: suspend () -> Flow<T>,
        trigger: Trigger = Trigger(),
    ): Flow<DataFlowResult<T>>

    fun <T, R> refreshableRequest(
        request: suspend () -> Flow<T>,
        trigger: Trigger = Trigger(),
        v2r: suspend T.(Trigger) -> DataFlowResult<R>
    ): Flow<DataFlowResult<R>>
}
object RetryRequestImpl : RetryRequest
```

- 每次 `trigger` 触发都会 `flatMapLatest` 重新执行 `request`。
- `v2r` 重载：原始值 → 业务状态转换（如业务码判断）。

#### `ApiRequest` / `ApiRequestImpl`

```kotlin
interface ApiRequest : RetryRequest {
    fun <T> request(request: suspend () -> T): Flow<T?>
    fun <T> reRequest(request: suspend () -> T): Flow<DataFlowResult<T>>
    fun <T, R> reRequest(request: suspend () -> T, v2r: suspend T.(Trigger) -> DataFlowResult<R>): Flow<DataFlowResult<R>>
}
object ApiRequestImpl : ApiRequest, RetryRequest by RetryRequestImpl
```

| 方法 | 说明 |
| --- | --- |
| `request {}` | 单次请求 + 自动重试，返回 `Flow<T?>` |
| `reRequest {}` | 自动重试 + 可刷新，返回 `Flow<DataFlowResult<T>>` |

#### `retry()` 扩展

```kotlin
fun <T> Flow<T>.retry()  // 见 6.4 配置说明
```

### 7.4 cURL 插件（`org.muc.network.curlplugin`）

```kotlin
@OptIn(InternalAPI::class)
fun curlLoggerPlugin(
    enableLog: () -> Boolean = { false },
    onServerTime: ((String, Long) -> Unit)? = null,
    historyStore: HistoryStore? = null
): ClientPlugin<Unit>
```

**Ktor 接入示例**：

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

val client = HttpClient(OkHttp) {
    install(curlLoggerPlugin(
        enableLog = { BuildConfig.DEBUG },
        historyStore = InMemoryHistoryStore(),
    ))
}
```

| 参数 | 说明 |
| --- | --- |
| `enableLog` | 返回 `true` 时打印 curl 命令与响应日志 |
| `onServerTime` | 服务器时间回调：`(dateHeader, durationMs)`，仅在耗时小于历史最小值时回调一次 |
| `historyStore` | 非空时每次请求写入历史 |

### 7.5 历史存储（`org.muc.network.curlplugin.history`）

#### `HistoryStore`

```kotlin
interface HistoryStore {
    val entries: StateFlow<List<HistoryEntry>>
    fun append(entry: HistoryEntry)
    fun update(id: Long, transform: (HistoryEntry) -> HistoryEntry)
    fun clear()
}
```

#### `InMemoryHistoryStore`

```kotlin
class InMemoryHistoryStore(private val capacity: Int = DEFAULT_CAPACITY) : HistoryStore
```

线程安全的内存实现（`synchronized` + `ArrayDeque` O(1) 头部淘汰），`DEFAULT_CAPACITY = 99`。

#### `HistoryEntry` / `HttpHistoryEntry`

```kotlin
interface HistoryEntry {
    val id: Long
    val isSuccess: Boolean
}

data class HttpHistoryEntry(
    override val id: Long,
    val timestampEpochMs: Long,
    val method: String,
    val url: String,
    val curl: String,
    val responseBodyString: String,
    val durationMs: Long,
    val httpCode: Int?,
    val shortError: String?,
) : HistoryEntry
```

`isSuccess` = `httpCode in 200..299 && shortError.isNullOrBlank()`。

### 7.6 Serializer（`org.muc.network.serializer`）

| Serializer | 类型 | 序列化格式 |
| --- | --- | --- |
| `IntAsBooleanSerializer` | `Boolean` | `1` / `0`（Int） |
| `StringAsBooleanSerializer` | `Boolean` | `Y` / `N`（String，可配置） |
| `ColorSerializer` | `androidx.compose.ui.graphics.Color` | `#AARRGGBB` HEX（兼容 #RGB/#ARGB/#RRGGBB 读取） |
| `LocalDateStringSerializer` | `LocalDate` | `yyyy-MM-dd` |
| `LocalDateLongSerializer` | `LocalDate` | 当天 0 点毫秒时间戳（兼容秒级输入） |
| `LocalDateTimeStringSerializer` | `LocalDateTime` | `yyyy-MM-dd HH:mm:ss` |
| `LocalDateTimeLongSerializer` | `LocalDateTime` | 毫秒时间戳（兼容秒级输入） |

**用法示例**（`@Serializable(with = ...)`）：

```kotlin
import kotlinx.serialization.Serializable
import org.muc.network.serializer.LocalDateStringSerializer

@Serializable
data class UserProfile(
    val name: String,
    @Serializable(with = LocalDateStringSerializer::class)
    val birthday: LocalDate,
)
```

**工具扩展**：

```kotlin
object ColorSerializer {
    fun hexStrToColor(hex: String): Color
    fun colorToHexStr(color: Color): String
}
fun Int.toHexColor(): String
val DayOfWeek.shortName: String      // 周一..周日
fun LocalDateTime.Companion.now(): LocalDateTime
fun LocalDate.Companion.now(): LocalDate
val weekFullNames / weekShortNames: DayOfWeekNames
```

---

## 8. 错误码表

**无自定义业务错误码。** 失败统一通过 `DataFail` / `DataFlowFail` 的 `error: Throwable?` 暴露。

| HTTP / 异常场景 | 表现 |
| --- | --- |
| 网络异常（连接失败/超时） | `DataFail(IOException(...))` |
| 反序列化异常 | `DataFail(JsonConvertException(...))`；自动重试会跳过此类异常 |
| 业务失败（Http 200 但业务 code ≠ 0） | 需在 `v2r` 转换中自行映射为 `DataFail`（源码 TODO 注释提示当前未自动处理内层业务码） <!-- TODO: 待确认 —— 内层业务码自动映射策略待实现 --> |
| HTTP 非 2xx | 由请求函数抛异常或返回非预期值，由调用方约定；`HttpHistoryEntry.httpCode` 记录状态码 |
| 取消（CancellationException） | `runCatching*AsDataResult` 捕获后包装为 `DataFail(e)`（不吞协程取消语义） |

---

## 9. 常见场景示例（至少 3 个：认证、分页/批量、错误处理）

### 9.1 认证：登录请求 + 业务码校验 + 刷新

```kotlin
import org.muc.network.DataResult
import org.muc.network.DataFail
import org.muc.network.DataSuccess
import org.muc.network.retry.ApiRequestImpl
import org.muc.network.status.DataFlowResult
import org.muc.network.status.DataFlowFail
import org.muc.network.status.DataFlowSuccess

// 登录：v2r 校验业务码
fun loginFlow(account: String, pwd: String) = ApiRequestImpl.reRequest(
    request = { api.login(account, pwd) },          // suspend () -> LoginResp
    v2r = { resp ->                                  // T.(Trigger) -> DataFlowResult<LoginResp>
        if (resp.code == 0) DataFlowSuccess(resp, this)
        else DataFlowFail(resp.msg, this)
    },
)

// 消费
scope.launch {
    loginFlow("alice", "123456").collect { state ->
        when (state) {
            is DataFlowFail -> showError(state.error?.message ?: "登录失败")
            is DataFlowSuccess -> { saveToken(state().token); goHome() }
            else -> {}
        }
    }
}
```

### 9.2 分页 / 批量：列表请求 + 下拉刷新 + 自动重试

```kotlin
import org.muc.network.retry.Trigger

class ListViewModel(private val scope: CoroutineScope) {
    private val trigger = Trigger()
    private val pageSize = 20

    val listFlow = RetryRequestImpl.refreshableRequest(
        request = { api.fetchPage(page = 1, size = pageSize) },
        trigger = trigger,
    )

    fun refresh() = scope.launch { trigger.retry() }   // 下拉刷新 / 点击重试

    fun loadMore() = scope.launch {
        // 追加分页可结合 state 与 offset 自行实现
        val more = runCatchingAsDataResult { api.fetchPage(page = 2, size = pageSize) }
        more.onSuccess { items -> append(items) }
    }
}
```

### 9.3 错误处理：空态区分 + 兜底默认值

```kotlin
import org.muc.network.runCatchingAsDataResult
import org.muc.network.onSuccess
import org.muc.network.onEmpty
import org.muc.network.onFail

val result = runCatchingAsDataResult { fetchOrders() }

result
    .onEmpty { showEmptyView("暂无订单") }
    .onFail { cause ->
        showErrorView(cause?.message ?: "加载失败")
        // 或降级读取本地缓存
        orders = loadLocalCache()
    }
    .onSuccess { orders -> showList(orders) }
```

---

## 10. 常见问题 FAQ（至少 5 条）

**Q1：`DataResult` 和 `DataFlowResult` 怎么选？**

一次性数据（如单次按钮触发的请求）用 `DataResult`；需要在 UI 上持续驱动状态（Loading → Success/Empty/Fail 流转，配合重试/刷新）用 `DataFlowResult`。`DataFlowResult` 自带 `Uninitialized` 初始态与 `complete` 标记，更适合 Compose `collectAsState`。

**Q2：`Trigger` 为什么首次订阅会自动请求一次？**

`getRetryFlow()` 在 `onStart` 阶段发射一次 `Unit`，保证 `refreshableRequest` 组装出的 Flow 在被收集时立即执行首次请求，无需手动触发。

**Q3：自动重试会重试哪些异常？最多几次？**

非 `JsonConvertException` 的异常最多重试 10 次，每次随机退避 1~5 秒。解析失败不重试（重试无意义）；协程取消（`CancellationException`）会向上传播语义，不会无限重试。

**Q4：`onFail` 回调里怎么拿到错误对象？**

`onFail` 的接收者是 `DataResult<T>` 本身，内部可用 `error` 属性：`onFail { show(it?.message) }` 或 `onFail { cause -> show(this.error?.message) }`。`DataFlowFail` 同理。

**Q5：curl 插件为什么有时打印不出请求？**

① `enableLog` 返回 `false`（默认）时不打印；② 插件只记录 `onRequest` 之后的请求，若在插件安装前已发出则不会记录；③ `historyStore` 为 `null` 时不写历史（仅打印）。调试期建议 `enableLog = { BuildConfig.DEBUG }` 并传入 `InMemoryHistoryStore()`。

**Q6：`ColorSerializer` 支持哪些 HEX 格式？**

读取支持 `#RGB`（如 `#F0F`）、`#ARGB`（4 位）、`#RRGGBB`、`#AARRGGBB` 以及无 `#` 前缀的数字形式；格式非法时容错返回 `Color.Gray`。序列化统一输出 `#AARRGGBB`（大写）。

---

## 11. 版本与变更

| 版本 | 说明 |
| --- | --- |
| `1.1.22` | 当前版本（与 Mold 各模块统一版本号） |

<!-- TODO: 待确认 —— 仓库中未发现 CHANGELOG，历史版本与变更记录请补充 -->

---

## 12. License

本项目基于 **MIT License** 发布。

<!-- TODO: 待确认 —— 仓库内暂未包含 LICENSE 文件，发布前请补充 LICENSE 文件并核对版权归属。 -->
