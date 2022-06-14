package com.github.jw3.harvest21

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject




@HiltAndroidApp
class TheApp : Application() {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    fun getWorkManagerConfiguration(): Configuration? {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
