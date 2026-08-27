package org.muc.ui.action.feedback

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import org.muc.ui.banner.MoldBanner
import org.muc.ui.banner.MoldBannerDismissStyle
import org.muc.ui.banner.MoldBannerType
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MoldTheme.windowWidthType
import org.muc.ui.design.adaptive.WindowWidthType

@Composable
internal fun SnackBarHost(
    contentAlignment: Alignment = BiasAlignment(0f, .7f),
    content: @Composable (SnackbarData) -> Unit = { SnackBar(contentAlignment, it) }
) {
    FeedbackManager.snackbar?.let {
        content(it)
    }
}

@Composable
internal fun SnackBar(
    contentAlignment: Alignment = BiasAlignment(0f, .7f),
    bar: SnackbarData
) {
    MoldBanner(
        message = bar.message,
        type = when (bar.type) {
            FeedBackType.PRIMARY -> MoldBannerType.PRIMARY
            FeedBackType.INFO -> MoldBannerType.INFO
            FeedBackType.SUCCESS -> MoldBannerType.SUCCESS
            FeedBackType.ERROR -> MoldBannerType.ERROR
            FeedBackType.WARNING -> MoldBannerType.WARNING
        },
        onDismiss = {
            bar.onAction?.invoke()
            FeedbackManager.dismissSnackbar()
        },
        alignment = contentAlignment,
        dismissStyle = if (bar.actionLabel == null) MoldBannerDismissStyle.ICON else MoldBannerDismissStyle.TEXT,
        dismissText = bar.actionLabel ?: "ok",
        modifier = Modifier.fillMaxWidth(if (windowWidthType == WindowWidthType.EXPANDED) .6f else .9f)
            .padding(Dimensions.paddingDefault),
    )
}
