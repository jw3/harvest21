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
import com.github.jw3.harvest21.prefs.BrokerPrefs
import com.github.jw3.harvest21.prefs.DevicePrefs
import com.github.jw3.harvest21.prefs.MapPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    @Inject lateinit var map: MapPrefs
    @Inject lateinit var prefs: BrokerPrefs
    @Inject lateinit var device: DevicePrefs

    override val subscribers = ArrayList<Messenger>()
    private lateinit var messenger: Messenger

    var lastPos: Point? = null
    private lateinit var svcConnection: ServiceConnection

    @ExperimentalSerializationApi
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
                        alds.addLocationChangedListener { moved ->
                            val loc = moved.location
                            val m = P(loc.position.x, loc.position.y)
                            val payload = Json.encodeToString(m)
                            tok.client.publish("${device.id}/m", MqttMessage(payload.toByteArray()))
                            Toast.makeText(applicationContext, "moved: ✅", Toast.LENGTH_SHORT).show()
                        }
                        alds.startAsync()
                        Toast.makeText(applicationContext, "connected ✅", Toast.LENGTH_SHORT).show()

                        // topic: device-id/m
                        // topic: device-id/+
                        // topic: +/m
                        // device-id first allows simple substring to parse the id
                        tok.client.subscribe("+/m", 0).let { sub ->
                            sub.client.setCallback(object : MqttCallback {
                                override fun messageArrived(topic: String?, message: MqttMessage?) {
                                    println("received $topic ${message.toString()}")
                                    topic?.split("/", limit = 2)?.first()?.let { id ->
                                        if(id != device.id || map.echoLocation) {
                                            val payload = message?.payload?.let { String(it) }
                                            payload?.let { encoded ->
                                                val e = Json.decodeFromString<P>(encoded)
                                                val b = Bundle()
                                                b.putString("id", id)
                                                b.putParcelable("e", M(id, e.x, e.y))
                                                val m: Message = Message.obtain(null, Events.Move).also { it.data = b }
                                                subscribers.forEach { it.send(m) }
                                            }
                                        }
                                    }
                                }

                                override fun connectionLost(cause: Throwable?) {
                                    println("================== connection lost ==================")
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
