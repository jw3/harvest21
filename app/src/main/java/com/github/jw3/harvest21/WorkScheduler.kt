package com.github.jw3.harvest21

import android.content.Context
import androidx.work.*
import com.github.jw3.harvest21.work.MqttReconnector
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// schedules work on the WorkManager
@Singleton
class WorkScheduler @Inject constructor (@ApplicationContext var context: Context) {
    val MqttReconnect = "__mqtt__reconnect__"
    val constraintNetwork = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleMqttReconnect(user: String, pass: String) {
        val mqttReconnectWorkRequest = OneTimeWorkRequest.Builder(MqttReconnector::class.java)
            .addTag(MqttReconnect)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.SECONDS)
            .setConstraints(constraintNetwork)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(MqttReconnect, ExistingWorkPolicy.REPLACE, mqttReconnectWorkRequest)
    }
}
