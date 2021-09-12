package com.github.jw3.harvest21

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class, ServiceComponent::class)
object BrokerModule {
    @Provides
    fun provideBrokerPrefs(@ApplicationContext applicationContext: Context): BrokerPrefs {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        return BrokerPrefs.newInstance(
            prefs.getString("broker_url", "localhost")!!,
            prefs.getString("broker_user", "admin")!!,
            prefs.getString("broker_pass", "admin")?.toCharArray()!!
        )
    }
}
