package com.github.jw3.harvest21.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jw3.harvest21.prefs.BrokerPrefs
import com.github.jw3.harvest21.prefs.DevicePrefs
import org.eclipse.paho.android.service.MqttService
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import javax.inject.Inject

@HiltWorker
class MqttReconnector(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    @Inject
    lateinit var device: DevicePrefs

    @Inject
    lateinit var prefs: BrokerPrefs

    @Inject
    lateinit var mqtt: MqttService

    override fun doWork(): Result {
        val opts = MqttConnectOptions()
        opts.userName = prefs.user
        opts.password = prefs.pass
        opts.keepAliveInterval = 10
        opts.isAutomaticReconnect = true
        opts.maxReconnectDelay = 30 * 1000

        mqtt.connect(device.id, opts, "foo", "bar")

        val x= if (mqtt.isConnected(device.id)) Result.success() else Result.failure()
        return x
    }
}
