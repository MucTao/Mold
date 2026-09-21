@file:Suppress("unused")

package org.muc.mold.utils.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.ConvertUtils.toBitmap
import org.muc.mold.utils.util.ResourceUtils.copyFileFromAssets
import org.muc.mold.utils.util.ResourceUtils.copyFileFromRaw
import org.muc.mold.utils.util.ResourceUtils.getResIdByName
import java.io.IOException
import java.nio.charset.Charset

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/05/07
 * desc  : utils about resource
 * </pre>
 *
 * 资源访问工具。
 *
 * - 获取资源 id：`getResIdByName` 及 `getXxxIdByName` 系列（同步，微秒级）
 * - 读取 assets/raw：`readXxx2String` / `readXxx2List`（同步）+ `xxxAsync`（协程，跑在 IO 调度器）
 * - 复制 assets/raw：`copyFileFromXxx`（同步）+ `copyFileFromXxxAsync`（协程）
 */
object ResourceUtils {

    private const val BUFFER_SIZE = 8192

    // ====================================================================================
    //  Drawable
    // ====================================================================================

    /**
     * Return the drawable by identifier.
     *
     * @param id The identifier.
     * @return the drawable by identifier
     */
    fun getDrawable(@DrawableRes id: Int): Drawable? =
        ContextCompat.getDrawable(Utils.app, id)

    // ====================================================================================
    //  资源 id 查找（原 10 个方法，全部委托给 getResIdByName）
    // ====================================================================================

    /**
     * Return the resource id by name.
     *
     * 底层是 [android.content.res.Resources.getIdentifier]，**性能较差**，
     * 建议只在初始化阶段使用，不要在 `onDraw` / `onBindViewHolder` 等热路径调用。
     *
     * @param name The name of resource.
     * @param type The resource type (e.g. "id", "string", "drawable", "layout", ...).
     * @return the resource id；查不到时返回 `0`
     */
    @Suppress("InternalInsetResource", "DiscouragedApi")
    fun getResIdByName(name: String, type: String): Int =
        Utils.app.resources.getIdentifier(name, type, Utils.app.packageName)

    /** @see getResIdByName */
    fun getIdByName(name: String): Int = getResIdByName(name, "id")

    /** @see getResIdByName */
    fun getStringIdByName(name: String): Int = getResIdByName(name, "string")

    /** @see getResIdByName */
    fun getColorIdByName(name: String): Int = getResIdByName(name, "color")

    /** @see getResIdByName */
    fun getDimenIdByName(name: String): Int = getResIdByName(name, "dimen")

    /** @see getResIdByName */
    fun getDrawableIdByName(name: String): Int = getResIdByName(name, "drawable")

    /** @see getResIdByName */
    fun getMipmapIdByName(name: String): Int = getResIdByName(name, "mipmap")

    /** @see getResIdByName */
    fun getLayoutIdByName(name: String): Int = getResIdByName(name, "layout")

    /** @see getResIdByName */
    fun getStyleIdByName(name: String): Int = getResIdByName(name, "style")

    /** @see getResIdByName */
    fun getAnimIdByName(name: String): Int = getResIdByName(name, "anim")

    /** @see getResIdByName */
    fun getMenuIdByName(name: String): Int = getResIdByName(name, "menu")

    // ====================================================================================
    //  Assets：复制
    // ====================================================================================

    /**
     * Copy the file (or directory) from assets.
     *
     * 当 [assetsFilePath] 指向目录时递归复制整个目录到 [destFilePath]。
     *
     * @param assetsFilePath The path of file in assets.
     * @param destFilePath The path of destination file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun copyFileFromAssets(assetsFilePath: String, destFilePath: String): Boolean = try {
        val assets = Utils.app.assets.list(assetsFilePath)
        if (!assets.isNullOrEmpty()) {
            assets.all { asset ->
                copyFileFromAssets(
                    "$assetsFilePath/$asset",
                    "$destFilePath/$asset",
                )
            }
        } else {
            FileIOUtils.writeFileFromIS(
                destFilePath,
                Utils.app.assets.open(assetsFilePath),
            )
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    /**
     * 协程版 [copyFileFromAssets]，在 [Dispatchers.IO] 执行。
     *
     * @param assetsFilePath The path of file in assets.
     * @param destFilePath The path of destination file.
     * @return `true`: success<br></br>`false`: fail
     */
    suspend fun copyFileFromAssetsAsync(
        assetsFilePath: String,
        destFilePath: String,
    ): Boolean = withContext(Dispatchers.IO) {
        copyFileFromAssets(assetsFilePath, destFilePath)
    }

    // ====================================================================================
    //  Assets：读取
    // ====================================================================================
    /**
     * 从 assets 读取文本文件。
     *
     * @param filePath assets 中的文件路径（如 `"data/config.json"`）
     * @param charset  字符编码，默认 UTF-8
     */
    suspend fun assets2String(
        filePath: String,
        charset: Charset = Charsets.UTF_8
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            Utils.app.assets.open(filePath).bufferedReader(charset).readText()
        }
    }


    // ====================================================================================
    //  Raw：复制
    // ====================================================================================

    /**
     * Copy the file from raw.
     *
     * @param resId The resource id.
     * @param destFilePath The path of destination file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun copyFileFromRaw(@RawRes resId: Int, destFilePath: String): Boolean =
        FileIOUtils.writeFileFromIS(
            destFilePath,
            Utils.app.resources.openRawResource(resId),
        )

    /**
     * 协程版 [copyFileFromRaw]，在 [Dispatchers.IO] 执行。
     */
    suspend fun copyFileFromRawAsync(
        @RawRes resId: Int,
        destFilePath: String,
    ): Boolean = withContext(Dispatchers.IO) {
        copyFileFromRaw(resId, destFilePath)
    }

    // ====================================================================================
    //  Raw：读取
    // ====================================================================================

    /**
     * 从 res/raw 读取文本文件。
     *
     * @param rawId   资源 ID（如 `R.raw.config`）
     * @param charset 字符编码，默认 UTF-8
     */
    suspend fun raw2String(
        @RawRes rawId: Int,
        charset: Charset = Charsets.UTF_8
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching { Utils.app.resources.openRawResource(rawId).bufferedReader(charset).readText() }
    }


    /**
     * 从资源 ID 获取 Bitmap。
     */
    fun res2Bitmap(@DrawableRes drawableRes: Int): Result<Bitmap>? = ContextCompat.getDrawable(Utils.app, drawableRes)?.toBitmap()
}