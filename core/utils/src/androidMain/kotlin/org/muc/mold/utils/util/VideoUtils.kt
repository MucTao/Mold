package org.muc.mold.utils.util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoUtils {

    /**
     * 获取视频指定时间点的帧。
     *
     * @param filePath 视频文件路径
     * @param timeUs   时间（微秒），默认 -1 表示取第一帧
     */
    suspend fun getVideoFrame(
        filePath: String,
        timeUs: Long = -1L
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val frame = if (timeUs >= 0) {
                retriever.getFrameAtTime(timeUs)
            } else {
                retriever.frameAtTime
            }
            retriever.release()
            frame!!
        }
    }

    /**
     * 获取视频缩略图数组（字节形式）。
     *
     * @param filePath 视频文件路径
     * @return [Result.success] 包含 ByteArray；[Result.failure] 包含异常
     */
    suspend fun getVideoThumbnail(
        filePath: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val picture = retriever.embeddedPicture
            retriever.release()
            picture!!
        }
    }

}