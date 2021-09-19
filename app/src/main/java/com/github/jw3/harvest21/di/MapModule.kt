package com.github.jw3.harvest21.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.github.jw3.harvest21.prefs.MapPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(FragmentComponent::class, ServiceComponent::class)
class MapModule {
    @Provides
    fun providePrefs(@ApplicationContext applicationContext: Context): MapPrefs {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        return MapPrefs.newInstance(
            prefs.getBoolean("map_auto_pan", false),
            prefs.getBoolean("map_echo_location", false)
        )
    }
}
