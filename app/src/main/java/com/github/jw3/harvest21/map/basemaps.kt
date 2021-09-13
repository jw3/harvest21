package com.github.jw3.harvest21.map

import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import java.io.File
import java.nio.file.Paths

object basemaps {
    fun fromStorage(dir: File?): Basemap? {
        val basemap = Basemap()
        dir?.let { GeoPackage(Paths.get(it.path, "mvf-basemap-v0.0.4.gpkg").toString()) }
            .also { it?.loadAsync() }.also { gpkg ->
                gpkg?.addDoneLoadingListener {
                    if (gpkg.loadStatus === LoadStatus.LOADED) {
                        val baseFeatures =
                            gpkg.geoPackageFeatureTables.filter { l ->
                                when (l.tableName.split("-").last()) {
                                    "roads" -> true
                                    "landcover" -> true
                                    "fields" -> true
                                    "places" -> true
                                    else -> false
                                }
                            }.map { t -> FeatureLayer(t) }
                                .sortedBy { it.name }

                        baseFeatures.forEach { fl ->
                            when (fl.name.split("-").last()) {
                                "roads" -> {
                                    fl.isScaleSymbols = true
                                    fl.renderer = roadRenderer
                                }
                                "landcover" -> fl.renderer = woodlandRenderer
                                //"terrain" -> fl.renderer = terrainRenderer
                                "fields" -> fl.renderer = fieldRenderer
                                "places" -> fl.renderer = placesRenderer
                            }
                        }
                        baseFeatures.forEach { fl ->
                            basemap.baseLayers.add(fl)
                        }
                    }
                    // else error logged
                }
            }
        return basemap
    }

    val roadRenderer = SimpleRenderer(
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF000000.toInt(), 2f)
    )

    val woodlandRenderer = SimpleRenderer(
        SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID,
            0xAA5dd272.toInt(), SimpleLineSymbol()
        )
    )

    val terrainRenderer = SimpleRenderer(
        SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xAA808080.toInt(), 1f)
    )

    val fieldRenderer = SimpleRenderer(
        SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID,
            0xFFff4500.toInt(), SimpleLineSymbol()
        )
    )

    val placesRenderer = SimpleRenderer(
        SimpleFillSymbol(
            SimpleFillSymbol.Style.BACKWARD_DIAGONAL,
            0xAAffff00.toInt(), SimpleLineSymbol()
        )
    )
}