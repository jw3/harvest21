package com.github.jw3.harvest21

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val deviceId = prefs.getString("device_uid", "")
        val brokerUrl = prefs.getString("broker_url", "")
        val brokerUser = prefs.getString("broker_user", "")
        val brokerPass = prefs.getString("broker_pass", "")

        theText.text = "connecting to $brokerUrl as $brokerUser"

        try {
            val client = MqttAndroidClient(this, "ssl://$brokerUrl:443", deviceId)
            val opts = MqttConnectOptions()
            opts.userName = brokerUser
            opts.password = brokerPass?.toCharArray()

            client.connect(opts, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    asyncActionToken?.let {
                        it.client.publish("test", MqttMessage("!!!!".toByteArray()))
                        theText.text = "${theText.text} ✅"
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    theText.text = "${theText.text}\n❗${exception?.cause?.message}"
                }
            })
        } catch (e: Exception) {
            theText.text = e.message
            return
        }
    }
}
