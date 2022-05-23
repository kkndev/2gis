package com.example.dgis_flutter

import android.content.Context
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.*
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.MapView

internal class NativeView(context: Context, id: Int, creationParams: Map<String, Any?>?) :
    PlatformView {
    private var sdkContext: ru.dgis.sdk.Context = DGis.initialize(
        context, ApiKeys(
            directory = creationParams?.get("directory") as String, //"rubyqf9316",
            map =  creationParams["map"] as String //"b7272230-6bc3-47e9-b24b-0eba73b12fe1"
        ),
    )
    private var gisView: MapView


    override fun getView(): MapView {
        return gisView
    }

    override fun dispose() {}

    init {
        val mapOptions = MapOptions()
        gisView = MapView(context, mapOptions);
    }

}