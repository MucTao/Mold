package org.muc.datakv

import kotlinx.serialization.Serializable
import org.muc.datakv.di.NO_EXPIRATION

@Serializable
data class ExpirableData<T>(
    val data: T? = null, // 原始存储数据
    val expireTime: Long = NO_EXPIRATION // 过期时间戳（毫秒），<=0 表示永不过期
)

