# Mold Utils

## SDK 基本信息

| 项目 | 内容 |
| --- | --- |
| 名称 | Mold Utils（`utils`） |
| 用途 | 跨平台工具库：时间处理（kotlinx-datetime）、常用常量、Android 系统级工具（App/Activity/网络/文件/权限/屏幕/加密等 40+ 工具类） |
| 语言 / 平台 | Kotlin Multiplatform（Android / iOS / Desktop-JVM）；大量工具类为 Android 专用（androidMain） |
| 包管理器与安装命令 | Gradle（Maven / JitPack），详见[安装](#3-安装) |
| 当前版本 | `1.1.22` |
| License | MIT <!-- TODO: 待确认 —— 仓库内暂未发现 LICENSE 文件，发布前请补充并核对许可证文本 --> |
| 目标读者 | Android 开发者（系统级工具类）、KMP 开发者（跨平台时间工具） |

> 坐标：`com.github.MucTao.Mold:utils:1.1.22`

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

**Mold Utils** 是 Mold 生态的公共工具模块，分为三层：

- **commonMain（跨平台）**：
  - `TimeUtils`：基于 `kotlinx-datetime` 的完整时间工具（格式化、时间差、友好时间、星期/月份、星座、生肖、节日），**不使用** `java.util.Date` / `SimpleDateFormat`；
  - 常量对象：`TimeConstants`、`MemoryConstants`、`CacheConstants`、`RegexConstants`。
- **androidMain（Android 专用）**：
  - `Utils`：初始化入口与 Application 上下文获取；
  - 40+ 工具类：App、Activity、Intent、网络、文件、屏幕、键盘、权限、通知、剪贴板、加密、编码、压缩、图片、视频、反射、Shell、ROM、音量、闪光灯、震动、语言、进程、服务、URI、Regex、数字、字符串、异常、元数据等；
  - `PermissionConstants`：权限常量。
- 依赖：kotlinx-datetime、kotlinx-serialization、DataStore、lifecycle-runtime、atomicfu 等（供 `TimeUtils` 及部分工具使用）。

主要特性：

- **现代时间 API**：`TimeUtils` 全部基于 `kotlinx.datetime`，格式化使用 **Builder DSL**，失败返回 `Result<T>`。
- **零依赖使用**：Android 工具类大多为 `object` 单例，静态调用即可。
- **生命周期集成**：`Utils.init()` 内部自动注册 `ActivityLifecycleCallbacks`，可监听前后台切换。

---

## 2. 环境要求（兼容性矩阵表格）

| 项目 | 要求 |
| --- | --- |
| Kotlin | ≥ 2.4.10 |
| Android compileSdk | 37 |
| Android minSdk | 21 |
| JVM 目标 | 11 |
| kotlinx-datetime | 0.8.0 |
| kotlinx-serialization | 1.11.0 |
| DataStore | 1.2.1 |
| androidx.lifecycle | 2.11.0（runtime） |

**平台目标矩阵**：

| 平台 | 支持 | 说明 |
| --- | --- | --- |
| Android | ✅ | 全部能力可用 |
| iOS（iosArm64 / iosSimulatorArm64） | ✅ | 仅 commonMain（`TimeUtils` + 常量）可用 |
| Desktop（JVM） | ✅ | 仅 commonMain 可用 |
| Web（Wasm/JS） | ❌ | 当前未配置 |

> Android 专用工具类位于 `androidMain`，在 iOS / JVM 目标中不存在，编译时不可引用。

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
    implementation("com.github.MucTao.Mold:utils:1.1.22")
}
```

### 3.3 本地仓库（可选）

```kotlin
repositories {
    maven { url = uri("F:/Android/WorkSpace/repo") }
}
```

---

## 4. 快速开始（含最小可运行示例 + 预期输出）

### 4.1 Android 初始化

```kotlin
import org.muc.mold.utils.util.Utils

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}
```

初始化后即可通过 `Utils.app` 获取全局 Application。

### 4.2 时间工具（跨平台）

```kotlin
import org.muc.mold.utils.util.TimeUtils
import kotlin.time.Duration.Companion.minutes

fun demo() {
    // 当前时间字符串（默认 yyyy-MM-dd HH:mm:ss）
    println(TimeUtils.getNowDateTimeString())

    // 时间戳转日期时间
    println(TimeUtils.getNowMills().asDateTime())

    // 5 分钟前的时间差描述
    println(TimeUtils.getNowDateTime().minus(5, kotlin.time.DurationUnit.MINUTES).getFriendlyTimeSpanByNow())

    // 星座 / 生肖 / 星期
    println(TimeUtils.getConstellation())
    println(TimeUtils.getChineseZodiac())
    println(TimeUtils.getChineseWeek())
}
```

### 4.3 预期输出

```text
2026-09-22 14:30:00
2026-09-22 14:30:00
5分钟前
天秤座
马
星期二
```

> 输出随当前时间动态变化，仅作格式示意。

---

## 5. 核心概念

### 5.1 初始化与 Application 获取

- `Utils.init(app)` 保存 Application 引用并初始化 `UtilsActivityLifecycleImpl`（注册 Activity 生命周期回调），同时预加载屏幕适配（`AdaptScreenUtils.preLoadRunnable`）。
- `Utils.app`：优先返回已保存实例；未初始化时通过反射（`applicationByReflect`）自动补初始化；失败抛 `NullPointerException("reflect failed.")`。
- `Utils.sp`：内部默认 `SharedPreferences`（名 `utils`）。

### 5.2 TimeUtils 设计约定

- 全部基于 `kotlinx.datetime` 的 `LocalDate` / `LocalDateTime` / `LocalTime` / `TimeZone`；
- 格式化一律使用 **Builder DSL**（`LocalDateTime.Format { year(); char('-') ... }`），不使用 `byUnicodePattern` 格式字符串；
- 可能失败的操作返回 `Result<T>`（如 `String.asDate()`），调用方用 `getOrNull()` / `getOrThrow()` 消费；
- 默认时区为 `TimeZone.currentSystemDefault()`。

### 5.3 Android 工具类组织

所有工具均为 `object`（静态方法），按领域命名（`XxxUtils`）。`ReflectUtils` 为链式反射 API（唯一构造私有类）。部分工具类提供回调接口：

| 工具类 | 回调接口 / 内部类型 |
| --- | --- |
| `Utils` | `OnAppStatusChangedListener`（前后台切换）、`ActivityLifecycleCallbacks` |
| `CrashUtils` | `OnCrashListener`、`CrashInfo` |
| `KeyboardUtils` | `OnSoftInputChangedListener` |
| `FileIOUtils` | `OnProgressUpdateListener` |
| `FileUtils` | `OnReplaceListener` |
| `NetworkUtils` | `NetworkType`、`WifiScanResults` |
| `ThreadUtils` | `PoolType`（Single/Cached/Io/Cpu）、`OnTimeoutListener`、`SyncValue<T>`、`SimpleTask<Result>` |
| `VolumeUtils` | `StreamType` |
| `ImageUtils` | `ImageType` |
| `NotificationUtils` | `ChannelConfig` |

---

## 6. 配置说明（表格：字段/类型/必填/默认值/说明）

### 6.1 `Utils.init`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `app` | `Application` | 是 | Application 实例；重复调用幂等（相同实例直接返回） |

### 6.2 `TimeUtils` 常用方法默认值

| 方法 | 默认参数 | 说明 |
| --- | --- | --- |
| `getNowDateTimeString()` | `timeZone = 系统时区, format = yyyy-MM-dd HH:mm:ss` | 当前日期时间字符串 |
| `getNowDateString()` | `timeZone = 系统时区, format = yyyy-MM-dd` | 当前日期字符串 |
| `getNowTimeString()` | `timeZone = 系统时区, format = HH:mm:ss` | 当前时间字符串 |
| `asDateTime()` / `asDate()` / `asTime()` | `timeZone = 系统时区` | Long 时间戳转换 |
| `String.asDate()/asTime()/asDateTime()` | 对应默认 format | 字符串解析，返回 `Result` |
| `getChineseWeek()` / `getEnglishWeek()` / `getChineseMonth()` / `getEnglishMonth()` | `date = 今天` | 星期/月份文本 |
| `getConstellation()` / `getChineseZodiac()` | `date = 今天` | 星座 / 生肖 |
| `getFriendlyTimeSpanByNow()` | `timeZone = 系统时区` | 相对现在友好时间差 |
| `getFitTimeSpan(millis1, millis2)` | `timeZone = 系统时区` | 两时间点跨度描述 |

### 6.3 常量对象

| 对象 | 常量 | 值 |
| --- | --- | --- |
| `TimeConstants` | `MSEC / SEC / MIN / HOUR / DAY` | `1 / 1000 / 60000 / 3600000 / 86400000`（毫秒） |
| `MemoryConstants` | `BYTE / KB / MB / GB` | `1 / 1024 / 1048576 / 1073741824`（字节） |
| `CacheConstants` | `SEC / MIN / HOUR / DAY` | `1 / 60 / 3600 / 86400`（秒） |
| `RegexConstants` | 常用正则常量 | 见源码 `RegexConstants.kt` <!-- TODO: 待确认 —— 具体正则条目以源码为准 --> |
| `PermissionConstants`（Android） | 权限字符串常量 | 见源码 `PermissionConstants.kt` <!-- TODO: 待确认 --> |

---

## 7. API 参考

包结构：`org.muc.mold.utils`

- `org.muc.mold.utils.constant`：`TimeConstants` / `MemoryConstants` / `CacheConstants` / `RegexConstants`（commonMain）+ `PermissionConstants`（androidMain）
- `org.muc.mold.utils.util`：`TimeUtils`（commonMain）+ 全部 Android 工具类

### 7.1 `TimeUtils`（commonMain）

```kotlin
object TimeUtils
```

| 分类 | 方法签名 | 返回值 | 说明 |
| --- | --- | --- | --- |
| 当前时间 | `getNowDateTimeString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalDateTime> = dateTimeFormat): String` | `String` | 当前日期时间字符串 |
| 当前时间 | `getNowDateString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalDate> = dateFormat): String` | `String` | 当前日期字符串 |
| 当前时间 | `getNowTimeString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalTime> = timeFormat): String` | `String` | 当前时间字符串 |
| 当前时间 | `getNowDateTime(timeZone: TimeZone = defaultTimeZone): LocalDateTime` | `LocalDateTime` | 当前日期时间对象 |
| 当前时间 | `getNowDate(timeZone: TimeZone = defaultTimeZone): LocalDate` | `LocalDate` | 当前日期对象 |
| 当前时间 | `getNowTime(timeZone: TimeZone = defaultTimeZone): LocalTime` | `LocalTime` | 当前时间对象 |
| 当前时间 | `getNowMills(): Long` / `getNowSeconds(): Long` | `Long` | 当前毫秒 / 秒时间戳 |
| 时间戳转换 | `Long.asDateTime(timeZone: TimeZone = defaultTimeZone): LocalDateTime` | `LocalDateTime` | 毫秒 → 日期时间 |
| 时间戳转换 | `Long.asDate(...)` / `Long.asTime(...)` | `LocalDate` / `LocalTime` | 毫秒 → 日期 / 时间 |
| 字符串解析 | `String.asTime(format: DateTimeFormat<LocalTime> = timeFormat): Result<LocalTime>` | `Result<LocalTime>` | 解析时间字符串 |
| 字符串解析 | `String.asDate(...): Result<LocalDate>` / `String.asDateTime(...): Result<LocalDateTime>` | `Result<...>` | 解析日期 / 日期时间 |
| 时间差 | `LocalDateTime.periodUntil(other: LocalDateTime, timeZone: TimeZone = defaultTimeZone): DateTimePeriod` | `DateTimePeriod` | 时间差周期 |
| 时间差 | `DateTimePeriod.toChinese(showZero: Boolean = false): String` | `String` | 周期中文描述（如 "1天2小时"） |
| 时间差 | `getFitTimeSpan(millis1: Long, millis2: Long, timeZone: TimeZone = defaultTimeZone): String` | `String` | 两时间点跨度 |
| 时间差 | `LocalDateTime.getFitTimeSpanByNow(timeZone: TimeZone = defaultTimeZone): String` | `String` | 与现在跨度 |
| 友好时间 | `LocalDateTime.getFriendlyTimeSpanByNow(timeZone: TimeZone = defaultTimeZone): String` | `String` | "刚刚 / N分钟前 / N小时前 / N天前 / 日期" |
| 星期 | `getChineseWeek(date: LocalDate = getNowDate()): String` | `String` | "星期二" |
| 星期 | `getEnglishWeek(date: LocalDate = getNowDate()): String` | `String` | "Tuesday" |
| 月份 | `getChineseMonth(date: LocalDate = getNowDate()): String` | `String` | "九月" |
| 月份 | `getEnglishMonth(date: LocalDate = getNowDate()): String` | `String` | "September" |
| 天文 | `getConstellation(date: LocalDate = getNowDate()): String` | `String` | 星座（"天秤座"） |
| 天文 | `getChineseZodiac(date: LocalDate = getNowDate()): String` | `String` | 生肖（"马"） |
| 节日 | `LocalDate.getChineseFestival(): String` | `String` | 中国传统节日名（无则空串） |
| 其他 | `isLeapYear(date: LocalDate = getNowDate()): Boolean` | `Boolean` | 是否闰年 |

**示例**：

```kotlin
// 时间戳 → 友好时间
val span = 1_700_000_000_000L.asDateTime().getFriendlyTimeSpanByNow()
println(span)

// 解析失败安全消费
val parsed = "2026-09-22".asDate().getOrNull() ?: LocalDate.now()
```

### 7.2 `Utils`（Android）

```kotlin
object Utils {
    fun init(app: Application)
    val app: Application
    // 内部: sp: SharedPreferences
    interface OnAppStatusChangedListener { fun onForeground(activity: Activity?); fun onBackground(activity: Activity?) }
    class ActivityLifecycleCallbacks { /* onActivityCreated/Started/Resumed/Paused/Stopped/Destroyed/onLifecycleChanged */ }
    abstract class Task<Result>(consumer: ((Result?) -> Unit)?) : ThreadUtils.SimpleTask<Result>()
}
```

| 成员 | 说明 |
| --- | --- |
| `init(app)` | 初始化（必须） |
| `app` | 全局 Application（支持反射兜底） |

### 7.3 Android 工具类速查表（`org.muc.mold.utils.util`）

| 工具类 | 功能 | 典型方法（示意） |
| --- | --- | --- |
| `ActivityUtils` | Activity 管理 | `isActivityExists`、`startActivity`、`finishActivity` 等 |
| `AppUtils` | App 信息 | `getAppName`、`isAppInstalled`、`isAppRunning`、`getAppVersionName/Code` 等 |
| `AdaptScreenUtils` | 屏幕适配 | `adaptWidth/adaptHeight`、`preLoadRunnable` |
| `BarUtils` | 状态栏/导航栏 | `setStatusBarVisibility/Color`、`getStatusBarHeight`、`setNavBarColor` 等 |
| `BrightnessUtils` | 屏幕亮度 | `setBrightness`、`getBrightness`、`setWindowBrightness` 等 |
| `CleanUtils` | 缓存清理 | `cleanInternalCache`、`cleanExternalCache`、`cleanCustomCache` 等 |
| `ClipboardUtils` | 剪贴板 | `copyText`、`getText`、`addTextChangedListener` 等 |
| `ConvertUtils` | 单位转换 | 字节↔单位、dp↔px、数字进制、字符串转数字等 |
| `CrashUtils` | 崩溃捕获 | `init(crashDir, OnCrashListener)`、`getCrashFiles`、`CrashInfo` |
| `DeviceUtils` | 设备信息 | `isDeviceRooted`、`getSDKVersionName`、`getMacAddress`、`getManufacturer` 等 |
| `EncodeUtils` | 编解码 | `urlEncode/Decode`、`base64Encode/Decode` |
| `EncryptUtils` | 加解密 | `encryptMD5/SHA/...`、`encryptAES/DES/RSA`、`decrypt...` |
| `FileIOUtils` | 文件 IO | `writeFileFromString/IS/Bytes`、`readFile2String/List/Bytes`（含进度回调） |
| `FileUtils` | 文件操作 | `getFileByPath`、`createOrExistsFile/Dir`、`delete/rename/copy/move`、`listFilesInDir` |
| `FlashlightUtils` | 手电筒 | `setFlashlightEnabled`、`isFlashlightEnabled` |
| `ImageUtils` | 图像处理 | `bitmap2Bytes/Bytes2Bitmap`、`compressByScale/Quality`、`rotate/crop`、`getImageType` |
| `IntentUtils` | Intent 构造 | `getLaunchAppIntent`、`getDialIntent`、`getShareTextIntent`、`getSendSmsIntent` 等 |
| `KeyboardUtils` | 软键盘 | `showSoftInput`、`hideSoftInput`、`toggleSoftInput`、软键盘高度监听 |
| `LanguageUtils` | 多语言 | `applyLanguage`、`getLanguageType`、`isEnglish` 等 |
| `MetaDataUtils` | Manifest 元数据 | `getMetaDataInApp/Activity/Service` |
| `NetworkUtils` | 网络 | `isConnected`、`getNetworkType`、`isWifiConnected`、`getIPAddress`、`openWirelessSettings` 等 |
| `NotificationUtils` | 通知 | `areNotificationsEnabled`、`createChannel`（`ChannelConfig`）、`notify` 等 |
| `NumberUtils` | 数字 | 格式化、进制、随机数等 |
| `PathUtils` | 路径 | `getRootPath`、`getDataPath`、`getInternalAppDataPath`、`getExternalApp...Path` 等 |
| `PermissionUtils` | 权限 | `isGranted`、`launchAppDetailsSettings`、运行时权限申请封装 |
| `PhoneUtils` | 手机信息 | `getIMEI/IMSI`、`getPhoneType`、`getSimOperatorName`、`isSimCardReady` 等 |
| `ProcessUtils` | 进程 | `getForegroundProcessName`、`killAllBackgroundProcesses`、`currentProcessName` 等 |
| `ReflectUtils` | 反射链式 | `reflect(name)` → `field/method` 链式调用，`ReflectException` |
| `RegexUtils` | 正则校验 | `isMobileSimple/Exact`、`isEmail`、`isIDCard18`、`isUrl` 等 |
| `ResourceUtils` | 资源读取 | `readAssets2String`、`readRaw2String`、`copyAssetsDir` 等 |
| `RomUtils` | ROM 厂商 | `isXiaomi/EMUI/ColorOS/Flyme/Samsung`、`getRomVersion` 等 |
| `ScreenUtils` | 屏幕 | `getScreenWidth/Height`、`setFullScreen`、`setLandscape/Portrait`、`isScreenLock` 等 |
| `SDCardUtils` | SD 卡 | `isSDCardEnable`、`getSDCardPath`、`getSDCardInfo` 等 |
| `ServiceUtils` | 服务 | `getAllRunningService`、`isServiceRunning`、`startService`、`stopService` 等 |
| `ShellUtils` | Shell 命令 | `execCmd(cmd, isRoot, isNeedResultMsg)`、`CommandResult` |
| `StringUtils` | 字符串 | `isEmpty`、`isSpace`、`equals`、`getString`、`toDBC` 等 |
| `ThreadUtils` | 线程池 | `executeBySingle/Cached/Io/Cpu`、`SimpleTask`、`SyncValue`、超时控制 |
| `ThrowableUtils` | 异常栈 | `getFullStackTrace`、`getRootCause` 等 |
| `UriUtils` | URI | `getUriForFile`、`getUriForResources`、`file2Uri` 等 |
| `VibrateUtils` | 震动 | `vibrate(ms)`、`vibrate(pattern, repeat)`、`cancel()` |
| `VideoUtils` | 视频 | 视频信息、时长、帧抽取等 |
| `VolumeUtils` | 音量 | `getVolume`、`setVolume`、`getMaxVolume`（按 `StreamType`） |
| `ZipUtils` | 压缩 | `zipFiles`、`unzipFile`、`getFilesPath`、`getComments` 等 |

> 说明：以上为按源码目录归纳的能力清单，具体方法签名请查阅对应源码或 IDE 补全。个别方法为平台受限（如 `PhoneUtils.getIMEI` 需权限 / API 级别支持）。

---

## 8. 错误码表

**无自定义错误码。**

| 失败场景 | 表现 |
| --- | --- |
| `Utils.app` 反射初始化失败 | 抛 `NullPointerException("reflect failed.")` |
| `TimeUtils` 解析失败 | 返回 `Result.failure`（如 `String.asDate()`），不抛异常 |
| 需要权限的工具方法（IMEI、读写存储等） | 抛出 `SecurityException` 或返回空值，需调用方先申请权限（`PermissionUtils`） |
| Shell 命令失败 | `ShellUtils.CommandResult` 携带 `result` / `successMsg` / `errorMsg`，由调用方判断 |

---

## 9. 常见场景示例（至少 3 个：认证、分页/批量、错误处理）

### 9.1 认证：Token 过期时间判断

```kotlin
import org.muc.mold.utils.util.TimeUtils

// 判断 token 是否过期（服务器下发过期时间戳）
fun isTokenExpired(expireAt: Long): Boolean {
    return expireAt <= TimeUtils.getNowMills()
}

// 展示剩余有效期
fun remainingLabel(expireAt: Long): String {
    val span = expireAt.asDateTime().getFitTimeSpanByNow()  // 或 periodUntil
    return "剩余 $span"
}
```

### 9.2 批量：缓存清理（Android）

```kotlin
import org.muc.mold.utils.util.CleanUtils
import org.muc.mold.utils.util.Utils

fun cleanAllCache(onDone: () -> Unit) {
    Utils.app?.let {
        CleanUtils.cleanInternalCache(it)
        CleanUtils.cleanExternalCache(it)
        CleanUtils.cleanCustomCache(it.cacheDir)
        onDone()
    }
}
```

### 9.3 错误处理：崩溃日志收集（Android）

```kotlin
import org.muc.mold.utils.util.CrashUtils
import org.muc.mold.utils.util.Utils
import java.io.File

fun initCrashCapture() {
    CrashUtils.init(
        crashDir = File(Utils.app.cacheDir, "crash"),
        onCrashListener = object : CrashUtils.OnCrashListener {
            override fun onCrash(crashInfo: CrashUtils.CrashInfo) {
                // 上报崩溃（异步）
                upload(crashInfo.throwable, crashInfo.time)
            }
        },
    )
}
```

---

## 10. 常见问题 FAQ（至少 5 条）

**Q1：不调用 `Utils.init()` 直接用 `Utils.app` 会怎样？**

`Utils.app` 会尝试反射获取 Application 自动初始化；若反射失败则抛 `NullPointerException("reflect failed.")`。强烈建议在 `Application.onCreate()` 中显式调用 `Utils.init(this)`。

**Q2：`TimeUtils` 为什么不用 `SimpleDateFormat` / `java.util.Date`？**

`TimeUtils` 面向 Kotlin Multiplatform（commonMain），`java.time` 在部分平台不可用，因此统一基于 `kotlinx-datetime` 实现，保证 Android / iOS / JVM 行为一致，且格式化使用线程安全的 Builder DSL。

**Q3：`String.asDate()` 解析失败会崩溃吗？**

不会。`asDate()` / `asTime()` / `asDateTime()` 返回 `Result<T>`，失败时返回 `Result.failure`，需用 `getOrNull()` / `getOrThrow()` / `getOrDefault()` 消费。

**Q4：iOS / 桌面端能用 `FileUtils`、`NetworkUtils` 吗？**

不能。这些工具类在 `androidMain` 中，只有 Android 目标可见。跨平台代码只能使用 `TimeUtils` 与常量对象；若需要跨平台文件/网络能力，请使用 `dataKV` / `dataRequest` 模块或自行封装 expect/actual。

**Q5：`CleanUtils.cleanInternalCache` 会删除应用数据吗？**

不会。它只清理缓存目录（`cacheDir`），不会删除数据库、SharedPreferences、文件目录等业务数据。`cleanInternalDbs` / `cleanInternalSp` 等按需调用。

**Q6：权限相关方法为什么拿不到结果？**

运行时权限（如定位、相机、存储）需要先通过 `PermissionUtils` 申请并等待授权回调，且部分 API（如 `getIMEI`）在 Android 10+ 有硬性限制，返回空或抛异常属预期行为，请做空值兜底。

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
