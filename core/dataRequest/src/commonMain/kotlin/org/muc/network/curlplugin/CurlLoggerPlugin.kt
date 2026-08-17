@file:Suppress("unused")

package org.muc.network.curlplugin

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.utils.io.InternalAPI
import kotlinx.atomicfu.atomic
import kotlinx.serialization.serializer
import org.muc.network.di.json
import org.muc.network.di.nowMillis
import org.muc.network.curlplugin.history.HistoryStore
import org.muc.network.curlplugin.history.HttpHistoryEntry

@OptIn(InternalAPI::class)
fun curlLoggerPlugin(historyStore: HistoryStore): ClientPlugin<HistoryStore> =
    createClientPlugin("CurlLogger", { historyStore }) {
        val nextHistoryId = atomic(-1L)
        // 1. 定义 Key
        val historyIdKey = AttributeKey<Long>("HistoryEntryId")
        val curlKey = io.ktor.util.AttributeKey<String>("CurlCommand")
        val startTimeKey = io.ktor.util.AttributeKey<Long>("StartTime")
        onRequest { request, _ ->
            // 构建 curl 命令
            val startedAtMs = nowMillis()
            val currentId = nextHistoryId.getAndDecrement()
            val curl = buildString {
                append("curl --location --request ${request.method.value}")
                appendLine(" \"${request.url}\"")
                // 拼接 headers
                request.headers.entries().forEach { (name, values) ->
                    values.forEach { value ->
                        appendLine(" --header \"$name: $value\"")
                    }
                }

                // Body（安全处理，不崩溃）
                if (request.method != HttpMethod.Get) {
                    val body = request.body
                    runCatching {
                        if (body is EmptyContent) return@runCatching
                        if (body is MultiPartFormDataContent) {
                            body.parts.forEach { part ->
                                val disposition = part.headers[HttpHeaders.ContentDisposition] ?: ""
                                val nameMatch = Regex("name=\"([^\"]+)\"").find(disposition)
                                val filenameMatch = Regex("filename=\"([^\"]+)\"").find(disposition)
                                when {
                                    // 有文件名：文件字段（用 @ 指向文件路径，ApiFox 支持识别）
                                    nameMatch != null && filenameMatch != null -> {
                                        val fieldName = nameMatch.groupValues[1]
                                        val filePath = filenameMatch.groupValues[1]
                                        // 转义文件路径中的特殊字符（双引号、空格、单引号）
                                        val escapedFilePath = filePath
                                            .replace("\"", "\\\"")
                                            .replace("'", "\\'")
                                            .replace(" ", "\\ ")
                                        // 格式：-F "fieldName=@文件路径;filename=文件名"
                                        appendLine("--form \"$fieldName=@$escapedFilePath\"")
                                    }
                                    // 无文件名：普通表单字段
                                    nameMatch != null -> {
                                        val fieldName = nameMatch.groupValues[1]
                                        val fieldValue = part.name
                                        appendLine("--form \"$fieldName=$fieldValue\"")
                                    }
                                }
                            }
                            return@runCatching
                        }
                        if (body is OutgoingContent) {
                            appendLine("--form \"$body\"")
                            return@runCatching
                        }
                        val serializer = json.serializersModule.serializer(request.bodyType?.kotlinType!!)
                        val escaped = json.encodeToString(serializer, body).replace("'", "'\"'\"'")
                        appendLine(" --data-raw '$escaped'")
                    }.onFailure {
                        appendLine(" --data-raw '$body'")
                    }
                }
            }
            println("start request:$currentId")
            println(curl)
            request.attributes.put(historyIdKey, currentId)
            request.attributes.put(curlKey, curl)
            request.attributes.put(startTimeKey, startedAtMs)
        }
        onResponse { response ->
            val attrs = response.call.attributes
            val historyId = attrs.getOrNull(historyIdKey) ?: return@onResponse
            val curl = attrs.getOrNull(curlKey) ?: ""
            val startedAtMs = attrs.getOrNull(startTimeKey) ?: nowMillis()
            val durationMs = nowMillis() - startedAtMs
            println("end request:$historyId  durationMs:$durationMs")
            val responseBodyString = runCatching { response.bodyAsText() }.getOrElse { "Could not read response body: ${it.message}" }
            val formattedJson = runCatching {
                val jsonElement = json.parseToJsonElement(responseBodyString)
                json.encodeToString(jsonElement)
            }.getOrElse {
                responseBodyString
            }
            val request = response.call.request
            historyStore.append(
                HttpHistoryEntry(
                    id = historyId,
                    timestampEpochMs = startedAtMs,
                    method = request.method.value,
                    url = request.url.toString(),
                    curl = curl,
                    responseBodyString = formattedJson,
                    durationMs = nowMillis() - startedAtMs,
                    httpCode = response.status.value,
                    shortError = null,
                )
            )
        }
    }