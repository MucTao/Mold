# Mold DataKV

## SDK 基本信息

| 项目 | 内容 |
| --- | --- |
| 名称 | Mold DataKV（`dataKV`） |
| 用途 | 类型安全的键值存储 SDK，基于属性委托 + DataStore，支持过期时间、响应式 Flow 与跨应用数据共享 |
| 语言 / 平台 | Kotlin Multiplatform（Android / iOS / Desktop-JVM） |
| 包管理器与安装命令 | Gradle（Maven / JitPack），详见[安装](#3-安装) |
| 当前版本 | `1.1.22` |
| License | MIT <!-- TODO: 待确认 —— 仓库内暂未发现 LICENSE 文件，发布前请补充并核对许可证文本 --> |
| 目标读者 | 需要本地持久化或跨应用数据共享的 Android / KMP 应用开发者 |

> 坐标：`com.github.MucTao.Mold:dataKV:1.1.22`

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

**Mold DataKV** 提供声明式的键值存储能力：你只需要让数据宿主类实现 `IDataKVOwner`，再用 `datakv()` / `dataCross()` 属性委托声明字段，即可获得自动序列化、过期管理、响应式监听，以及**跨应用数据共享**。

主要特性：

- **属性委托零模板**：`val count by datakv(0)` 即可获得读写、监听能力，无需手写序列化与文件 IO。
- **自动序列化**：基于 kotlinx-serialization，任意 `@Serializable` 类型均可存储。
- **过期时间（Expiration）**：支持时间戳、`Duration`、`LocalDateTime` 三种过期设定，过期自动清理并回落默认值。
- **响应式监听**：每个委托暴露 `Flow<V>` / `StateFlow<V>`，数据变化即通知。
- **双引擎**：
  - `DataStoreKVDelegate`：本地持久化（DataStore + Okio），进程内单例缓存；
  - `DataContentKVDelegate`：通过 `ContentProvider` 实现跨应用共享，支持多 App 实时同步（监听 `ContentObserver`）。
- **宽松 JSON 配置**：内置宽松解析、忽略未知键、强制输入值等容错配置，兼容脏数据。

---

## 2. 环境要求（兼容性矩阵表格）

| 项目 | 要求 |
| --- | --- |
| Kotlin | ≥ 2.4.10 |
| Android compileSdk | 37 |
| Android minSdk | 21 |
| JVM 目标 | 11（`jvmTarget = JVM_11`） |
| DataStore | androidx.datastore 1.2.1（core + preferences） |
| kotlinx-serialization | 1.11.0 |
| kotlinx-datetime | 0.8.0 |
| 协程 | kotlinx-coroutines 1.11.0 |

**平台目标矩阵**：

| 平台 | 支持 | 说明 |
| --- | --- | --- |
| Android | ✅ | 默认目标；跨应用共享能力仅 Android 可用 |
| iOS（iosArm64 / iosSimulatorArm64） | ✅ | 本地存储可用；跨应用共享不可用 |
| Desktop（JVM） | ✅ | 本地存储可用；跨应用共享不可用 |
| Web（Wasm/JS） | ❌ | 当前未配置 |

> 跨应用共享（`dataCross`）依赖 Android `ContentProvider`，仅在 Android 平台生效。

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
    implementation("com.github.MucTao.Mold:dataKV:1.1.22")
}
```

### 3.3 本地仓库（可选）

```kotlin
repositories {
    maven { url = uri("F:/Android/WorkSpace/repo") }
}
```

### 3.4 Android 额外配置

使用跨应用共享（`dataCross`）时，必须在 `AndroidManifest.xml` 中注册 `ShareProvider`：

```xml
<application>
    <!-- authority 必须与 DataKV.init() 传入的 authority 一致 -->
    <provider
        android:name="org.muc.datakv.ShareProvider"
        android:authorities="org.muc.content.provider.ShareProvider"
        android:exported="true"
        android:process=":MoldShareProvider" />
</application>
```

> 说明：`ShareProvider` 在独立进程 `:MoldShareProvider` 中运行；`android:exported="true"` 是跨应用共享的必要条件（若仅单应用使用可考虑收紧）。

---

## 4. 快速开始（含最小可运行示例 + 预期输出）

### 4.1 Android 初始化（可选，跨应用共享时需要）

```kotlin
import org.muc.datakv.di.DataKV

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // authority 与 Manifest 注册保持一致；table 为数据表名（默认 MoldCP）
        DataKV.init(this, authority = "org.muc.content.provider.ShareProvider", table = "MoldCP")
    }
}
```

### 4.2 定义数据宿主与字段

```kotlin
import org.muc.datakv.IDataKVOwner
import org.muc.datakv.datakv

// 1) 让类实现 IDataKVOwner
class SettingsRepository : IDataKVOwner {
    // 2) 用 datakv() 声明字段（本地存储）
    var userName by datakv("guest")
    var loginCount by datakv(0)

    // 3) 如需区分存储目录，可重写 storeName
    // override val storeName: String? = "userPrefs"
}
```

### 4.3 读写与监听

```kotlin
import kotlinx.coroutines.flow.collect

suspend fun demo() {
    val repo = SettingsRepository()

    // 读取（挂起）
    val name = repo.userName.getValue()
    println("当前用户: $name")

    // 写入（挂起）：基于旧值变换
    repo.loginCount.setValue { old -> old + 1 }
    println("登录次数: ${repo.loginCount.getValue()}")

    // 响应式监听
    repo.userName.flow.collect { value ->
        println("用户名变化: $value")
    }
}
```

### 4.4 预期输出

```text
当前用户: guest
登录次数: 1
用户名变化: guest   （首次订阅即发射当前值）
```

> 说明：委托字段（`repo.userName`）的类型是 `DataKVDelegate<String>`，需通过 `getValue()` / `setValue {}` / `flow` 等成员访问，而非直接赋值。

---

## 5. 核心概念

### 5.1 三层结构：Owner → Property → Delegate

```text
IDataKVOwner（宿主类）
      │  声明属性委托
      ▼
DataKVProperty（属性占位，缓存 Delegate）
      │  createDelegate()
      ▼
DataKVDelegate<V>（真正读写入口：flow / getValue / setValue / clear）
```

- `IDataKVOwner`：宿主标记接口，可选覆盖 `storeName`（默认 `null`，使用属性名作为存储文件名）。
- `DataKVProperty<V>`：抽象属性委托类，内部缓存 `DataKVDelegate`，保证同一属性多次访问复用同一实例。
- `DataKVDelegate<V>`：对外核心接口，见 [7.3](#73-datakvdelegate)。

### 5.2 两种存储引擎

| 委托 | 工厂函数 | 存储介质 | 能力 |
| --- | --- | --- | --- |
| `DataStoreKVDelegate` | `datakv(default)` | DataStore + Okio 文件（`filesDir/<storeName>/<key>.json`） | 本地持久化、过期、Flow |
| `DataContentKVDelegate` | `dataCross(default)`（即 `datakv(default, cross = true)`） | ContentProvider（DataStore 表） | 上述全部 + **跨应用共享、跨进程实时同步** |

### 5.3 过期机制

- 存储结构为 `ExpirableData<T>(data, expireTime)`，`expireTime <= 0`（即 `NO_EXPIRATION = 0L`）表示永不过期。
- 过期时间支持三种写法（`setValue` 重载）：
  - 时间戳毫秒：`setValue(expireTime = nowMillis() + 3_600_000) { ... }`
  - `Duration`：`setValue(3.hours) { ... }`
  - `LocalDateTime`：`setValue(LocalDateTime.now().plus(3, DateTimeUnit.HOUR)) { ... }`
- 读取时发现过期 → 返回默认值并异步清理存储；`expireTimeFlow` / `expireTimeDurationFlow` 实时反映剩余有效期（`Duration.ZERO` 表示已过期/无过期）。

### 5.4 跨应用共享原理（Android）

- 写端通过 `ContentResolver.update(content://AUTHORITY/TABLE/<key>, ...)` 写入，`ShareProvider` 内部落盘 DataStore 并 `notifyChange()`。
- 读端注册 `ContentObserver`，数据变化时向 `valueChangeKeyFlow` 发射 key，`DataContentKVDelegate` 监听后自动重新加载并更新 `StateFlow`。
- 因此**多 App / 多进程可实时感知同一 key 的变化**。

### 5.5 JSON 容错配置

模块内置 `Json` 实例（`org.muc.datakv.di.json`）：

| 配置 | 值 | 效果 |
| --- | --- | --- |
| `prettyPrint` | `true` | 输出格式化 JSON |
| `isLenient` | `true` | 宽松解析（容忍无引号 key、类型自动转换） |
| `ignoreUnknownKeys` | `true` | 忽略未知字段 |
| `coerceInputValues` | `true` | 字段类型不匹配时使用默认值 |
| `encodeDefaults` | `true` | 序列化时输出默认值字段 |
| `explicitNulls` | `true` | 保留显式 null |
| `allowStructuredMapKeys` | `true` | 允许对象作为 Map key |
| `allowSpecialFloatingPointValues` | `true` | 允许 NaN / Infinity |

---

## 6. 配置说明（表格：字段/类型/必填/默认值/说明）

### 6.1 `IDataKVOwner`

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `storeName` | `String?` | 否 | `null` | 存储目录名；为 `null` 时直接以属性名作为存储文件名 |

### 6.2 `datakv` / `dataCross`（属性委托工厂）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `default` | `T` | 是 | — | 默认值；读取缺失/过期/失败时返回 |
| `cross` | `Boolean` | 否 | `false` | `datakv` 专用：`true` 等价于 `dataCross` |

### 6.3 `DataKV.init`（仅 Android 跨应用共享）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `app` | `Application` | 是 | — | Application 实例 |
| `authority` | `String` | 否 | `org.muc.content.provider.ShareProvider` | ContentProvider 的 authorities，须与 Manifest 一致 |
| `table` | `String` | 否 | `MoldCP` | 数据表名 |

### 6.4 `setValue` 过期参数

| 重载 | 参数 | 说明 |
| --- | --- | --- |
| `setValue(expireTime: Long, block)` | 过期时间戳（毫秒），`0` = 永不过期 | 最底层实现 |
| `setValue(duration: Duration, block)` | `Duration`，非正数视为永不过期 | 相对当前时间 |
| `setValue(dateTime: LocalDateTime, block)` | 具体日期时间（按系统时区换算） | 绝对时间点 |

---

## 7. API 参考

包结构：`org.muc.datakv`

- 根包：`IDataKVOwner`、`DataKVDelegate`、`DataKVProperty`、`ExpirableData`、`datakv()`、`dataCross()`
- `org.muc.datakv.di`：`DataKV`（Android 入口）、`NO_EXPIRATION`、`json`、`ioScope`、`nowMillis()`
- `org.muc.datakv.content`：`DataContentEngine`、`DataContentKVProperty`、`DataContentKVDelegate`
- `org.muc.datakv.datastore`：`DataStoreCache`、`DataStoreKVProperty`、`DataStoreKVDelegate`

### 7.1 `IDataKVOwner`

```kotlin
interface IDataKVOwner {
    val storeName: String? get() = null
}
```

数据宿主标记接口。

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `storeName` | `val storeName: String?` | 可选存储目录名 |

### 7.2 属性委托工厂函数

```kotlin
inline fun <reified T> IDataKVOwner.dataCross(default: T): DataKVProperty<T>

inline fun <reified T> IDataKVOwner.datakv(default: T, cross: Boolean = false): DataKVProperty<T>
```

| 函数 | 说明 |
| --- | --- |
| `datakv(default)` | 本地持久化委托（DataStore） |
| `dataCross(default)` | 跨应用共享委托（ContentProvider） |

**示例**：

```kotlin
class Repo : IDataKVOwner {
    val localCount by datakv(0)
    val sharedToken by dataCross("")
    // 等价写法：val sharedToken by datakv("", cross = true)
}
```

### 7.3 `DataKVDelegate`

```kotlin
interface DataKVDelegate<V> {
    val defaultValue: V
    val flow: Flow<V>
    suspend fun getValue(): V
    val value: V get() = runBlocking { getValue() }   // 阻塞版本，慎用
    val expireTimeFlow: StateFlow<Long>
    val expireTime: Long get() = expireTimeFlow.value
    val expireTimeDurationFlow: Flow<Duration>
    suspend fun setValue(expireTime: Long = NO_EXPIRATION, block: (V) -> V): V
    suspend fun setValue(duration: Duration, block: (V) -> V): V
    suspend fun setValue(dateTime: LocalDateTime, block: (V) -> V): V
    fun clear()
}
```

| 成员 | 签名 | 返回值 | 说明 |
| --- | --- | --- | --- |
| `defaultValue` | `val defaultValue: V` | `V` | 默认值 |
| `flow` | `val flow: Flow<V>` | `Flow<V>` | 响应式数据流（含过期回退逻辑） |
| `getValue` | `suspend fun getValue(): V` | `V` | 读取当前值（挂起） |
| `value` | `val value: V` | `V` | 阻塞读取（`runBlocking`，主线程慎用） |
| `expireTimeFlow` | `val expireTimeFlow: StateFlow<Long>` | `StateFlow<Long>` | 过期时间戳（0 = 永不过期） |
| `expireTime` | `val expireTime: Long` | `Long` | 当前过期时间戳 |
| `expireTimeDurationFlow` | `val expireTimeDurationFlow: Flow<Duration>` | `Flow<Duration>` | 剩余有效期流（自动递减） |
| `setValue` | `suspend fun setValue(expireTime: Long = NO_EXPIRATION, block: (V) -> V): V` | `V` | 基于旧值变换并写入，可带过期时间 |
| `setValue` | `suspend fun setValue(duration: Duration, block: (V) -> V): V` | `V` | Duration 过期重载 |
| `setValue` | `suspend fun setValue(dateTime: LocalDateTime, block: (V) -> V): V` | `V` | 绝对时间过期重载 |
| `clear` | `fun clear()` | 无 | 清除存储，值回退为默认值 |

**示例**：

```kotlin
suspend fun usage(delegate: DataKVDelegate<Int>) {
    delegate.setValue { it + 1 }                 // 自增
    delegate.setValue(5.minutes) { it * 2 }      // 5 分钟后过期
    delegate.setValue(LocalDateTime.now().plus(1, DateTimeUnit.HOUR)) { 42 }
    delegate.flow.collect { println(it) }
    delegate.clear()
}
```

### 7.4 `DataKVProperty`

```kotlin
abstract class DataKVProperty<V>(private val default: V) : ReadOnlyProperty<IDataKVOwner, DataKVDelegate<V>>
```

抽象委托类，`getValue` 内部缓存 `DataKVDelegate` 实例。子类：

| 子类 | 说明 |
| --- | --- |
| `DataStoreKVProperty<V>` | 本地存储委托；构造参数 `(getDataStore: (String) -> DataStore<ExpirableData<V>>, default: V)` |
| `DataContentKVProperty<V>` | 跨应用委托；构造参数 `(serializer: KSerializer<V>, engine: DataContentEngine, storeName: String?, default: V)`；key 规则：`storeName == null ? 属性名 : "${storeName}_${属性名}"` |

### 7.5 `ExpirableData`

```kotlin
@Serializable
data class ExpirableData<T>(
    val data: T? = null,          // 原始存储数据
    val expireTime: Long = NO_EXPIRATION  // 过期时间戳（毫秒），<=0 表示永不过期
)
```

持久化载体数据类。`expireTime <= 0`（`NO_EXPIRATION = 0L`）表示永不过期。

### 7.6 `DataKV`（Android 跨应用引擎，`org.muc.datakv.di`）

```kotlin
object DataKV : DataContentEngine
```

| 成员 | 签名 | 说明 |
| --- | --- | --- |
| `init` | `fun init(app: Application, authority: String = ShareProvider.AUTHORITY, table: String = ShareProvider.TABLE)` | 初始化并注册 ContentObserver |
| `app` | `val app: Application` | 当前 Application（`setApp` / `init` 后可用） |
| `setApp` | `fun setApp(app: Application)` | 设置 Application |
| `put` | `suspend fun <T> put(key, value, serializer, expireTime): T` | 写入（空值/过期值自动删除） |
| `delete` | `fun delete(key: String)` | 删除 |
| `get` | `suspend fun <T> get(key, serializer, default): ExpirableData<T>` | 读取（过期自动删除） |
| `valueChangeKeyFlow` | `MutableSharedFlow<String>` | key 变化通知流 |

> 注意：`DataKV` 是 `DataContentEngine` 的 Android 实现；`ShareProvider.onCreate()` 会调用 `DataKV.setApp()`，因此注册 Provider 后引擎自动可用。

### 7.7 `DataContentEngine`

```kotlin
interface DataContentEngine {
    suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long = NO_EXPIRATION): T
    fun delete(key: String)
    suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): ExpirableData<T>
    val valueChangeKeyFlow: MutableSharedFlow<String>
}
```

存储引擎抽象。自定义引擎可实现该接口后通过 `DataContentKVProperty` 使用。

### 7.8 `DataStoreCache`（`org.muc.datakv.datastore`）

```kotlin
object DataStoreCache {
    val lock: SynchronizedObject
    val map: HashMap<String, DataStore<*>>
}

inline fun <reified T> DataStoreCache.createExpirableDataStore(filePath: String): DataStore<ExpirableData<T>>
```

进程内 DataStore 单例缓存：同一 `filePath` 只创建一个 DataStore 实例（双重检查锁）。

---

## 8. 错误码表

**无自定义错误码。**

| 失败场景 | 表现 |
| --- | --- |
| 读取 / 写入 IO 异常 | 多数入口已 `runCatching` 容错：`get` 返回默认值，`put` 返回原值；Android `ContentResolver` 操作异常记 Log（TAG `MoldCP`） |

---

## 9. 常见场景示例（至少 3 个：认证、分页/批量、错误处理）

### 9.1 认证：登录态与 Token 持久化 + 过期

```kotlin
class AuthStore : IDataKVOwner {
    var token by dataCross("")                 // 跨应用共享：主 App 写，扩展 App 读
    var expiresAt by datakv(0L)
}

suspend fun login(token: String, expiresInSeconds: Long) {
    val store = AuthStore()
    store.token.setValue { token }
    store.expiresAt.setValue { nowMillis() + expiresInSeconds * 1000 }
}

// 读取端（另一 App）：自动感知变化
val store = AuthStore()
store.token.flow.collect { newToken ->
    if (newToken.isNotEmpty()) refreshSession(newToken)
}
```

### 9.2 批量：配置批量加载与合并

```kotlin
class AppConfig : IDataKVOwner {
    var theme by datakv("light")
    var fontSize by datakv(14)
    var notificationsEnabled by datakv(true)
}

suspend fun loadConfig() {
    val config = AppConfig()
    // 批量读取（挂起）
    val theme = config.theme.getValue()
    val fontSize = config.fontSize.getValue()
    val notify = config.notificationsEnabled.getValue()
    applyConfig(theme, fontSize, notify)
}
```

### 9.3 错误处理：脏数据容错与过期兜底

```kotlin
class CacheStore : IDataKVOwner {
    var cachedUser by datakv(User())   // 要求 User 是 @Serializable
}

suspend fun readUserSafely(): User {
    val delegate = CacheStore().cachedUser
    return try {
        val user = delegate.getValue()
        if (user.id.isBlank()) User() else user   // 业务校验兜底
    } catch (e: Exception) {
        delegate.clear()                          // 解析失败清缓存
        User()
    }
}
```

---

## 10. 常见问题 FAQ（至少 5 条）

**Q1：为什么 `repo.userName = "x"` 不能直接赋值？**

委托字段的类型是 `DataKVDelegate<V>` 而不是 `V`。需要调用 `setValue { ... }`（挂起）或 `value = ...`（阻塞）完成写入。这是刻意设计：写入本身是异步 IO，且支持基于旧值的变换与过期设定。

**Q2：`datakv` 和 `dataCross` 有什么区别？**

`datakv` 使用本地 DataStore 文件存储，仅本进程可见；`dataCross` 使用 ContentProvider 存储，可在多个 App / 进程间共享并实时同步。后者需要注册 `ShareProvider` 并调用 `DataKV.init()`。

**Q3：数据什么时候被物理删除？**

过期数据在读取时发现（`getValue` / `flow` 内部判断）会触发异步清理（`clearExpiredData`），并回落默认值。若一直不读取，过期数据会保留到下次访问；写入新值时若检测到已过期也会先清理。

**Q4：`expireTimeDurationFlow` 会一直递减吗？精度如何？**

会。它基于 `expireTimeFlow` 每秒/每 10 秒/每分钟（按剩余时长自适应）计算剩余时间并发射；剩余时间 ≤ 0 时发射 `Duration.ZERO` 并结束。可用于 UI 倒计时展示。

**Q5：Android 上 `ShareProvider` 为什么建议独立进程？**

Manifest 示例中 `android:process=":MoldShareProvider"` 使 Provider 在独立进程运行，避免主进程初始化负担。跨进程写入与主进程读取通过 `ContentResolver` 解耦。若你的应用无需跨进程隔离，也可移除该属性（单进程）。

**Q6：存储文件在哪里？**

本地存储文件位于 `filesDir/<storeName或属性名>/<属性名>.json`（`getDataKVStorePath` 将路径拼到 `filesDir` 下）。例如 `filesDir/userPrefs/loginCount.json`。不要手动修改这些文件。

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
