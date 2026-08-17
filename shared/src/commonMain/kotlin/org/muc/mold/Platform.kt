package org.muc.mold

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform