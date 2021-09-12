package com.github.jw3.harvest21

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.geometry.SpatialReferences

object geo {
    val sr = SpatialReference.create(26917)
    val wgs84 = SpatialReferences.getWgs84()
    val pt0 = Point(0.0, 0.0, sr)


    fun location(ctx: Context): Location? {
        val m = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            m.getLastKnownLocation(m.getProviders(true).first())
        } catch (e: SecurityException) {
            Log.w("gps", "unable to get initial location")
            null
        }
    }
}