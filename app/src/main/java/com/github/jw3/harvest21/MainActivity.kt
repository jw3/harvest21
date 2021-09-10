package com.github.jw3.harvest21

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val client =
                MqttAndroidClient(applicationContext, "ssl://xxxx.xxxx.xxxx:443", "androidz")
            val opts = MqttConnectOptions()
            opts.userName = "xxx"
            opts.password = "xxx".toCharArray()

            client.connect(opts, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    asyncActionToken?.let {
                        it.client.publish("test", MqttMessage("foo".toByteArray()))
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.printStackTrace()
                    theText.text = exception?.message
                }
            })

        } catch (e: Exception) {
            theText.text = e.message
            return
        }
    }
}