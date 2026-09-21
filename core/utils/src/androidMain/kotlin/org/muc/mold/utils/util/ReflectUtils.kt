@file:Suppress("unused", "UNCHECKED_CAST")

package org.muc.mold.utils.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/12/15
 * desc  : utils about reflect
 * </pre>
 *
 * 链式反射工具。所有 API 都支持"失败即抛 [ReflectException]"的语义，
 * 便于在链式调用中一次 `try/catch` 收敛错误。
 */
class ReflectUtils private constructor(
    private val type: Class<*>?,
    private val obj: Any?,
) {

    private constructor(type: Class<*>) : this(type, type)

    // ====================================================================================
    //  newInstance
    // ====================================================================================

    /**
     * Create and initialize a new instance.
     *
     * @return the single [ReflectUtils] instance
     */
    fun newInstance(): ReflectUtils = newInstance(*emptyArray())

    /**
     * Create and initialize a new instance.
     *
     * @param args The args.
     * @return the single [ReflectUtils] instance
     * @throws ReflectException if reflect unsuccessfully
     */
    fun newInstance(vararg args: Any?): ReflectUtils {
        val types = getArgsType(*args)
        return try {
            newInstance(type!!.getDeclaredConstructor(*types), *args)
        } catch (e: NoSuchMethodException) {
            val list = type!!.declaredConstructors
                .filter { match(it.parameterTypes, types) }
                .toMutableList()
            if (list.isEmpty()) throw ReflectException(e)
            list.sortWith(Comparator { o1, o2 -> compareParams(o1.parameterTypes, o2.parameterTypes) })
            newInstance(list[0], *args)
        }
    }

    /**
     * 协程版 [newInstance]，在 [Dispatchers.IO] 执行。
     *
     * 适用于构造过程可能耗时（触发类加载、静态初始化）的场景。
     *
     * @param args The args.
     * @return the single [ReflectUtils] instance
     */
    suspend fun newInstanceAsync(vararg args: Any?): ReflectUtils =
        withContext(Dispatchers.IO) { newInstance(*args) }

    private fun getArgsType(vararg args: Any?): Array<Class<*>> =
        Array(args.size) { args[it]?.javaClass ?: NULL::class.java }

    private fun newInstance(constructor: Constructor<*>, vararg args: Any?): ReflectUtils = try {
        ReflectUtils(
            constructor.declaringClass,
            accessible(constructor).newInstance(*args),
        )
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    // ====================================================================================
    //  field
    // ====================================================================================

    /**
     * Get the field.
     *
     * @param name The name of field.
     * @return the single [ReflectUtils] instance wrapping the field value
     * @throws ReflectException if reflect unsuccessfully
     */
    fun field(name: String): ReflectUtils = try {
        val field = getField(name)
        ReflectUtils(field.type, field.get(obj))
    } catch (e: IllegalAccessException) {
        throw ReflectException(e)
    }

    /**
     * Set the field.
     *
     * @param name The name of field.
     * @param value The value.
     * @return the single [ReflectUtils] instance (self)
     * @throws ReflectException if reflect unsuccessfully
     */
    fun field(name: String, value: Any): ReflectUtils = try {
        getField(name).set(obj, unwrap(value))
        this
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    @Throws(IllegalAccessException::class)
    private fun getField(name: String): Field {
        val field = getAccessibleField(name)
        if (field.modifiers and Modifier.FINAL == Modifier.FINAL) {
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (_: NoSuchFieldException) {
                // Android 上不允许改 modifiers，退化为只置为 accessible
                field.isAccessible = true
            }
        }
        return field
    }

    private fun getAccessibleField(name: String): Field {
        var t: Class<*>? = type
        try {
            return accessible(t!!.getField(name))
        } catch (e: NoSuchFieldException) {
            do {
                try {
                    return accessible(t!!.getDeclaredField(name))
                } catch (_: NoSuchFieldException) {
                }
                t = t?.superclass
            } while (t != null)
            throw ReflectException(e)
        }
    }

    private fun unwrap(value: Any): Any? =
        if (value is ReflectUtils) value.get() else value

    // ====================================================================================
    //  method
    // ====================================================================================

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @return the single [ReflectUtils] instance wrapping the return value
     * @throws ReflectException if reflect unsuccessfully
     */
    fun method(name: String): ReflectUtils? = method(name, *emptyArray())

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @param args The args.
     * @return the single [ReflectUtils] instance wrapping the return value
     * @throws ReflectException if reflect unsuccessfully
     */
    fun method(name: String, vararg args: Any?): ReflectUtils? {
        val types = getArgsType(*args)
        return try {
            method(exactMethod(name, types), obj, *args)
        } catch (e: NoSuchMethodException) {
            try {
                method(similarMethod(name, types), obj, *args)
            } catch (e1: NoSuchMethodException) {
                throw ReflectException(e1)
            }
        }
    }

    /**
     * 协程版 [method]，在 [Dispatchers.IO] 执行。
     *
     * 适用于可能被……调用耗时（IO / 网络 / 复杂计算）的场景。
     *
     * @param name The name of method.
     * @param args The args.
     * @return the single [ReflectUtils] instance wrapping the return value
     */
    suspend fun methodAsync(name: String, vararg args: Any?): ReflectUtils? =
        withContext(Dispatchers.IO) { method(name, *args) }

    private fun method(method: Method, obj: Any?, vararg args: Any?): ReflectUtils? = try {
        accessible(method)
        if (method.returnType == Void.TYPE) {
            method.invoke(obj, *args)
            reflect(obj)
        } else {
            reflect(method.invoke(obj, *args))
        }
    } catch (e: Exception) {
        throw ReflectException(e)
    }

    @Throws(NoSuchMethodException::class)
    private fun exactMethod(name: String, types: Array<Class<*>>): Method {
        var t: Class<*>? = type
        try {
            return t!!.getMethod(name, *types)
        } catch (e: NoSuchMethodException) {
            do {
                try {
                    return t!!.getDeclaredMethod(name, *types)
                } catch (_: NoSuchMethodException) {
                }
                t = t?.superclass
            } while (t != null)
            throw e
        }
    }

    @Throws(NoSuchMethodException::class)
    private fun similarMethod(name: String, types: Array<Class<*>>): Method {
        var t: Class<*>? = type
        val methods = mutableListOf<Method>()
        for (m in t!!.methods) if (isSimilarSignature(m, name, types)) methods.add(m)
        if (methods.isNotEmpty()) {
            methods.sortWith(Comparator { o1, o2 -> compareParams(o1.parameterTypes, o2.parameterTypes) })
            return methods[0]
        }
        do {
            for (m in t!!.declaredMethods) if (isSimilarSignature(m, name, types)) methods.add(m)
            if (methods.isNotEmpty()) {
                methods.sortWith(Comparator { o1, o2 -> compareParams(o1.parameterTypes, o2.parameterTypes) })
                return methods[0]
            }
            t = t.superclass
        } while (t != null)

        throw NoSuchMethodException(
            "No similar method $name with params ${types.contentToString()} could be found on type $type."
        )
    }

    private fun isSimilarSignature(
        possiblyMatchingMethod: Method,
        desiredMethodName: String,
        desiredParamTypes: Array<Class<*>>,
    ): Boolean = possiblyMatchingMethod.name == desiredMethodName &&
            match(possiblyMatchingMethod.parameterTypes, desiredParamTypes)

    private fun match(declaredTypes: Array<Class<*>>, actualTypes: Array<Class<*>>): Boolean {
        if (declaredTypes.size != actualTypes.size) return false
        for (i in actualTypes.indices) {
            if (actualTypes[i] == NULL::class.java) continue
            if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) continue
            return false
        }
        return true
    }

    /** 用于构造器/方法排序：参数类型更窄的在前。 */
    private fun compareParams(t1: Array<Class<*>>, t2: Array<Class<*>>): Int {
        for (i in t1.indices) {
            if (t1[i] != t2[i]) {
                return if (wrapper(t1[i]).isAssignableFrom(wrapper(t2[i]))) 1 else -1
            }
        }
        return 0
    }

    private fun <T : AccessibleObject> accessible(accessible: T): T {
        if (accessible is Member) {
            if (Modifier.isPublic(accessible.modifiers) &&
                Modifier.isPublic(accessible.declaringClass.modifiers)
            ) {
                return accessible
            }
        }
        if (!accessible.isAccessible) accessible.isAccessible = true
        return accessible
    }

    // ====================================================================================
    //  proxy
    // ====================================================================================

    /**
     * Create a proxy for the wrapped object allowing to typesafe invoke methods on it using a
     * custom interface.
     *
     * @param proxyType The interface type that is implemented by the proxy.
     * @return a proxy for the wrapped object
     */
    fun <P> proxy(proxyType: Class<P>): P? {
        val isMap = obj is Map<*, *>
        val handler = InvocationHandler { _, method, args ->
            try {
                reflect(obj).method(method.name, *args.orEmpty())?.get<Any?>()
            } catch (e: ReflectException) {
                if (!isMap) throw e
                @Suppress("UNCHECKED_CAST")
                val map = obj as MutableMap<Any?, Any?>
                val name = method.name
                when {
                    args.isNullOrEmpty() && name.startsWith("get") ->
                        map[property(name.substring(3))]
                    args.isNullOrEmpty() && name.startsWith("is") ->
                        map[property(name.substring(2))]
                    args?.size == 1 && name.startsWith("set") -> {
                        map[property(name.substring(3))] = args[0]
                        null
                    }
                    else -> throw e
                }
            }
        }
        return Proxy.newProxyInstance(
            proxyType.classLoader,
            arrayOf<Class<*>>(proxyType),
            handler,
        ) as P?
    }

    // ====================================================================================
    //  取值 / equals / hashCode / toString
    // ====================================================================================

    /** Get the wrapped result. */
    fun <T> get(): T? = obj as T?

    override fun hashCode(): Int = obj?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean =
        other is ReflectUtils && obj == other.obj

    override fun toString(): String = obj?.toString() ?: "null"

    // ====================================================================================
    //  内部工具
    // ====================================================================================

    /** 包装类映射：基本类型 → 包装类型；非基本类型原样返回。 */
    private fun wrapper(type: Class<*>): Class<*> = when {
        !type.isPrimitive -> type
        type == Boolean::class.javaPrimitiveType -> Boolean::class.java
        type == Int::class.javaPrimitiveType -> Int::class.java
        type == Long::class.javaPrimitiveType -> Long::class.java
        type == Short::class.javaPrimitiveType -> Short::class.java
        type == Byte::class.javaPrimitiveType -> Byte::class.java
        type == Double::class.javaPrimitiveType -> Double::class.java
        type == Float::class.javaPrimitiveType -> Float::class.java
        type == Char::class.javaPrimitiveType -> Char::class.java
        type == Void.TYPE -> Void::class.java
        else -> type
    }

    private class NULL

    /**
     * 反射操作异常。所有 API 失败时统一抛出，方便调用方一处 catch。
     */
    class ReflectException : RuntimeException {
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)

        companion object {
            private const val serialVersionUID = 858774075258496016L
        }
    }

    // ====================================================================================
    //  Companion: reflect 入口
    // ====================================================================================

    companion object {

        /**
         * Reflect the class by name.
         *
         * @param className The name of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        fun reflect(className: String): ReflectUtils = reflect(forName(className))

        /**
         * 协程版 [reflect(className)]，在 [Dispatchers.IO] 执行。
         *
         * 适用于类加载可能耗时（首次触发静态初始化）的场景。
         */
        suspend fun reflectAsync(className: String): ReflectUtils =
            withContext(Dispatchers.IO) { reflect(className) }

        /**
         * Reflect the class by name with a custom class loader.
         *
         * @param className The name of class.
         * @param classLoader The loader of class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        fun reflect(className: String, classLoader: ClassLoader?): ReflectUtils =
            reflect(forName(className, classLoader))

        /**
         * Reflect the class.
         *
         * @param clazz The class.
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        fun reflect(clazz: Class<*>): ReflectUtils = ReflectUtils(clazz)

        /**
         * Reflect the object.
         *
         * @param obj The object. `null` 时以 [Any] 为类型。
         * @return the single [ReflectUtils] instance
         * @throws ReflectException if reflect unsuccessfully
         */
        fun reflect(obj: Any?): ReflectUtils =
            ReflectUtils(obj?.javaClass ?: Any::class.java, obj)

        private fun forName(className: String): Class<*> = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw ReflectException(e)
        }

        private fun forName(name: String, classLoader: ClassLoader?): Class<*> = try {
            Class.forName(name, true, classLoader)
        } catch (e: ClassNotFoundException) {
            throw ReflectException(e)
        }

        /** Get the POJO property name of a getter/setter. */
        private fun property(string: String): String = when (string.length) {
            0 -> ""
            1 -> string.lowercase()
            else -> string.substring(0, 1).lowercase() + string.substring(1)
        }
    }
}