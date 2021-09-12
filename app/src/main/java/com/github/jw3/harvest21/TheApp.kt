package com.github.jw3.harvest21

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TheApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}