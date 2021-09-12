package com.github.jw3.harvest21

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.widget.Toast
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.location.AndroidLocationDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ImplicitReflectionSerializer
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject


/**
 * GeoService
 * Two responsibilities
 * 1. Sending the device position
 * 2. Distributing received positions
 */
@AndroidEntryPoint
class TheService : Service(), Events {
    @Inject lateinit var prefs: BrokerPrefs
    @Inject lateinit var device: DevicePrefs

    override val subscribers = ArrayList<Messenger>()
    private lateinit var messenger: Messenger

    var lastPos: Point? = null
    private lateinit var svcConnection: ServiceConnection



    @ImplicitReflectionSerializer
    override fun onCreate() {
        super.onCreate()

        val msg = "connecting to ${prefs.url} as ${prefs.user}"

        try {
            val client = MqttAndroidClient(this, "ssl://${prefs.url}:443", device.id)
            val opts = MqttConnectOptions()
            opts.userName = prefs.user
            opts.password = prefs.pass

            client.connect(opts, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    asyncActionToken?.let { tok ->
                        val alds = AndroidLocationDataSource(applicationContext,"gps", 5L, 1f)
                        alds.addLocationChangedListener { _ ->
                            tok.client.publish("mov", MqttMessage("moved".toByteArray()))
                            Toast.makeText(applicationContext, "moved: ✅", Toast.LENGTH_SHORT).show()
                        }
                        alds.startAsync()
                        Toast.makeText(applicationContext, "connected ✅", Toast.LENGTH_SHORT).show()


                        tok.client.subscribe("mov", 0).let { sub ->
                            sub.client.setCallback(object : MqttCallback {
                                override fun connectionLost(cause: Throwable?) {
                                }

                                override fun messageArrived(topic: String?, message: MqttMessage?) {
                                    println("received $topic ${message.toString()}")

                                    message?.payload.contentToString().let { encoded ->
                                        if (!encoded.startsWith(device.id)) {
                                            val (id, x, y) = encoded.split(":")
                                            val b = Bundle()
                                            b.putString("id", id)
                                            b.putDouble("x", x.toDouble())
                                            b.putDouble("y", y.toDouble())
                                            val m: Message = Message.obtain(null, Events.Move)
                                            m.data = b
                                            subscribers.forEach { it.send(m) }
                                        }
                                    }
                                }

                                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                                }
                            })
                        }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(applicationContext, "$msg\n❗${exception?.cause?.message}", Toast.LENGTH_LONG).show()
                }
            })


        } catch (e: Exception) {
            Toast.makeText(applicationContext, "$msg\n❗${e.cause?.message}", Toast.LENGTH_LONG).show()
            return
        }
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(ProducerHandler(this@TheService))
        return messenger.binder
    }
}