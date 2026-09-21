@file:Suppress("unused")

package org.muc.mold.utils.util

import java.io.PrintWriter

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2019/02/12
 * desc  : utils about exception
 * </pre> *
 */
object ThrowableUtils {
    private val LINE_SEP: String = System.lineSeparator()

    fun getFullStackTrace(throwable: Throwable?): String {
        var throwable = throwable
        val throwableList = ArrayList<Throwable>()
        while (throwable != null && !throwableList.contains(throwable)) {
            throwableList.add(throwable)
            throwable = throwable.cause
        }
        val size: Int = throwableList.size
        val frames = ArrayList<String>()
        var nextTrace = getStackFrameList(throwableList[size - 1])
        var i = size
        while (--i >= 0) {
            val trace = nextTrace
            if (i != 0) {
                nextTrace = getStackFrameList(throwableList[i - 1])
                removeCommonFrames(trace, nextTrace)
            }
            if (i == size - 1) {
                frames.add(throwableList[i].toString())
            } else {
                frames.add(" Caused by: " + throwableList[i].toString())
            }
            frames.addAll(trace)
        }
        val sb = StringBuilder()
        for (element in frames) {
            sb.append(element).append(LINE_SEP)
        }
        return sb.toString()
    }

    private fun getStackFrameList(throwable: Throwable): MutableList<String> {
        return java.io.StringWriter().use { sw ->
            PrintWriter(sw, true).use { pw ->
                throwable.printStackTrace(pw)
                val frames = java.util.StringTokenizer(sw.toString(), LINE_SEP)
                val list = mutableListOf<String>()
                var traceStarted = false
                while (frames.hasMoreTokens()) {
                    val token = frames.nextToken()
                    val at = token.indexOf("at")
                    if (at != -1 && token.substring(0, at).trim().isEmpty()) {
                        traceStarted = true
                        list.add(token)
                    } else if (traceStarted) {
                        break
                    }
                }
                list
            }
        }
    }

    private fun removeCommonFrames(causeFrames: MutableList<String>, wrapperFrames: List<String>) {
        var causeFrameIndex: Int = causeFrames.size - 1
        var wrapperFrameIndex: Int = wrapperFrames.size - 1
        while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
            val causeFrame: String = causeFrames[causeFrameIndex]
            val wrapperFrame = wrapperFrames[wrapperFrameIndex]
            if (causeFrame == wrapperFrame) {
                causeFrames.removeAt(causeFrameIndex)
            }
            causeFrameIndex--
            wrapperFrameIndex--
        }
    }
}
