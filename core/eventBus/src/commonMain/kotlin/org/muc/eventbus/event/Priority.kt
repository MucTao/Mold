@file:Suppress("unused")

package org.muc.eventbus.event

enum class Priority(val level: Int) {
    CRITICAL(4),   // 关键事件，必须立即处理
    HIGH(3),       // 高优先级
    NORMAL(2),     // 普通优先级
    LOW(1),        // 低优先级
    BACKGROUND(0)  // 后台事件
}
