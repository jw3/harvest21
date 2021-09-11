package com.github.jw3.harvest21

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_map.view.*

class MapFragment : Fragment() {
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
        return fragment
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}