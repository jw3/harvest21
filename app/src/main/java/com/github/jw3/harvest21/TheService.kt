package com.github.jw3.harvest21

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.widget.Toast
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.location.AndroidLocationDataSource
import com.esri.arcgisruntime.location.LocationDataSource
import com.github.jw3.harvest21.prefs.BrokerPrefs
import com.github.jw3.harvest21.prefs.DevicePrefs
import com.github.jw3.harvest21.prefs.MapPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttService
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject
import kotlin.math.roundToInt


/**
 * GeoService
 * Two responsibilities
 * 1. Sending the device position
 * 2. Distributing received positions
 */
@AndroidEntryPoint
class TheService : Service(), Events {
    @Inject
    lateinit var map: MapPrefs

    @Inject
    lateinit var prefs: BrokerPrefs

    @Inject
    lateinit var device: DevicePrefs

    @Inject
    lateinit var scheduler: WorkScheduler


    override val subscribers = ArrayList<Messenger>()
    private lateinit var messenger: Messenger

    private var lastLocation: Point? = null
    private lateinit var svcConnection: ServiceConnection

    private lateinit var mqttClient: MqttAndroidClient
    private lateinit var locationListener: AndroidLocationDataSource

    override val state = HashMap<String, Message>()

    private lateinit var connActor: MqttConnActor

    @ExperimentalSerializationApi
    override fun onCreate() {
        super.onCreate()


        val msg = "connecting to ${prefs.url} as ${prefs.user}"

        //mqttClient = MqttAsyncClient("ssl://${prefs.url}:443", device.id, MemoryPersistence())
        mqttClient = MqttAndroidClient(
            this.applicationContext,
            "ssl://${prefs.url}:443",
            device.id,
            MemoryPersistence()
        )
        connActor = MqttConnActor(mqttClient)



        val opts = MqttConnectOptions()
        opts.userName = prefs.user
        opts.password = prefs.pass
        opts.keepAliveInterval = 10
        opts.connectionTimeout = 5
        opts.isAutomaticReconnect = true
        opts.maxReconnectDelay = 30 * 1000



        val client = mqttClient.connect(opts)
        client.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                initializeLocationServices(opts)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, e: Throwable?) {
                Toast.makeText(applicationContext, "$msg\n❗${e?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun initializeLocationServices(opts: MqttConnectOptions) {
        // topic: device-id/m
        // topic: device-id/+
        // topic: +/m
        // device-id first allows simple substring to parse the id
        mqttClient.subscribe("+/m", 1)
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                topic?.split("/", limit = 2)?.first()?.let { id ->
                    if (id != device.id || map.echoLocation) {
                        val payload = message?.payload?.let { String(it) }
                        payload?.let { encoded ->
                            val e = Json.decodeFromString<P>(encoded)
                            val b = Bundle()
                            b.putString("id", id)
                            b.putParcelable("e", M(id, e.x.toDouble(), e.y.toDouble()))
                            val m: Message = Message.obtain(null, Events.Move).also { it.data = b }
                            state[id] = m
                            subscribers.forEach { it.send(m) }
                        }
                    }
                }
                println("received $topic ${message.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Toast.makeText(applicationContext, "connected ✅", Toast.LENGTH_SHORT).show()
            }

            override fun connectionLost(cause: Throwable?) {
                Toast.makeText(applicationContext, "❗ connection lost ❗", Toast.LENGTH_LONG).show()
                connActor.actor.offer(Reconnect(opts))
            }
        })

        // todo;; pref map_min_move_distance
        val minMoveDistance = 1f

        // todo;; pref map_min_ping_interval
        val minPingInterval = 10 * 1000L

        locationListener =
            AndroidLocationDataSource(applicationContext, "gps", minPingInterval, minMoveDistance)
        locationListener.addLocationChangedListener { moved -> handleLocalMovement(moved) }
        locationListener.startAsync()
    }

    fun handleLocalMovement(moved: LocationDataSource.LocationChangedEvent) {
        // todo;; pref map_min_move_distance
        val minMoveDistance = 1f

        // todo;; pref map_move_resolution
        val moveResolution = 5

        try {
            val here = moved.location.position
            when (lastLocation) {
                null -> {
                    val payload = makePayload(here, moveResolution)
                    mqttClient.publish("${device.id}/m", MqttMessage(payload.toByteArray()))
                    Toast.makeText(applicationContext, "first move ✅", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    val d = GeometryEngine.distanceBetween(here, lastLocation)
                    val payload = makePayload(here, moveResolution)
                    mqttClient.publish("${device.id}/m", MqttMessage(payload.toByteArray()))
                    Toast.makeText(
                        applicationContext,
                        "move ${d.roundToInt()}m ✅",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            lastLocation = here
        } catch (e: Exception) {
            val msg = "connecting to ${prefs.url} as ${prefs.user}"
            Toast.makeText(
                applicationContext,
                "$msg\n❗${e.message} ${mqttClient.isConnected}",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(ProducerHandler(this@TheService))
        return messenger.binder
    }

    companion object {
        @ExperimentalSerializationApi
        fun makePayload(pt: Point, res: Int): String {
            val x = String.format("%.${res}f", pt.x)
            val y = String.format("%.${res}f", pt.y)
            return Json.encodeToString(P(x, y))
        }
    }
}
