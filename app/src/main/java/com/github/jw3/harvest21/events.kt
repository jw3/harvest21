package com.github.jw3.harvest21


import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.Serializable

sealed class Msg
data class Move(val x: Double, val y: Double) : Msg()
data class Signal(val strength: Float, val quality: Float) : Msg()
data class PingDelay(val sz: Delayed) : Msg()
enum class Delayed(val rgb: Int, val style: SimpleMarkerSymbol.Style) {
    None(-0xff0100, SimpleMarkerSymbol.Style.CIRCLE),
    Short(-0x100, SimpleMarkerSymbol.Style.TRIANGLE),
    Long(-0x10000, SimpleMarkerSymbol.Style.X)
}

// Wire format
@Serializable
data class P(val x: Double, val y: Double)

// IPC format
@Parcelize
data class M(val id: String, val x: Double, val y: Double): Parcelable

interface Events {
    val subscribers: ArrayList<Messenger>

    companion object {
        val Sub = 100
        val Move = 900
    }
}

class ProducerHandler(private val e: Events) : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            Events.Sub -> e.subscribers.add(msg.replyTo)
        }
    }
}

class ConsumerHandler(private val bus: SendChannel<M>) : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            Events.Move -> {
                val b = msg.data
                b.getString("id")?.let { id ->
                    b.getParcelable<M>("e")?.let { m ->
                        bus.offer(m)
                    }
                }
            }
        }
    }
}

class EventsServiceConnection(val consumer: Messenger) : ServiceConnection {
    private var messenger: Messenger? = null

    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        messenger = Messenger(service)
        messenger?.send(
            Message.obtain(null, Events.Sub).apply { replyTo = consumer }
        )
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        messenger = null
    }

    override fun onBindingDied(name: ComponentName?) {
        println("died")
    }

    override fun onNullBinding(name: ComponentName?) {
        println("null")
    }
}
