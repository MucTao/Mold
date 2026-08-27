package org.muc.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.compose.SubcomposeAsyncImage
import com.materialkolor.ktx.themeColorOrNull
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MoldTheme
import org.muc.ui.status.LoadingView

private val cacheMap = HashMap<Any, Color>()

@Composable
fun MoldImage(
    model: Any?,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Image,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = model?.toString(),
    colorFilter: ColorFilter? = null,
    onSeedColor: ((Color?) -> Unit)? = null
) {
    if (model == null) {
        ErrorPlaceholder(modifier, icon, null)
        return
    }
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        placeholder = rememberVectorPainter(Icons.Outlined.ImageSearch),
        error = rememberVectorPainter(icon),
        onSuccess = { state ->
            onSeedColor?.let { callback ->
                callback(cacheMap[model] ?: state.result.image.toImageBitmap()?.themeColorOrNull()?.also {
                    cacheMap[model] = it
                })
            }
        },
        colorFilter = colorFilter,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
fun MoldSubImage(
    model: Any?,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Image,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = model?.toString(),
    colorFilter: ColorFilter? = null,
    onSeedColor: ((Color?) -> Unit)? = null
) {
    if (model == null) {
        ErrorPlaceholder(modifier, icon, null)
        return
    }
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        error = {
            ErrorPlaceholder(Modifier.fillMaxSize(), icon, it.result.throwable)
        },
        loading = {
            LoadingView("图片加载中", Modifier.fillMaxSize().background(MoldTheme.colorScheme.surfaceVariant))
        },
        onSuccess = { state ->
            onSeedColor?.let { callback ->
                callback(cacheMap[model] ?: state.result.image.toImageBitmap()?.themeColorOrNull()?.also {
                    cacheMap[model] = it
                })
            }
        },
        transform = {
            if (it is State.Success) {
                State.Success(it.painter, it.result)
            } else it
        },
        colorFilter = colorFilter,
        contentScale = contentScale
    )
}

@Composable
private fun ErrorPlaceholder(targetModifier: Modifier, icon: ImageVector = Icons.Outlined.Image, throwable: Throwable?) {
    Column(
        modifier = targetModifier.background(MoldTheme.colorScheme.surfaceVariant),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.4f),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        throwable?.let {
            Text(it.toString(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

expect fun coil3.Image.toImageBitmap(): ImageBitmap?
