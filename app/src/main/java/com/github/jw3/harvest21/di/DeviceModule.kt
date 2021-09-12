package com.github.jw3.harvest21.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.github.jw3.harvest21.prefs.DevicePrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class, ServiceComponent::class)
class DeviceModule {
    @Provides
    fun providePrefs(@ApplicationContext applicationContext: Context): DevicePrefs {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val id = prefs.getString("device_uid", "androidz").let {
            if(it.isNullOrBlank()) "unknown" else it
        }
        return DevicePrefs.newInstance(id)
    }
}