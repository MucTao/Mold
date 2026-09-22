# Mold UI

## SDK 基本信息

| 项目 | 内容 |
| --- | --- |
| 名称 | Mold UI（`ui`） |
| 用途 | 基于 Compose Multiplatform 的企业级 UI 组件库：统一主题体系、按钮/输入/弹窗/选择器等通用组件、反馈系统、导航与相机/截图等能力 |
| 语言 / 平台 | Kotlin Multiplatform（Android / iOS / Desktop-JVM），Compose Multiplatform 1.11.1 |
| 包管理器与安装命令 | Gradle（Maven / JitPack），详见[安装](#3-安装) |
| 当前版本 | `1.1.22` |
| License | MIT <!-- TODO: 待确认 —— 仓库内暂未发现 LICENSE 文件，发布前请补充并核对许可证文本 --> |
| 目标读者 | 使用 Compose Multiplatform 构建界面、需要统一主题与组件库的 UI 工程师 |

> 坐标：`com.github.MucTao.Mold:ui:1.1.22`

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

**Mold UI** 是 Mold 生态的 Compose Multiplatform 组件库，目标是"开箱即用的统一视觉与交互"。它提供：

- **统一主题体系**：`MoldTheme` 一键注入色彩（Material You 动态取色）、排版、圆角与语义色，并内置全局 Toast / Snackbar 宿主。
- **完整组件族**：
  - 按钮：`MoldFilledButton` / `MoldOutlinedButton` / `MoldPlainButton`（含图标、加载态、尺寸体系）；
  - 输入：填充 / 轮廓 / 朴素三种文本输入，下拉选择、自动补全、树形选择、紧凑搜索框；
  - 弹层：`MoldAlertDialog`、`MoldBanner`、`ConfirmDialog`、`FullScreenSheet`、Tooltip；
  - 选择：分段按钮、分割按钮、菜单按钮、多选、多选清单、树勾选；
  - 反馈：`FeedbackManager`（Toast + Snackbar 统一队列）、`ActionManager`（可撤销动作）；
  - 状态：`LoadingView` / `EmptyView` / `ErrorView`；
  - 时间：时间范围选择、日排期选择、`DayScheduleChart` 排期图、日期时间拾取。
- **增强能力**：`Navigator` + `NavigationState`（Navigation3 封装）、`CameraManager`（拍照/录像）、`CaptureController`（截图）、颜色选择器、拖拽悬浮、图片加载封装（Coil）、自适应窗口宽度。
- **跨平台一致**：同一套代码覆盖 Android / iOS / Desktop；相机、截图等平台能力通过 `expect/actual` 隔离。

---

## 2. 环境要求（兼容性矩阵表格）

| 项目 | 要求 |
| --- | --- |
| Kotlin | ≥ 2.4.10 |
| Android compileSdk | 37 |
| Android minSdk | 21 |
| JVM 目标 | 11 |
| Compose Multiplatform | 1.11.1（ui / foundation / material3 1.11.0-alpha07 / runtime） |
| Material Icons Extended | 1.7.3 |
| Navigation3 | 1.1.1 |
| Coil（图片加载） | 3.5.0（compose + network-ktor3） |
| material-kolor（动态取色） | 5.0.0 |
| CameraX（Android 相机） | 1.5.0 |
| reorderable（拖拽排序） | 3.1.0 |
| Ktor | 3.5.2 |

**平台目标矩阵**：

| 平台 | 支持 | 平台差异说明 |
| --- | --- | --- |
| Android | ✅ | 相机 CameraX、MoldImage 走 Coil/本地、CaptureController 走 View 截图 |
| iOS（iosArm64 / iosSimulatorArm64） | ✅ | 相机走 iOS API、MoldImage 走 iOS 实现 |
| Desktop（JVM） | ✅ | 相机能力受限（`CameraManager.jvm`）、截图走 AWT |
| Web（Wasm/JS） | ❌ | 当前未配置 |

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
    implementation("com.github.MucTao.Mold:ui:1.1.22")
}
```

### 3.3 本地仓库（可选）

```kotlin
repositories {
    maven { url = uri("F:/Android/WorkSpace/repo") }
}
```

> 提示：模块 `api` 暴露了 Navigation3 / material3-adaptive / lifecycle-viewmodel-navigation3 / reorderable，使用方可直接使用这些 API。

---

## 4. 快速开始（含最小可运行示例 + 预期输出）

### 4.1 最小应用骨架

```kotlin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.design.MoldTheme

@Composable
fun App() {
    MoldTheme {                       // 自动注入主题 + Toast/Snackbar 宿主
        MoldFilledButton(
            text = "点击我",
            onClick = { println("按钮被点击") },
            size = MoldButtonSize.MEDIUM,
        )
    }
}
```

### 4.2 主题切换（深色/动态取色）

```kotlin
@Composable
fun ThemedApp() {
    var dark by remember { mutableStateOf(false) }
    MoldTheme(
        seedColor = MoldBlue,          // 种子色
        isDynamic = true,              // 启用 Material You 动态取色（受支持平台）
        isDarkTheme = dark,            // 深浅色
        toastAlignment = Alignment.BottomCenter,
    ) {
        // 业务内容
        Switch(checked = dark, onCheckedChange = { dark = it })
    }
}
```

### 4.3 预期输出

- 页面渲染出主题化按钮，点击控制台输出 `按钮被点击`；
- 主题切换开关时，按钮/背景颜色随种子色与明暗模式变化；
- 调用 `FeedbackManager.showToast("...")` 时，Toast 出现在 `MoldTheme` 底部宿主。

---

## 5. 核心概念

### 5.1 主题体系（`org.muc.ui.design`）

```text
MoldTheme
 ├─ ColorScheme：Material You（rememberDynamicColorScheme / dynamicColor）
 ├─ MoldSemanticColors：业务语义色（LocalMoldSemanticColors）
 ├─ Typography：MoldTypography
 ├─ Shapes：MoldCornerRadius（XSMALL..XLARGE）
 ├─ WindowWidthType：自适应窗口宽度（LocalWindowWidthType）
 └─ 全局宿主：ToastHost + SnackBarHost
```

- `MoldTheme` 是应用的根主题，所有组件默认跟随其语义色/圆角/排版。
- 组件内部通过 `MoldTheme.colors` / `MoldTheme.typography` / `MoldTheme.shapes` / `MoldTheme.windowWidthType` 访问上下文资源。

### 5.2 反馈系统（`org.muc.ui.action.feedback`）

- `FeedbackManager`：进程内单例，管理 Toast 队列与 Snackbar 状态。`showToast(message, type, duration)` 入队（最多同时 3 条，超出移除最旧）；`showSnackbar(message, type, actionLabel, onAction, duration)` 展示可交互 Snackbar。
- `FeedBackType`：`PRIMARY / INFO / SUCCESS / ERROR / WARNING`。
- `ToastHost` / `SnackBarHost`：由 `MoldTheme` 自动挂载，无需手动添加。

### 5.3 动作系统（`org.muc.ui.action`）

- `ActionManager`：管理可撤销/可确认动作（如删除、覆盖确认），`ActionView` 渲染动作请求弹窗。
- `Action` 密封类 + `BaseActionState`：定义动作内容（`msg` 为 `@Composable () -> String`，支持富文本/多语言）。

### 5.4 导航（`org.muc.ui.navigation`）

- 基于 JetBrains `Navigation3`：`NavKey` 作为目的地标识（如 `object Home : NavKey`，需 `@Serializable` 并注册多态 Serializer 以支持状态恢复）。
- `rememberNavigationState(startKey, topLevelKeys, serializers)` 创建导航状态；`Navigator(state, onNavigateToRestrictedKey, isLoggedIn)` 封装导航动作（含登录拦截）。
- `EntryProviderInstaller` / `baseEntryProvider` 将 `NavKey` 映射为 `NavEntry`（页面）。

### 5.5 相机与截图（`org.muc.ui.camera` / `org.muc.ui.snapshot`）

- `createCameraManager()`（expect/actual）：`Preview()` 预览、`takePhoto` / `startRecording` / `stopRecording` / `release`。
- `rememberCaptureController()` + `Modifier.capture(controller)`：截取视图为位图，`ImageBitmap.toBase64(quality)` 导出。

---

## 6. 配置说明（表格：字段/类型/必填/默认值/说明）

### 6.1 `MoldTheme`

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `seedColor` | `Color` | 否 | `MoldBlue` | 动态取色种子色 |
| `isDynamic` | `Boolean?` | 否 | `null` | `true` 启用平台动态取色；`null` 不使用 |
| `isDarkTheme` | `Boolean` | 否 | `isSystemInDarkTheme()` | 深色模式 |
| `toastAlignment` | `Alignment` | 否 | `BiasAlignment(0f, .7f)` | Toast 宿主对齐位置 |
| `toastContent` | `@Composable (ToastData) -> Unit` | 否 | `ToastItem(it)` | Toast 自定义渲染 |
| `snackbarAlignment` | `Alignment` | 否 | `Alignment.BottomEnd` | Snackbar 宿主对齐位置 |
| `snackbarContent` | `@Composable (SnackbarData) -> Unit` | 否 | `SnackBar(...)` | Snackbar 自定义渲染 |
| `content` | `@Composable () -> Unit` | 是 | — | 业务内容 |

### 6.2 `FeedbackManager`

| 方法/字段 | 参数 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `showToast` | `message: String, type: FeedBackType, duration: Duration` | `INFO`, `2.seconds` | 弹 Toast（队列最多 3 条） |
| `showSnackbar` | `message, type, actionLabel, onAction, duration` | `INFO`, `null`, `null`, `null` | 弹 Snackbar；`duration=null` 不自动消失 |
| `toasts` | — | — | 当前 Toast 列表（只读） |
| `snackbar` | — | — | 当前 Snackbar（只读） |
| `dismissSnackbar` | — | — | 手动关闭 Snackbar |

### 6.3 `Navigator`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `state` | `NavigationState` | 是 | 导航状态 |
| `onNavigateToRestrictedKey` | `(NavKey?) -> NavKey` | 是 | 未登录时跳转的登录页 key |
| `isLoggedIn` | `() -> Boolean` | 是 | 登录态判定 |

### 6.4 通用组件默认值速查

| 组件 | 关键默认值 |
| --- | --- |
| `MoldFilledButton` / `MoldOutlinedButton` / `MoldPlainButton` | `type = MoldButtonType.PRIMARY`，`size = MoldButtonSize.MEDIUM` |
| `MoldFilledTextField` / `MoldOutlinedTextField` / `MoldPlainTextField` | `type = PRIMARY`，`size = MEDIUM`，`cornerRadius = MEDIUM`，`singleLine = true` |
| `MoldSingleSegmentedButtons` | `size = MEDIUM`，`cornerRadius = MEDIUM` |
| `MoldSplitButton` | `size = MEDIUM` |
| `MoldBanner` | 见 `MoldBannerType`（INFO/SUCCESS/WARNING/ERROR 等） |
| `MoldSectionCard` | `titleColor = colorScheme.primary`，`titleTextStyle = labelSmall` |
| `CompactTextField` | 无建议词时用 `MoldOutlinedTextField`，有建议词时用 `MoldOutlinedAutocompleteTextField` |

---

## 7. API 参考

包结构：`org.muc.ui.*`（全部组件均以 `Mold` 前缀命名，保持命名一致性）

### 7.1 设计体系（`org.muc.ui.design`）

#### `MoldTheme`

```kotlin
@Composable
fun MoldTheme(
    seedColor: Color = MoldBlue,
    isDynamic: Boolean? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    toastAlignment: Alignment = BiasAlignment(0f, .7f),
    toastContent: @Composable (ToastData) -> Unit = { ToastItem(it) },
    snackbarAlignment: Alignment = Alignment.BottomEnd,
    snackbarContent: @Composable (SnackbarData) -> Unit = { SnackBar(contentAlignment = snackbarAlignment, bar = it) },
    content: @Composable () -> Unit,
)
```

根主题。内部使用 `MoldTheme.colors` / `MoldTheme.typography` / `MoldTheme.shapes` / `MoldTheme.windowWidthType` 提供上下文。

| 对象成员 | 说明 |
| --- | --- |
| `MoldTheme.colors` | 语义色（`LocalMoldSemanticColors.current`，`MoldSemanticColors`） |
| `MoldTheme.colorScheme` / `typography` / `shapes` | 透传 MaterialTheme |
| `MoldTheme.windowWidthType` | 窗口宽度类型（`WindowWidthType`：COMPACT/MEDIUM/EXPANDED 等，含 `col` 网格数） |

#### `MoldCornerRadius`

```kotlin
enum class MoldCornerRadius(val value: Dp, val shape: CornerBasedShape)
```

取值：`XSMALL / SMALL / MEDIUM / LARGE / XLARGE`，每个值同时携带 Dp 与 `CornerBasedShape`，可传给组件的 `cornerRadius` 参数。

#### 颜色工具扩展（`MoldColors.kt`）

```kotlin
fun Color.lighten(ratio: Float): Color
fun Color.darken(ratio: Float): Color
fun Color.harmonizeWith(other: Color, matchSaturation: Boolean = false): Color
fun Color.rand(other: Color, matchSaturation: Boolean = false): Color
fun randomVividColor(): Color
fun stableRandomColor(seed: Int): Color
fun Color.adapterIsDark(ratio: Float, isDark: Boolean?): Color
```

#### 其他

| 成员 | 说明 |
| --- | --- |
| `object Dimensions` | 统一间距/图标尺寸常量 |
| `object MoldTypography` | 排版体系 |
| `class CornerLineShape` | 拐角连线形状 |
| `enum class WindowWidthType(val col: Int)` | 窗口宽度类型（含网格列数） |
| `object MoldCommonStringRes` | 公共字符串资源（i18n） |

### 7.2 按钮（`org.muc.ui.buttons`）

```kotlin
@Composable
fun MoldFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
)

@Composable
fun MoldOutlinedButton(/* 同 MoldFilledButton 参数结构 */)

@Composable
fun MoldPlainButton(/* 同 MoldFilledButton 参数结构 */)
```

| 参数 | 说明 |
| --- | --- |
| `text` | 按钮文本 |
| `onClick` | 点击回调 |
| `enabled` | 是否可用 |
| `loading` | 加载态（显示进度） |
| `size` | `MoldButtonSize`：SMALL/MEDIUM/LARGE 等 |
| `leadingIcon` / `trailingIcon` | 首尾图标 |
| `cornerRadius` | 圆角 |

**模型**：

```kotlin
enum class MoldButtonType { PRIMARY, SECONDARY, DANGER, ... }   // 按钮类型
enum class MoldButtonSize(...)                                   // 尺寸
```

> 说明：`MoldButtonType` / `MoldButtonSize` 具体枚举值以源码 `MoldButtonsModels.kt` 为准，此处按编译产物列出类型。 <!-- TODO: 待确认 —— 枚举完整取值请以 MoldButtonsModels.kt 为准 -->

### 7.3 文本输入（`org.muc.ui.textfields`）

三个同构输入框（FILLED / OUTLINED / PLAIN 三种样式）：

```kotlin
@Composable
fun MoldFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
)

// MoldOutlinedTextField / MoldPlainTextField 签名结构一致
```

**下拉与自动补全**：

```kotlin
@Composable
fun <T> MoldDropdownTextField(
    value: T?,
    options: List<MoldDropdownOption<T>>,
    onValueSelected: (T) -> Unit,
    /* ... 同输入框参数 */
)

@Composable
fun MoldOutlinedAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldDropdownOption<String>>,
    onSuggestionSelected: (String) -> Unit,
    maxVisibleSuggestions: Int = ...,
    /* ... */
)

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suggestions: List<String> = emptyList(),
    modifier: Modifier = Modifier,
)
```

**树形选择**：`MoldTreeDropdownTextField`、`MoldTreeSelect`、`MoldTreeCheckView`、`MoldAutocompleteTreeTextFields`、`FlatTreeItem<T>`。

**模型**：

```kotlin
enum class MoldTextFieldType { PRIMARY, NEUTRAL, DANGER, SUCCESS, ... }   // 颜色语义
enum class MoldTextFieldSize(...)                                          // 尺寸
data class MoldDropdownOption<T>(val value: T, val label: String, val enabled: Boolean = true, ...)
data class MoldTreeDropdownOption<T>(...)
```

> 说明：枚举完整取值以 `MoldTextFieldsModels.kt` 源码为准。 <!-- TODO: 待确认 -->

### 7.4 弹窗与横幅（`org.muc.ui.alertdialogs` / `org.muc.ui.banner` / `org.muc.ui.action`）

```kotlin
@Composable
fun MoldAlertDialog(
    title: String,
    message: String? = null,
    confirmAction: MoldAlertDialogAction,
    dismissAction: MoldAlertDialogAction? = null,
    loading: Boolean = false,
    onDismissRequest: () -> Unit,
    /* ... */
)

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确定",
    dismissText: String = "取消",
    /* ... */
)

@Composable
fun MoldBanner(
    text: String,
    type: MoldBannerType = MoldBannerType.INFO,
    dismissStyle: MoldBannerDismissStyle = ...,
    onDismiss: (() -> Unit)? = null,
    /* ... */
)
```

- `MoldAlertDialogAction`：`data class MoldAlertDialogAction(text, onClick, enabled, ...)`。
- `MoldBannerType`：横幅类型（INFO/SUCCESS/WARNING/ERROR 等）；`MoldBannerDismissStyle`：关闭样式。
- `MoldBanner` 提供两个重载（带/不带操作区）。

### 7.5 分段 / 分割 / 菜单按钮

```kotlin
@Composable
fun <T> MoldSingleSegmentedButtons(
    options: List<MoldSegmentedOption<T>>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: MoldSegmentedButtonSize = MoldSegmentedButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    colors: MoldSegmentedButtonColors = MoldSegmentedButtonDefaults.colors(),
)

@Composable
fun <T> MoldSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menuItems: List<MoldSplitMenuItem<T>>,
    onMenuItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: MoldSplitButtonSize = MoldSplitButtonSize.MEDIUM,
    /* ... */
)

@Composable
fun <T> MoldOutlinedMenuButton(
    text: String,
    options: List<MoldMenuButtonOption<T>>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector = Icons.Outlined.KeyboardArrowDown,
    selectedOption: T? = null,
    showSelectedCheckmark: Boolean = false,
    contentDescription: String? = null,
)
```

模型：`MoldSegmentedOption<T>`、`MoldSegmentedButtonSize`、`MoldSegmentedButtonColors`、`MoldSegmentedButtonDefaults`、`MoldSplitMenuItem<T>`、`MoldSplitButtonSize`、`MoldSplitButtonColors`、`MoldSplitButtonDefaults`、`MoldMenuButtonOption<T>`。

### 7.6 分区卡片 / 多选 / 状态视图

```kotlin
@Composable
fun MoldSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleUppercase: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    titleTextStyle: TextStyle = MaterialTheme.typography.labelSmall,
    /* content: @Composable ColumnScope.() -> Unit */
)

@Composable
fun <T> MoldMultiSelection(
    state: MoldMultiSelectionState<T>,
    /* ... */
)

@Composable
fun LoadingView(modifier: Modifier = Modifier.fillMaxSize(), message: String? = null)
@Composable
fun EmptyView(message: String, modifier: Modifier = Modifier.fillMaxSize())
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier.fillMaxSize())
```

- `MoldListCheckView`：列表勾选视图。
- `MoldMultiSelectionState<T>`：多选状态数据类。

### 7.7 反馈与动作（`org.muc.ui.action.feedback` / `org.muc.ui.action`）

#### `FeedbackManager`

```kotlin
object FeedbackManager {
    val toasts: List<ToastData>
    val snackbar: SnackbarData?
    fun showToast(message: String, type: FeedBackType = FeedBackType.INFO, duration: Duration = 2.seconds)
    fun showSnackbar(message: String, type: FeedBackType = FeedBackType.INFO, actionLabel: String? = null, onAction: (() -> Unit)? = null, duration: Duration? = null)
    fun dismissSnackbar()
}

enum class FeedBackType { PRIMARY, INFO, SUCCESS, ERROR, WARNING }
data class ToastData(val message: String, val type: FeedBackType = FeedBackType.INFO, val duration: Duration = 2.seconds)
data class SnackbarData(val message: String, val type: FeedBackType = FeedBackType.INFO, val actionLabel: String? = null, val onAction: (() -> Unit)? = null, val duration: Duration? = null)
```

#### `ActionManager` / `Action`

```kotlin
interface ActionManager { /* 注册/执行/撤销动作 */ }
class ActionManagerImpl : ActionManager

sealed class Action(open val msg: @Composable () -> String) { /* 具体动作子类 */ }
interface ActionContentScope<T> { /* 动作内容 DSL */ }
data class BaseActionState(/* 动作状态 */)

@Composable
fun ActionView(actionManager: ActionManager)
```

### 7.8 导航（`org.muc.ui.navigation`）

```kotlin
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
    serializers: SerializersModule,
): NavigationState

class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey
    val topLevelKeys: Set<NavKey>
    val currentSubStack: NavBackStack<NavKey>
    val currentKey: NavKey
    val currentIsTopLevel: Boolean
}

class Navigator(
    val state: NavigationState,
    private val onNavigateToRestrictedKey: (targetKey: NavKey?) -> NavKey,
    private val isLoggedIn: () -> Boolean,
) {
    fun navigate(key: NavKey)
    fun goBack()
    fun backTo(key: NavKey)
    fun isTopLevel(key: NavKey): Boolean
}

typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit
fun baseEntryProvider(list: Set<EntryProviderInstaller>): (NavKey) -> NavEntry<NavKey>
fun rememberListDetailStrategy(): ListDetailSceneStrategy<NavKey>
@Composable fun NavigationState.toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): SnapshotStateList<NavEntry<NavKey>>
```

| 方法 | 说明 |
| --- | --- |
| `navigate(key)` | 导航：未登录走登录拦截；目标为当前顶层则清子栈；为顶层则切换；否则压子栈 |
| `goBack()` | 返回上一页 |
| `backTo(key)` | 回退到指定 key（清除其上的临时页） |

### 7.9 相机（`org.muc.ui.camera`）

```kotlin
interface CameraManager {
    @Composable fun Preview(modifier: Modifier = Modifier)
    suspend fun takePhoto(onFail: (String) -> Unit): String?
    suspend fun startRecording(onFail: (String) -> Unit, onSuccess: (suspend () -> String) -> Unit)
    fun stopRecording()
    fun release()
}

expect fun createCameraManager(): CameraManager

@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    /* ... */
)
```

### 7.10 截图（`org.muc.ui.snapshot`）

```kotlin
@Composable
fun rememberCaptureController(): CaptureController
fun Modifier.capture(controller: CaptureController, onDrawRecord: (() -> Unit)? = null): Modifier
fun ImageBitmap.toBase64(quality: Int = 100): String
```

**示例**：

```kotlin
val controller = rememberCaptureController()
Box(Modifier.capture(controller)) { /* 被截取内容 */ }
// 触发截图后导出
val base64 = controller.captureBitmap().toBase64()
```

> 说明：`CaptureController` 内部持有 `GraphicsLayer` 与作用域，截图调用方式以源码 `CaptureController.kt` 为准。 <!-- TODO: 待确认 -->

### 7.11 颜色选择器（`org.muc.ui.colorpicker`）

```kotlin
@Composable
fun ColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    /* ... */
)
@Composable
fun ColorPickerPopup(/* 弹出式 */)
@Composable
fun ColorSliderBar(/* 滑动条 */)
@Composable
fun HexColorTextField(value: String, onValueChange: (String) -> Unit, /* ... */)
@Composable
fun SaturationValuePanel(/* 饱和度/明度面板 */)
data class ColorPickerState(/* 状态 */)
fun Color.toHsv(): FloatArray
fun Int.toHexColor(supportAlpha: Boolean = true): String
```

### 7.12 时间组件（`org.muc.ui.time`）

```kotlin
data class TimeRange(val start: LocalTime, val end: LocalTime)

@Composable
fun TimeRangePickerDialog(
    /* 时间范围选择，配合 rememberTimeRangePickerDialogState() */
)
fun TimeRangePickerDialogState.show(range: TimeRange, index: Int = -1)
fun TimeRangePickerDialogState.hide()

data class DaySchedule(/* 排期模型 */)
@Composable
fun DaySchedulePickerDialog(/* 排期选择 */)
fun DayScheduleDialogState.show(schedule: DaySchedule)

@Composable
fun DayScheduleChart(/* 排期图表 */)

// org.muc.ui.time.datetime
@Composable
fun DateTimePicker(/* 日期时间拾取 */)
fun LocalDateTime.Companion.now(): LocalDateTime
fun LocalDate.Companion.now(): LocalDate
```

### 7.13 其他组件

| 组件 | 包 | 说明 |
| --- | --- | --- |
| `DraggableContent` | `org.muc.ui.floatdrag` | `BoxScope` 内可拖拽悬浮内容 |
| `MoldImage` | `org.muc.ui.image` | 跨平台图片加载（expect/actual，Android/桌面走 Coil 或本地实现） |
| `MoldListCheckView` | `org.muc.ui.list` | 列表勾选视图 |
| `FullScreenSheet` | `org.muc.ui.sheet` | 全屏底部弹层 |
| `MoldTooltipArea` | `org.muc.ui.tooltiparea` | `expect fun MoldTooltipArea(text, modifier, content)` 悬停提示区域 |
| `MoldMenuButtons` 系列 | `org.muc.ui.menubuttons` | 菜单按钮 |
| `MoldSegmentedButtons` 系列 | `org.muc.ui.segmentedbuttons` | 分段选择 |
| `MoldMultiSelection` | `org.muc.ui.selection` | 多选 |
| `MoldAlertDialogs` 系列 | `org.muc.ui.alertdialogs` | 警告弹窗 |

---

## 8. 错误码表

**无自定义错误码。**

| 失败场景 | 表现 |
| --- | --- |
| 未在 `MoldTheme` 内使用组件 | 部分组件访问 `MaterialTheme` 时报错或回退默认样式 |
| 访问 `LocalNavigator` 但未提供 | 抛 `IllegalStateException("No LocalNavigator found!")` |
| 相机初始化失败 | `takePhoto` / `startRecording` 的 `onFail: (String) -> Unit` 回调错误信息 |
| 图片加载失败 | `MoldImage` 内部容错（显示占位） |

---

## 9. 常见场景示例（至少 3 个：认证、分页/批量、错误处理）

### 9.1 认证：登录页 + 未登录导航拦截

```kotlin
import org.muc.ui.navigation.*

@Serializable
object Login : NavKey
@Serializable
object Home : NavKey

// 1) 构造导航
val state = rememberNavigationState(
    startKey = Home,
    topLevelKeys = setOf(Home, Settings),
    serializers = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Home.serializer())
            subclass(Login.serializer())
            subclass(Settings.serializer())
        }
    },
)
val navigator = remember {
    Navigator(
        state = state,
        onNavigateToRestrictedKey = { Login },      // 未登录 -> 登录页
        isLoggedIn = { session.isLoggedIn },
    )
}
CompositionLocalProvider(LocalNavigator provides navigator) {
    // 2) 业务页内导航
    Button(onClick = { navigator.navigate(Profile) }) { Text("个人中心") }
}
```

### 9.2 批量：表单校验 + 分段选择 + 多选

```kotlin
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.segmentedbuttons.MoldSingleSegmentedButtons
import org.muc.ui.segmentedbuttons.MoldSegmentedOption
import org.muc.ui.textfields.MoldOutlinedTextField
import org.muc.ui.action.feedback.FeedbackManager

@Composable
fun BatchForm() {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("L1") }

    MoldOutlinedTextField(value = name, onValueChange = { name = it }, placeholder = "姓名")
    MoldSingleSegmentedButtons(
        options = listOf(
            MoldSegmentedOption("L1", "一级"),
            MoldSegmentedOption("L2", "二级"),
        ),
        selectedValue = level,
        onValueSelected = { level = it },
    )
    MoldFilledButton(
        text = "批量提交",
        size = MoldButtonSize.LARGE,
        onClick = {
            FeedbackManager.showToast("已提交 $name / $level", FeedBackType.SUCCESS)
        },
    )
}
```

### 9.3 错误处理：状态视图 + 重试

```kotlin
import org.muc.ui.status.*

@Composable
fun OrderPage(state: PageState) {
    when (state) {
        is Loading -> LoadingView(message = "加载中...")
        is Empty -> EmptyView(message = "暂无订单")
        is Error -> ErrorView(message = state.msg, onRetry = { viewModel.retry() })
        is Data -> OrderList(state.orders)
    }
}
```

---

## 10. 常见问题 FAQ（至少 5 条）

**Q1：为什么我的组件颜色不对/没有统一风格？**

所有 Mold 组件都读取 `MoldTheme` 提供的语义色与排版。请确认组件位于 `MoldTheme { ... }` 内部，且未在外部覆盖 `LocalMoldSemanticColors`。

**Q2：`FeedbackManager.showToast` 之后屏幕上没有出现 Toast？**

Toast 由 `MoldTheme` 内部的 `ToastHost` 渲染。若你的根布局没有使用 `MoldTheme`，或业务内容覆盖在宿主之上（如全屏 Dialog），Toast 可能不可见。请确保 `MoldTheme` 包裹业务内容，并检查 `toastAlignment` 是否被遮挡。

**Q3：`Navigator.navigate` 没反应 / 被跳去登录页？**

`navigate` 内部会先调用 `isLoggedIn()`，返回 `false` 时跳转到 `onNavigateToRestrictedKey` 返回的 key。检查登录态判定与拦截回调。

**Q4：`MoldDropdownTextField` 为什么泛型选项收不到选中值？**

选项模型是 `MoldDropdownOption<T>(value, label, enabled)`，回调 `onValueSelected: (T) -> Unit` 回调的是 `option.value` 而非 `label`。确认你传入的 `value` 与展示文案分离正确。

**Q5：iOS/桌面端 `CameraManager` 能用吗？**

`CameraManager` 是三平台 expect/actual 实现。iOS 使用原生相机 API，桌面（JVM）实现能力受限（以源码 `CameraManager.jvm.kt` 为准）；拍照/录像类业务建议仅 Android/iOS 使用。 <!-- TODO: 待确认 —— 桌面端相机能力边界请以 CameraManager.jvm.kt 为准 -->

**Q6：`MoldImage` 和 Coil 什么关系？**

`MoldImage` 是跨平台封装（expect/actual）：Android 侧基于 Coil 加载网络图，桌面/iOS 侧有各自实现。网络图加载统一走 `coil-network-ktor3`，如遇加载失败检查 URL 与 Coil 配置。

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
