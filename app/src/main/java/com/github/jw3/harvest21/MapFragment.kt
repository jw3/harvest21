package com.github.jw3.harvest21

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Messenger
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor

class MapFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var svcConnection: ServiceConnection

    private val locationsLayer = GraphicsOverlay()
    private val graphicsById = mutableMapOf<String, GraphicActor>()

    private val symbolbus = actor<M> {
        var eventCount: Long = 0
        for (e in channel) {
            graphicsById.getOrPut(e.id, {
                GraphicActor.add(e.id, locationsLayer)
            }).apply {
                actor.offer(Move(e.x, e.y))
                //eventCounterTxt.text = "${eventCount++}"
            }
        }
    }
    @ObsoleteCoroutinesApi
    val consumer = Messenger(ConsumerHandler(symbolbus))

    @ObsoleteCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = inflater.inflate(R.layout.fragment_map, container, false)
        fragment.findViewById<com.esri.arcgisruntime.mapping.view.MapView>(R.id.mapView)?.let {
            it.mapView.map = ArcGISMap(SpatialReference.create(26917))
            it.mapView.locationDisplay.isShowLocation = true
            it.mapView.locationDisplay.startAsync()
        }

        svcConnection = EventsServiceConnection(consumer)
        val ctx = requireContext().applicationContext
        ctx.bindService(Intent(ctx, TheService::class.java), svcConnection, Context.BIND_AUTO_CREATE)

        return fragment
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}