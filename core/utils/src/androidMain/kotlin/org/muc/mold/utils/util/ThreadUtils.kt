@file:Suppress("unused")

package org.muc.mold.utils.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import org.muc.mold.utils.util.ThreadUtils.TYPE_PRIORITY_POOLS
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.Volatile

/**
 * 线程相关工具类。
 *
 * 提供四类内置线程池（[PoolType.Single] / [PoolType.Cached] / [PoolType.Io] / [PoolType.Cpu]）
 * 以及固定大小线程池 [PoolType.Fixed]；并提供基于 [Task] 的可取消、可定时、可设置超时与回调线程的任务封装。
 */
object ThreadUtils {

    // region 池类型

    /**
     * 线程池类型标识。
     *
     * 内置池使用 [Single] / [Cached] / [Io] / [Cpu]（单例）；
     * 固定大小池使用 [Fixed]，其 [Fixed.size] 即为线程数。
     *
     * 作为 [TYPE_PRIORITY_POOLS] 的一级 key，相等性由类型 + size（Fixed 时）决定。
     */
    sealed class PoolType {

        /** 单线程池：1 个工作线程，任务串行执行。 */
        object Single : PoolType()

        /** 缓存线程池：按需创建线程，空闲 60s 回收，最大 128 线程。 */
        object Cached : PoolType()

        /** IO 密集型线程池：线程数固定 `2 * CPU + 1`，无界队列。 */
        object Io : PoolType()

        /** CPU 密集型线程池：核心 `CPU + 1`、最大 `2 * CPU + 1`，无界队列。 */
        object Cpu : PoolType()

        /**
         * 固定大小线程池。
         *
         * @property size 线程数，>= 1
         */
        data class Fixed(val size: Int) : PoolType()
    }

    // endregion

    // region 字段

    private val HANDLER = Handler(Looper.getMainLooper())

    /** 池类型 → 线程优先级 → 线程池 */
    private val TYPE_PRIORITY_POOLS = HashMap<PoolType, MutableMap<Int, ExecutorService>>()

    /** 已提交但未完成的任务 → 所在池 */
    private val TASK_POOL_MAP = ConcurrentHashMap<Task<*>, ExecutorService>()

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val TIMER = Timer()

    private var sDeliver: Executor? = null

    // endregion

    // region 主线程调度

    /**
     * 提交一个异步任务到缓存线程池（内部使用）。
     *
     * @param task 待执行的任务
     * @return 原样返回 [task]，便于链式调用
     */
    internal fun <T> doAsync(task: Utils.Task<T?>): Utils.Task<T?> {
        cachedPool.execute(task)
        return task
    }

    /** 当前线程是否为主线程（UI 线程）。 */
    val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    /** 主线程的 [Handler]。 */
    val mainHandler: Handler
        get() = HANDLER

    /**
     * 确保 [runnable] 在主线程执行。
     *
     * 若当前已在主线程，则直接同步执行；否则 post 到主线程消息队列。
     *
     * @param runnable 待执行的代码块
     */
    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) runnable.run()
        else HANDLER.post(runnable)
    }

    /**
     * 延迟 [delayMillis] 毫秒后，在主线程执行 [runnable]。
     *
     * @param runnable    待执行的代码块
     * @param delayMillis 延迟毫秒数
     */
    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
        HANDLER.postDelayed(runnable, delayMillis)
    }

    // endregion

    // region 线程池获取

    /**
     * 获取一个固定大小的线程池。
     *
     * @param size     线程数，>= 1
     * @param priority 线程优先级，1..10，默认 [Thread.NORM_PRIORITY]
     * @return 共享的固定大小线程池（同 size + priority 复用同一实例）
     */
    @JvmOverloads
    fun getFixedPool(
        @IntRange(from = 1) size: Int,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = getPoolByTypeAndPriority(PoolType.Fixed(size), priority)

    /** 单线程池：仅 1 个工作线程、无界队列、任务串行执行。 */
    val singlePool: ExecutorService get() = getPoolByTypeAndPriority(PoolType.Single)

    /** 缓存线程池：按需创建线程、空闲 60s 回收、最大 128 线程。 */
    val cachedPool: ExecutorService get() = getPoolByTypeAndPriority(PoolType.Cached)

    /** IO 密集型线程池：线程数固定 `2 * CPU + 1`。 */
    val ioPool: ExecutorService get() = getPoolByTypeAndPriority(PoolType.Io)

    /** CPU 密集型线程池：核心 `CPU + 1`、最大 `2 * CPU + 1`。 */
    val cpuPool: ExecutorService get() = getPoolByTypeAndPriority(PoolType.Cpu)

    /**
     * 获取单线程池（可指定线程优先级）。
     *
     * @param priority 线程优先级，1..10，默认 [Thread.NORM_PRIORITY]
     */
    fun getSinglePool(
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = getPoolByTypeAndPriority(PoolType.Single, priority)

    /**
     * 获取缓存线程池（可指定线程优先级）。
     *
     * @param priority 线程优先级，1..10，默认 [Thread.NORM_PRIORITY]
     */
    fun getCachedPool(
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = getPoolByTypeAndPriority(PoolType.Cached, priority)

    /**
     * 获取 IO 线程池（可指定线程优先级）。
     *
     * @param priority 线程优先级，1..10，默认 [Thread.NORM_PRIORITY]
     */
    fun getIoPool(
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = getPoolByTypeAndPriority(PoolType.Io, priority)

    /**
     * 获取 CPU 线程池（可指定线程优先级）。
     *
     * @param priority 线程优先级，1..10，默认 [Thread.NORM_PRIORITY]
     */
    fun getCpuPool(
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = getPoolByTypeAndPriority(PoolType.Cpu, priority)

    // endregion

    // region 任务执行 · 固定大小池

    /**
     * 在固定大小线程池中执行 [task]。
     *
     * @param size     线程池大小，>= 1
     * @param task     待执行的任务
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByFixed(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Fixed(size), priority), task)

    /**
     * 在固定大小线程池中延迟执行 [task]。
     *
     * @param size     线程池大小，>= 1
     * @param task     待执行的任务
     * @param delay    延迟时长
     * @param unit     [delay] 的时间单位
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByFixedWithDelay(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Fixed(size), priority), task, delay, unit = unit)

    /**
     * 在固定大小线程池中按固定速率周期执行 [task]。
     *
     * @param size         线程池大小，>= 1
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     * @param priority     线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(
        getPoolByTypeAndPriority(PoolType.Fixed(size), priority),
        task, initialDelay, period, unit,
    )

    // endregion

    // region 任务执行 · 单线程池

    /**
     * 在单线程池中执行 [task]。
     *
     * @param task     待执行的任务
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeBySingle(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Single, priority), task)

    /**
     * 在单线程池中延迟执行 [task]。
     *
     * @param task     待执行的任务
     * @param delay    延迟时长
     * @param unit     [delay] 的时间单位
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeBySingleWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Single, priority), task, delay, unit = unit)

    /**
     * 在单线程池中按固定速率周期执行 [task]。
     *
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     * @param priority     线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeBySingleAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(
        getPoolByTypeAndPriority(PoolType.Single, priority),
        task, initialDelay, period, unit,
    )

    // endregion

    // region 任务执行 · 缓存线程池

    /**
     * 在缓存线程池中执行 [task]。
     *
     * @param task     待执行的任务
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCached(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Cached, priority), task)

    /**
     * 在缓存线程池中延迟执行 [task]。
     *
     * @param task     待执行的任务
     * @param delay    延迟时长
     * @param unit     [delay] 的时间单位
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCachedWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Cached, priority), task, delay, unit = unit)

    /**
     * 在缓存线程池中按固定速率周期执行 [task]。
     *
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     * @param priority     线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCachedAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(
        getPoolByTypeAndPriority(PoolType.Cached, priority),
        task, initialDelay, period, unit,
    )

    // endregion

    // region 任务执行 · IO 线程池

    /**
     * 在 IO 线程池中执行 [task]。
     *
     * @param task     待执行的任务
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByIo(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Io, priority), task)

    /**
     * 在 IO 线程池中延迟执行 [task]。
     *
     * @param task     待执行的任务
     * @param delay    延迟时长
     * @param unit     [delay] 的时间单位
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByIoWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Io, priority), task, delay, unit = unit)

    /**
     * 在 IO 线程池中按固定速率周期执行 [task]。
     *
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     * @param priority     线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByIoAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(
        getPoolByTypeAndPriority(PoolType.Io, priority),
        task, initialDelay, period, unit,
    )

    // endregion

    // region 任务执行 · CPU 线程池

    /**
     * 在 CPU 线程池中执行 [task]。
     *
     * @param task     待执行的任务
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCpu(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Cpu, priority), task)

    /**
     * 在 CPU 线程池中延迟执行 [task]。
     *
     * @param task     待执行的任务
     * @param delay    延迟时长
     * @param unit     [delay] 的时间单位
     * @param priority 线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCpuWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(getPoolByTypeAndPriority(PoolType.Cpu, priority), task, delay, unit = unit)

    /**
     * 在 CPU 线程池中按固定速率周期执行 [task]。
     *
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     * @param priority     线程优先级，默认 [Thread.NORM_PRIORITY]
     */
    @JvmOverloads
    fun <T> executeByCpuAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        @IntRange(from = 1, to = 10) priority: Int = Thread.NORM_PRIORITY,
    ) = execute<T?>(
        getPoolByTypeAndPriority(PoolType.Cpu, priority),
        task, initialDelay, period, unit,
    )

    // endregion

    // region 任务执行 · 自定义池

    /**
     * 在 [pool] 中立即执行 [task]。
     *
     * @param pool 自定义线程池
     * @param task 待执行的任务
     */
    fun <T> executeByCustom(pool: ExecutorService, task: Task<T?>) =
        execute<T?>(pool, task)

    /**
     * 在 [pool] 中延迟执行 [task]。
     *
     * @param pool  自定义线程池
     * @param task  待执行的任务
     * @param delay 延迟时长
     * @param unit  [delay] 的时间单位
     */
    fun <T> executeByCustomWithDelay(
        pool: ExecutorService,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
    ) = execute<T?>(pool, task, delay, unit = unit)

    /**
     * 在 [pool] 中按固定速率周期执行 [task]。
     *
     * @param pool         自定义线程池
     * @param task         待执行的任务
     * @param period       相邻两次执行之间的间隔
     * @param unit         [period] 与 [initialDelay] 的时间单位
     * @param initialDelay 首次执行的延迟时间，默认 0
     */
    @JvmOverloads
    fun <T> executeByCustomAtFixRate(
        pool: ExecutorService,
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
    ) = execute<T?>(pool, task, initialDelay, period, unit)

    // endregion

    // region 取消任务

    /**
     * 取消单个任务。
     *
     * @param task 待取消的任务，[null] 时忽略
     */
    fun cancel(task: Task<*>?) = task?.cancel()

    /**
     * 批量取消任务。
     *
     * @param tasks 待取消的任务列表，元素为 [null] 时忽略
     */
    fun cancel(vararg tasks: Task<*>?) = tasks.forEach { it?.cancel() }

    /**
     * 批量取消任务。
     *
     * @param tasks 待取消的任务列表，[null] 或空集时忽略
     */
    fun cancel(tasks: List<Task<*>?>?) = tasks?.forEach { it?.cancel() }

    /**
     * 取消指定池中由 ThreadUtils 提交的所有任务。
     *
     * @param executorService 线程池；若不是 ThreadUtils 创建的池，仅打印告警
     */
    fun cancel(executorService: ExecutorService?) {
        if (executorService !is ThreadPoolExecutor4Util) {
            Log.e("ThreadUtils", "The executorService is not ThreadUtils's pool.")
            return
        }
        TASK_POOL_MAP.entries.forEach { (task, pool) ->
            if (pool == executorService) task.cancel()
        }
    }

    // endregion

    /**
     * 设置全局回调投递器；默认为主线程。
     *
     * @param deliver 自定义 [Executor]，[null] 时下次使用时恢复默认主线程投递
     */
    fun setDeliver(deliver: Executor?) {
        sDeliver = deliver
    }

    // region 内部实现

    /**
     * 提交任务的统一入口。
     *
     * @param pool   目标线程池
     * @param task   待执行任务
     * @param delay  延迟毫秒数（由 [unit] 换算），0 表示不延迟
     * @param period 周期毫秒数（由 [unit] 换算），0 表示仅执行一次
     * @param unit   时间单位；[period] / [delay] 为 0 时可传 [null]
     */
    private fun <T> execute(
        pool: ExecutorService,
        task: Task<T?>,
        delay: Long = 0,
        period: Long = 0,
        unit: TimeUnit? = null,
    ) {
        synchronized(TASK_POOL_MAP) {
            if (TASK_POOL_MAP[task] != null) {
                Log.e("ThreadUtils", "Task can only be executed once.")
                return
            }
            TASK_POOL_MAP[task] = pool
        }

        when {
            period == 0L && delay == 0L -> pool.execute(task)

            period == 0L -> TIMER.schedule(
                object : TimerTask() {
                    override fun run() = pool.execute(task)
                },
                unit?.toMillis(delay) ?: 0,
            )

            else -> {
                task.setSchedule(true)
                TIMER.schedule(
                    object : TimerTask() {
                        override fun run() = pool.execute(task)
                    },
                    unit?.toMillis(delay) ?: 0,
                    unit?.toMillis(period) ?: 0,
                )
            }
        }
    }

    /**
     * 按 type + priority 获取（或懒创建）内置/固定池。
     *
     * @param type     池类型，见 [PoolType]
     * @param priority 线程优先级
     */
    private fun getPoolByTypeAndPriority(
        type: PoolType,
        priority: Int = Thread.NORM_PRIORITY,
    ): ExecutorService = synchronized(TYPE_PRIORITY_POOLS) {
        TYPE_PRIORITY_POOLS
            .getOrPut(type) { ConcurrentHashMap() }
            .getOrPut(priority) { ThreadPoolExecutor4Util.createPool(type, priority) }
    }

    private fun getGlobalDeliver(): Executor =
        sDeliver ?: Executor { runOnUiThread(it) }.also { sDeliver = it }

    // endregion

    // region 线程池实现

    internal class ThreadPoolExecutor4Util private constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: LinkedBlockingQueue4Util,
        threadFactory: ThreadFactory?,
    ) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {

        private val mSubmittedCount = AtomicInteger()
        private val mWorkQueue: LinkedBlockingQueue4Util

        init {
            workQueue.mPool = this
            mWorkQueue = workQueue
        }

        override fun afterExecute(r: Runnable?, t: Throwable?) {
            mSubmittedCount.decrementAndGet()
            super.afterExecute(r, t)
        }

        override fun execute(command: Runnable) {
            if (isShutdown) return
            mSubmittedCount.incrementAndGet()
            runCatching { super.execute(command) }
                .onFailure { failure ->
                    if (failure is RejectedExecutionException) {
                        Log.e("ThreadUtils", "This will not happen!")
                        mWorkQueue.offer(command)
                    } else {
                        mSubmittedCount.decrementAndGet()
                    }
                }
        }

        companion object {
            /**
             * 根据 [type] 创建线程池。
             *
             * @param type     池类型，见 [PoolType]
             * @param priority 线程优先级，1..10
             */
            fun createPool(type: PoolType, priority: Int): ExecutorService = when (type) {
                PoolType.Single -> ThreadPoolExecutor4Util(
                    1, 1, 0L, TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue4Util(),
                    UtilsThreadFactory("single", priority),
                )

                PoolType.Cached -> ThreadPoolExecutor4Util(
                    0, 128, 60L, TimeUnit.SECONDS,
                    LinkedBlockingQueue4Util(true),
                    UtilsThreadFactory("cached", priority),
                )

                PoolType.Io -> ThreadPoolExecutor4Util(
                    2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1, 30, TimeUnit.SECONDS,
                    LinkedBlockingQueue4Util(),
                    UtilsThreadFactory("io", priority),
                )

                PoolType.Cpu -> ThreadPoolExecutor4Util(
                    CPU_COUNT + 1, 2 * CPU_COUNT + 1, 30, TimeUnit.SECONDS,
                    LinkedBlockingQueue4Util(true),
                    UtilsThreadFactory("cpu", priority),
                )

                is PoolType.Fixed -> ThreadPoolExecutor4Util(
                    type.size, type.size, 0L, TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue4Util(),
                    UtilsThreadFactory("fixed(${type.size})", priority),
                )
            }
        }
    }

    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable> {

        @Volatile
        var mPool: ThreadPoolExecutor4Util? = null

        private var mCapacity: Int = Integer.MAX_VALUE

        constructor() : super()

        constructor(isAddSubThreadFirstThenAddQueue: Boolean) : super() {
            if (isAddSubThreadFirstThenAddQueue) mCapacity = 0
        }

        constructor(capacity: Int) : super() {
            mCapacity = capacity
        }

        override fun offer(runnable: Runnable): Boolean {
            if (mCapacity <= size && mPool?.let { it.poolSize < it.maximumPoolSize } == true) {
                return false
            }
            return super.offer(runnable)
        }
    }

    internal class UtilsThreadFactory(
        prefix: String?,
        private val priority: Int,
        private val isDaemon: Boolean,
    ) : AtomicLong(), ThreadFactory {

        private val namePrefix = "$prefix-pool-${POOL_NUMBER.getAndIncrement()}-thread-"

        constructor(prefix: String?, priority: Int) : this(prefix, priority, false)

        override fun newThread(r: Runnable): Thread {
            val t = object : Thread(r, namePrefix + getAndIncrement()) {
                override fun run() {
                    runCatching { super.run() }
                        .onFailure { Log.e("ThreadUtils", "Request threw uncaught throwable", it) }
                }
            }
            t.isDaemon = isDaemon
            t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> println(e) }
            t.priority = priority
            return t
        }

        override fun toShort(): Short = TODO("Not yet implemented")
        override fun toByte(): Byte = TODO("Not yet implemented")

        companion object {
            private val POOL_NUMBER = AtomicInteger(1)
            private const val serialVersionUID = -9209200509960368598L
        }
    }

    // endregion

    // region Task

    /**
     * [Task] 的简单实现：默认打印回调日志。
     *
     * @param T 任务结果类型
     */
    abstract class SimpleTask<T> : Task<T?>() {
        override fun onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread())
        }

        override fun onFail(t: Throwable?) {
            Log.e("ThreadUtils", "onFail: ", t)
        }
    }

    /**
     * 可调度、可取消、可超时的任务抽象基类。
     *
     * 子类需实现 [doInBackground] 执行业务逻辑，以及 [onSuccess] / [onCancel] / [onFail] 回调；
     * 回调默认在主线程执行，可通过 [setDeliver] 修改投递线程。
     *
     * @param T 任务结果类型
     */
    abstract class Task<T> : Runnable {

        private val state = AtomicInteger(NEW)

        @Volatile
        private var isSchedule = false
        @Volatile
        private var runner: Thread? = null

        private var mTimer: Timer? = null
        private var mTimeoutMillis: Long = 0
        private var mTimeoutListener: OnTimeoutListener? = null
        private var deliver: Executor? = null

        /**
         * 执行后台逻辑。
         *
         * 抛出的异常会触发 [onFail]；[InterruptedException] 视作取消处理。
         */
        @Throws(Throwable::class)
        abstract fun doInBackground(): T?

        /**
         * 后台执行成功回调，默认在 UI 线程执行。
         *
         * @param result [doInBackground] 的返回值，可能为 [null]
         */
        abstract fun onSuccess(result: T?)

        /** 任务被取消时的回调。 */
        abstract fun onCancel()

        /**
         * 后台执行异常时的回调。
         *
         * @param t 抛出的异常
         */
        abstract fun onFail(t: Throwable?)


        override fun run() {
            if (isSchedule) {
                if (runner == null) {
                    if (!state.compareAndSet(NEW, RUNNING)) return
                    runner = Thread.currentThread()
                    if (mTimeoutListener != null) {
                        Log.w("ThreadUtils", "Scheduled task doesn't support timeout.")
                    }
                } else if (state.get() != RUNNING) {
                    return
                }
            } else {
                if (!state.compareAndSet(NEW, RUNNING)) return
                runner = Thread.currentThread()
                if (mTimeoutListener != null) {
                    mTimer = Timer().apply {
                        schedule(object : TimerTask() {
                            override fun run() {
                                if (!this@Task.isDone && mTimeoutListener != null) {
                                    timeout()
                                    mTimeoutListener!!.onTimeout()
                                    onDone()
                                }
                            }
                        }, mTimeoutMillis)
                    }
                }
            }

            runCatching {
                val result = doInBackground()
                if (isSchedule) {
                    if (state.get() != RUNNING) return
                    getDeliver().execute { onSuccess(result) }
                } else {
                    if (!state.compareAndSet(RUNNING, COMPLETING)) return
                    getDeliver().execute {
                        onSuccess(result)
                        onDone()
                    }
                }
            }.onFailure { failure ->
                if (failure is InterruptedException) {
                    state.compareAndSet(CANCELLED, INTERRUPTED)
                } else {
                    if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) return
                    getDeliver().execute {
                        onFail(failure)
                        onDone()
                    }
                }
            }
        }

        /**
         * 取消任务。
         *
         * @param mayInterruptIfRunning 是否中断正在执行任务的线程，默认 true
         */
        @JvmOverloads
        fun cancel(mayInterruptIfRunning: Boolean = true) {
            synchronized(state) {
                if (state.get() > RUNNING) return
                state.set(CANCELLED)
            }
            if (mayInterruptIfRunning) runner?.interrupt()

            getDeliver().execute {
                onCancel()
                onDone()
            }
        }

        private fun timeout() {
            synchronized(state) {
                if (state.get() > RUNNING) return
                state.set(TIMEOUT)
            }
            runner?.interrupt()
        }

        /** 任务是否已取消。 */
        val isCanceled: Boolean get() = state.get() >= CANCELLED

        /** 任务是否已结束（成功 / 失败 / 取消 / 超时）。 */
        val isDone: Boolean get() = state.get() > RUNNING

        /**
         * 设置本次任务回调的执行线程。
         *
         * @param deliver 回调投递 [Executor]，[Null] 使用全局投递（默认主线程）
         * @return 当前任务，便于链式调用
         */
        fun setDeliver(deliver: Executor?): Task<T> = apply { this.deliver = deliver }

        /**
         * 设置超时时长；仅对非周期性任务有效（周期性任务会忽略超时）。
         *
         * @param timeoutMillis 超时毫秒数
         * @param listener      超时回调
         * @return 当前任务，便于链式调用
         */
        fun setTimeout(timeoutMillis: Long, listener: OnTimeoutListener?): Task<T> = apply {
            mTimeoutMillis = timeoutMillis
            mTimeoutListener = listener
        }

        /** 标记是否为周期性任务（内部使用）。 */
        fun setSchedule(isSchedule: Boolean) {
            this.isSchedule = isSchedule
        }

        private fun getDeliver(): Executor = deliver ?: getGlobalDeliver()

        /**
         * 任务结束时清理内部资源（子类覆写需调用 `super`）。
         */
        @CallSuper
        protected fun onDone() {
            TASK_POOL_MAP.remove(this)
            mTimer?.cancel()
            mTimer = null
            mTimeoutListener = null
        }

        /** 任务超时回调。 */
        interface OnTimeoutListener {
            fun onTimeout()
        }

        companion object {
            private const val NEW = 0
            private const val RUNNING = 1
            private const val EXCEPTIONAL = 2
            private const val COMPLETING = 3
            private const val CANCELLED = 4
            private const val INTERRUPTED = 5
            private const val TIMEOUT = 6
        }
    }

    // endregion

    // region SyncValue

    /**
     * 线程间同步取值容器：一端 set 后，另一端 get 会阻塞至值可用或超时。
     *
     * @param T 值类型
     */
    class SyncValue<T> {

        private val mLatch = CountDownLatch(1)
        private val mFlag = AtomicBoolean()
        private var mValue: T? = null

        /** 阻塞等待直到有值写入，然后返回该值。 */
        var value: T?
            get() {
                if (!mFlag.get()) {
                    runCatching { mLatch.await() }
                        .onFailure { e ->
                            if (e !is InterruptedException) throw e
                            e.printStackTrace()
                        }
                }
                return mValue
            }
            set(value) {
                if (mFlag.compareAndSet(false, true)) {
                    mValue = value
                    mLatch.countDown()
                }
            }

        /**
         * 带超时的获取。
         *
         * @param timeout      超时时间
         * @param unit         [timeout] 的时间单位
         * @param defaultValue 超时未写入时返回的默认值
         * @return 已写入的值；若超时则返回 [defaultValue]
         */
        fun getValue(timeout: Long, unit: TimeUnit?, defaultValue: T?): T? {
            if (!mFlag.get()) {
                runCatching { mLatch.await(timeout, unit) }
                    .onFailure { e ->
                        if (e !is InterruptedException) throw e
                        e.printStackTrace()
                        return defaultValue
                    }
            }
            return mValue
        }
    }

    // endregion
}