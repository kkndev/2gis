package com.example.dgis_flutter

import ru.dgis.sdk.map.MapView

object GisMapSession {
    private var  mapView : MapView? = null;

    @JvmStatic
    fun setMapView(view: MapView){
        mapView = view
    }

    @JvmStatic
    fun getMapView(): MapView? {
        return mapView;
    }
}