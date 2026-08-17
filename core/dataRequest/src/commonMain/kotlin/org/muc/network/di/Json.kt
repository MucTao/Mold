@file:Suppress("unused")

package org.muc.network.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.datetime.serializers.LocalTimeIso8601Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.time.Clock

val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
val json by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Json {
        serializersModule = SerializersModule {
            contextual(LocalDateIso8601Serializer)
            contextual(LocalTimeIso8601Serializer)
        }
        prettyPrint = true //json格式化
        isLenient = true //宽松解析，json格式异常也可解析，如：{name:"小红",age:"18"} + Person(val name:String,val age:Int) ->Person("小红",18)
        ignoreUnknownKeys = true //忽略未知键，如{"name":"小红","age":"18"} ->Person(val name:String)
        coerceInputValues = true //强制输入值，如果json属性与对象格式不符，则使用对象默认值，如：{"name":"小红","age":null} + Person(val name:String = "小绿"，val age:Int = 18) ->Person("小红",18)
        encodeDefaults = true //编码默认值,默认情况下，默认值的属性不会参与序列化，通过设置encodeDefaults = true,可让默认属性参与序列化(可参考上述例子)
        explicitNulls = true //序列化时是否忽略null
        allowStructuredMapKeys = true //允许结构化映射(map的key可以使用对象)
        allowSpecialFloatingPointValues = true //特殊浮点值：允许Double为NaN或无穷大
    }
}
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
fun String.parseToJsonElement(): JsonElement = json.parseToJsonElement(this)
inline fun <reified T> String.decodeToJson(): T? = if (this.isBlank()) null else json.decodeFromString(this)
inline fun <reified T> T.encodeToJsonString(): String = json.encodeToString(this)