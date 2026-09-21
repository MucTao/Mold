@file:Suppress("unused")
package org.muc.mold.utils.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.ShellUtils.DEFAULT_TIMEOUT_MS
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Shell 命令工具类 —— 提供 root / 普通 shell 执行、超时控制、流式输出等能力。
 *
 * 全部阻塞 IO 已迁移至 Kotlin 协程（[Dispatchers.IO]），
 * 可能失败的操作直接返回 [Result<T>]（不再使用 [runCatching]）。
 * 支持 [suspend] 直接调用和 [Flow] 逐行流式输出两种模式。
 *
 * ## 使用前提
 * - root 命令需要设备已 root。
 * - 调用方需持有 [kotlinx.coroutines.CoroutineScope] 管理生命周期。
 *
 * ## 示例
 * ```kotlin
 * // suspend 直接调用
 * ShellUtils.exec("ls -la /data", isRoot = true)
 *     .onSuccess { println(it.stdout) }
 *     .onFailure { it.printStackTrace() }
 *
 * // Flow 流式逐行输出
 * ShellUtils.execStream("logcat -d")
 *     .collect { line -> println(line) }
 * ```
 *
 * 原始作者：Muc
 * 优化：线程 → 协程、回调 → suspend/Flow、runCatching → Result、完整 KDoc、API 21 兼容
 */
object ShellUtils {

    // ──────────────────────────────────────────────
    // 可配置常量
    // ──────────────────────────────────────────────

    /** 默认超时时间（毫秒） */
    private const val DEFAULT_TIMEOUT_MS = 30_000L

    // ──────────────────────────────────────────────
    // 公开 API：suspend → Result
    // ──────────────────────────────────────────────

    /**
     * 执行 shell 命令，返回完整输出。
     *
     * @param command   要执行的命令
     * @param isRoot    是否以 root 权限执行，默认 `false`
     * @param timeoutMs 超时时间（毫秒），默认 [DEFAULT_TIMEOUT_MS]
     * @param dispatcher 协程调度器，默认 [Dispatchers.IO]
     * @return [Result.success] 包含 [CommandResult]；[Result.failure] 包含异常
     */
    suspend fun exec(
        command: String,
        isRoot: Boolean = false,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Result<CommandResult> = withContext(dispatcher) {
        execInternal(command, isRoot, timeoutMs)
    }

    // ──────────────────────────────────────────────
    // 公开 API：Flow 流式输出
    // ──────────────────────────────────────────────

    /**
     * 以 [Flow] 逐行发射命令输出（适合大量输出如 `logcat`）。
     *
     * @param command   要执行的命令
     * @param isRoot    是否以 root 权限执行，默认 `true`
     * @param timeoutMs 超时时间（毫秒），默认 [DEFAULT_TIMEOUT_MS]
     * @param dispatcher 协程调度器，默认 [Dispatchers.IO]
     * @return 逐行发射 stdout 的 [Flow]<[String]>
     */
    fun execStream(
        command: String,
        isRoot: Boolean = true,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Flow<String> = flow {
        withContext(dispatcher) {
            execStreamInternal(command, isRoot, timeoutMs)
        }
    }.flowOn(dispatcher)

    /**
     * 以 [Flow] 逐行发射命令输出，包含 stderr（以 `[STDERR] ` 前缀标识）。
     *
     * @param command   要执行的命令
     * @param isRoot    是否以 root 权限执行，默认 `true`
     * @param timeoutMs 超时时间（毫秒），默认 [DEFAULT_TIMEOUT_MS]
     * @param dispatcher 协程调度器，默认 [Dispatchers.IO]
     * @return 逐行发射 stdout + stderr 的 [Flow]<[String]>
     */
    fun execStreamWithStderr(
        command: String,
        isRoot: Boolean = true,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Flow<String> = flow {
        withContext(dispatcher) {
            execStreamInternalWithStderr(command, isRoot, timeoutMs)
        }
    }.flowOn(dispatcher)

    // ──────────────────────────────────────────────
    // 内部阻塞实现（仅在 Dispatchers.IO 上调用）
    // ──────────────────────────────────────────────

    /**
     * 阻塞执行 shell 命令，返回 [CommandResult]。
     *
     * ⚠️ 必须运行在后台调度器（[Dispatchers.IO]）上。
     */
    @Throws(IOException::class, InterruptedException::class)
    private fun execInternal(
        command: String,
        isRoot: Boolean,
        timeoutMs: Long,
    ): Result<CommandResult> =
        runCatching {
            val process: Process = if (isRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdout = StringBuilder()
            val stderr = StringBuilder()

            try {
                // 启动两个线程并发读取 stdout / stderr
                val stdoutThread = threadRead(stdoutReader, stdout)
                val stderrThread = threadRead(stderrReader, stderr)

                // 兼容 API 21：轮询 isAlive() 实现超时
                waitForProcess(process, timeoutMs)

                // 等待读取线程结束
                stdoutThread.join(5000)
                stderrThread.join(5000)
            } finally {
                try {
                    stdoutReader.close()
                } catch (_: IOException) {
                }
                try {
                    stderrReader.close()
                } catch (_: IOException) {
                }
                try {
                    process.destroy()
                } catch (_: Exception) {
                }
            }
            if (process.exitValue() == 0)
                CommandResult(stdout.toString().trim(), stderr.toString().trim())
            else
                throw RuntimeException("errorCode:${process.exitValue()}")
        }

    /**
     * 兼容 `API 21` 的进程超时等待。
     *
     * 不依赖 [Process.isAlive]（API 26）或 [Process.waitFor] 重载（API 26），
     * 通过 [Process.exitValue] 是否抛 [IllegalThreadStateException] 判断进程存活状态。
     *
     * @param process   目标进程
     * @param timeoutMs 超时毫秒
     * @throws InterruptedException 超时或被中断时抛出，同时销毁进程
     */
    @Throws(InterruptedException::class)
    private fun waitForProcess(process: Process, timeoutMs: Long) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            if (System.currentTimeMillis() > deadline) {
                process.destroy()
                throw InterruptedException("Command timed out after $timeoutMs ms")
            }
            try {
                process.exitValue()       // ✅ 进程已结束 → 返回退出码
                return                     // 成功获取 → 退出
            } catch (_: IllegalThreadStateException) {
                // ✅ 进程仍在运行 → 继续等待
                Thread.sleep(50)
            }
        }
    }

    /**
     * 启动守护线程读取 [BufferedReader] 到 [StringBuilder]。
     */
    private fun threadRead(reader: BufferedReader, builder: StringBuilder): Thread {
        val thread = Thread {
            try {
                reader.forEachLine { builder.appendLine(it) }
            } catch (_: IOException) {
                // 流被关闭，正常结束
            }
        }
        thread.isDaemon = true
        thread.start()
        return thread
    }

    // ──────────────────────────────────────────────
    // 内部流式实现（emit 在 suspend 上下文中）
    // ──────────────────────────────────────────────

    /**
     * 流式阻塞执行 —— 在 [withContext] 内部调用，可以安全 `emit`。
     */
    private suspend fun FlowCollector<String>.execStreamInternal(
        command: String,
        isRoot: Boolean,
        timeoutMs: Long,
    ) {
        withContext(Dispatchers.IO) {
            val process: Process = if (isRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    emit(line!!)
                }
                waitForProcess(process, timeoutMs)
            } catch (_: InterruptedException) {
                process.destroy()
            } catch (_: IOException) {
                // 流关闭
            } finally {
                try {
                    reader.close()
                } catch (_: IOException) {
                }
                try {
                    process.destroy()
                } catch (_: Exception) {
                }
            }
        }
    }

    /**
     * 流式阻塞执行（含 stderr）—— 在 [withContext] 内部调用，可以安全 `emit`。
     */
    private suspend fun FlowCollector<String>.execStreamInternalWithStderr(
        command: String,
        isRoot: Boolean,
        timeoutMs: Long,
    ) {
        withContext(Dispatchers.IO) {
            val process: Process = if (isRoot) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }
            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))
            try {
                // 使用协程并发读取 stdout 和 stderr
                coroutineScope {
                    val stdoutJob = launchRead(stdoutReader, prefix = "")
                    val stderrJob = launchRead(stderrReader, prefix = "[STDERR] ")
                    stdoutJob.join()
                    stderrJob.join()
                }
                waitForProcess(process, timeoutMs)
            } catch (_: InterruptedException) {
                process.destroy()
            } catch (_: IOException) {
                // 流关闭
            } finally {
                try {
                    stdoutReader.close()
                } catch (_: IOException) {
                }
                try {
                    stderrReader.close()
                } catch (_: IOException) {
                }
                try {
                    process.destroy()
                } catch (_: Exception) {
                }
            }
        }
    }

    /**
     * 在 [coroutineScope] 内启动协程读取流并逐行 [emit]。
     */
    private suspend fun FlowCollector<String>.launchRead(
        reader: BufferedReader,
        prefix: String,
    ): Job = coroutineScope {
        launch {
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    emit("$prefix$line")
                }
            } catch (_: IOException) {
                // 流关闭
            }
        }
    }
    // ──────────────────────────────────────────────
    // 数据类
    // ──────────────────────────────────────────────

    /**
     * Shell 命令执行结果。
     * @property stdout   标准输出
     * @property stderr   标准错误输出
     */
    data class CommandResult(
        val stdout: String,
        val stderr: String,
    )

}