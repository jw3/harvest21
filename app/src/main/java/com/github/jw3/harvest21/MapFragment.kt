package com.github.jw3.harvest21

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Messenger
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.github.jw3.harvest21.map.basemaps
import com.github.jw3.harvest21.prefs.MapPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.yield
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), CoroutineScope by MainScope() {
    @Inject lateinit var prefs: MapPrefs

    private lateinit var svcConnection: ServiceConnection

    private val locationsLayer = GraphicsOverlay()
    private val graphicsById = mutableMapOf<String, GraphicActor>()

    private val symbolbus = actor<M> {
        var eventCount: Long = 0
        for (e in channel) {
            graphicsById.getOrPut(e.id, {
                GraphicActor.add(e.id, locationsLayer)
            }).apply {
                yield()
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
        val applicationContext = requireContext()

        // todo;; pref
        val initialScale = 10000.0

        // todo;; pref (full path)
        val basemapPath = applicationContext.filesDir

        val fragment = inflater.inflate(R.layout.fragment_map, container, false)
        fragment.findViewById<com.esri.arcgisruntime.mapping.view.MapView>(R.id.mapView)?.let { frag ->
            val pos: Point = currentLocation(applicationContext)?.let { Point(it.longitude, it.latitude, geo.wgs84) } ?: geo.pt0
            frag.mapView.map = basemaps.fromStorage(basemapPath)?.let { base ->
                val m = ArcGISMap(geo.sr)
                m.basemap = base
                m.initialViewpoint = Viewpoint(pos, initialScale)
                m
            } ?: ArcGISMap(Basemap.Type.IMAGERY, pos.x, pos.y, 15)

            frag.mapView.locationDisplay.isShowLocation = prefs.echoLocation.not()
            if(prefs.autoPan) {
                frag.mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
            }

            frag.mapView.graphicsOverlays.add(locationsLayer)
            frag.mapView.locationDisplay.startAsync()
        }

        svcConnection = EventsServiceConnection(consumer)
        applicationContext.bindService(
            Intent(applicationContext, TheService::class.java),
            svcConnection,
            Context.BIND_AUTO_CREATE
        )

        return fragment
    }

    private fun currentLocation(context: Context): Location? {
        // todo;; inject location manager
        val loc = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            loc.getLastKnownLocation(loc.getProviders(true).first())
        } catch (e: SecurityException) {
            Log.w("gps", "unable to get initial location")
            null
        } catch (e: NoSuchElementException) {
            Log.w("gps", "unable to get initial location")
            null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}
