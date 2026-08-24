package org.muc.ui.banner

/**
 * 横幅的语义类型。
 */
enum class MoldBannerType {
    NORMAL,

    PRIMARY,

    /** 信息消息。 */
    INFO,

    /** 操作成功完成。 */
    SUCCESS,

    /** 警告，不阻塞操作。 */
    WARNING,

    /** 错误或严重状态。 */
    ERROR,
}

/**
 * 横幅关闭按钮的样式。
 */
enum class MoldBannerDismissStyle {
    /** 图标形式的关闭按钮（"X"）。 */
    ICON,

    /** 文本形式的关闭按钮（"ОК"）。 */
    TEXT,
}


