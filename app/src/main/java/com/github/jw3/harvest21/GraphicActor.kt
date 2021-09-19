package com.github.jw3.harvest21

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay

class GraphicActor private constructor(val id: String) : CoroutineScope by MainScope() {
    private val symbol = SimpleMarkerSymbol(Delayed.None.style, Delayed.None.rgb, 15.0f)
    private val graphic = Graphic(Point(0.0, 0.0, geo.wgs84), symbol)
    val actor = actor<Msg> {
        var timer = lastPingAsync(0, channel)

        for (e in channel) {
            when (e) {
                is Move -> {
                    timer.cancel()
                    timer = lastPingAsync(System.currentTimeMillis(), channel)
                    symbol.color = Delayed.None.rgb
                    symbol.style = Delayed.None.style
                    graphic.geometry = Point(e.x, e.y, geo.wgs84)
                }
                is PingDelay -> {
                    symbol.color = e.sz.rgb
                    symbol.style = e.sz.style
                }
                is Signal -> println(e.strength)
            }
        }
    }

    private fun lastPingAsync(t: Long, channel: Channel<Msg>) = async {
        val millis: Long = 1000 * 30
        delay(millis)
        channel.send(PingDelay(Delayed.Short))
        delay(millis * 4)
        channel.send(PingDelay(Delayed.Long))
    }

    companion object {
        fun add(id: String, layer: GraphicsOverlay): GraphicActor {
            val new = GraphicActor(id)
            layer.graphics.add(new.graphic)
            return new
        }
    }
}
