package org.muc.mold

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.muc.datakv.IDataKVOwner
import org.muc.datakv.content.EngineProvider
import org.muc.datakv.datakv
import org.muc.eventbus.event.AppEvent
import org.muc.eventbus.event.core.EventBus
import org.muc.eventbus.utils.EventBusCollector
import org.muc.eventbus.utils.sendStackEvent
import org.muc.ui.action.Action
import org.muc.ui.action.ActionManagerImpl
import org.muc.ui.action.ActionView
import org.muc.ui.buttons.MoldButtonType
import org.muc.ui.image.MoldImage
import org.muc.ui.textfields.MoldOutlinedTextField
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TestEvent(val input: String = "") : AppEvent
data class TestStackEvent(val input: String = "") : AppEvent

class MainViewModel : ViewModel() {
    internal val uiState = mutableStateListOf<InfoData>()

    internal data class InfoData(
        val title: String,
        val items: List<InfoItemData>,
        val ext: (@Composable () -> Unit)? = null
    ) {
        internal data class InfoItemData(
            val title: String,
            val onClick: (() -> Unit)? = null,
            val ext: (@Composable () -> Unit)? = null
        )
    }

    init {
        viewModelScope.launch {
            loadEventBus()
            loadDataKv()
            loadUi()
        }
    }

    private fun loadUi() {
        val manager = ActionManagerImpl()
        uiState.add(
            InfoData(
                "ui", listOf(
                    InfoData.InfoItemData("action", {
                        manager.onRequestAction(
                            Action.ActionRequestTips(
                                msg = "确认action吗？",
                                type = MoldButtonType.NEUTRAL,
                                onConfirmAction = {
                                    manager.onConfirmAction()
                                    viewModelScope.launch {
                                        delay(3.seconds)
                                        manager.onCancelAction()
                                    }
                                },
                                content = {
                                    Text("发布后即生效，并且无法再重置成之前的状态")
                                })
                        )
                    }) {
                        ActionView(manager)
                    },
                    InfoData.InfoItemData("Image") {
                        MoldImage("https://saas.jf-r.com/pcweb/img/t1_1.png")
                    },
                    InfoData.InfoItemData("TextField") {
                        var value by remember { mutableStateOf("") }
                        MoldOutlinedTextField(value, { value = it })
                    }
                )
            )
        )
    }

    private fun loadDataKv() {
        val store = object : IDataKVOwner by EngineProvider {
            val testCount by datakv(0)
            val expireCount by datakv(0)
            val cpCountPt by datakv(0, cross = true)
        }
        uiState.add(
            InfoData(
                "DataKV", listOf(
                    InfoData.InfoItemData("testCount++", onClick = {
                        viewModelScope.launch {
                            store.testCount.setValue { it + 1 }
                        }
                    }) {
                        val testCount by store.testCount.flow.collectAsState(null)
                        Text("当前testCount的值${testCount}")
                    },
                    InfoData.InfoItemData("expireCount++", onClick = {
                        viewModelScope.launch {
                            store.expireCount.setValue(3.seconds) {
                                it + 1
                            }
                        }
                    }) {
                        val expireCount by store.expireCount.flow.collectAsState(null)
                        val expireDuration by store.expireCount.expireTimeDurationFlow.collectAsState(Duration.ZERO)
                        Text("${expireDuration}后过期，当前expireCount的值${expireCount}")
                    },
                    InfoData.InfoItemData("cpCountPt++", onClick = {
                        viewModelScope.launch {
                            store.cpCountPt.setValue(5.seconds) {
                                it + 1
                            }
                        }
                    }) {
                        val testCount by store.cpCountPt.flow.collectAsState(null)
                        val expireDuration by store.cpCountPt.expireTimeDurationFlow.collectAsState(Duration.ZERO)
                        Text("${expireDuration}后过期，当前testCount的值${testCount}")
                    },
                )
            )
        )
    }

    private fun loadEventBus() {
        uiState.add(
            InfoData(
                "EventBus", listOf(
                    InfoData.InfoItemData("发送事件", onClick = {
                        viewModelScope.launch {
                            EventBus.send(TestEvent((1..100).random().toString()))
                        }
                    }) {
                        var testEvent by remember {
                            mutableStateOf<TestEvent?>(null)
                        }
                        EventBus.EventBusCollector<TestEvent> {
                            testEvent = it
                        }
                        if (testEvent != null)
                            AlertDialog({ testEvent = null }, title = {
                                Text(testEvent.toString())
                            }, confirmButton = {
                                Button({ testEvent = null }) {
                                    Text("确定")
                                }
                            })
                    },
                    InfoData.InfoItemData("发送粘性事件", onClick = {
                        viewModelScope.launch {
                            EventBus.sendStackEvent(TestStackEvent((1..100).random().toString()))
                        }
                    }) {
                        var testEvent by remember {
                            mutableStateOf<TestStackEvent?>(null)
                        }
                        Button({
                            EventBus.subscribeSticky<TestStackEvent>(
                                scope = viewModelScope,
                                onEvent = { testEvent = it }
                            )
                        }) {
                            Text("开始收集")
                        }
                        if (testEvent != null)
                            AlertDialog({ testEvent = null }, title = {
                                Text(testEvent.toString())
                            }, confirmButton = {
                                Button({ testEvent = null }) {
                                    Text("确定")
                                }
                            })
                    },
                )
            )
        )
    }
}