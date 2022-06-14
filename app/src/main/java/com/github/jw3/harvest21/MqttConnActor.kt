package com.github.jw3.harvest21

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import org.eclipse.paho.android.service.MqttAndroidClient

class MqttConnActor(mqttClient: MqttAndroidClient) :
    CoroutineScope by MainScope() {

    @ObsoleteCoroutinesApi
    val actor = actor<MqttMsq> {
        for (e in channel) {
            when (e) {
                is Reconnect -> {
                    if (!mqttClient.isConnected) {
                        mqttClient.connect(e.opts)
                        checkAgainIn(e, 1000, channel)
                    } else {
                        println("=================== connected ===================")
                    }
                }
            }
        }
    }

    private fun checkAgainIn(e: Reconnect, t: Long, channel: Channel<MqttMsq>) = async {
        delay(t)
        channel.offer(e)
    }
}
