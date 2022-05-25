package com.example.dgis_flutter

import android.content.Context
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.*
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.map.*

internal class NativeView(
    context: Context,
    id: Int,
    creationParams: Map<String, Any?>?,
    messenger: BinaryMessenger
) :
    PlatformView, MethodChannel.MethodCallHandler {
    private var methodChannel: MethodChannel;
    private var sdkContext: ru.dgis.sdk.Context = DGis.initialize(
        context,
        ApiKeys(
            directory = creationParams?.get("directory") as String,
            map = creationParams["map"] as String
        ),
    )
    private var gisView: MapView

    override fun getView(): MapView {
        return gisView
    }


    override fun dispose() {}

    init {
        val mapOptions = MapOptions()
        val startPoint = GeoPoint(
            latitude = Latitude(creationParams?.get("latitude") as Double),
            longitude = Longitude(creationParams["longitude"] as Double),
        )
        mapOptions.position = CameraPosition(
            point = startPoint,
            zoom = Zoom((creationParams["zoom"] as Double).toFloat()),
            tilt = Tilt((creationParams["tilt"] as Double).toFloat()),
            bearing = Bearing((creationParams["bearing"] as Double))
        )
        methodChannel = MethodChannel(messenger, "fgis");
        methodChannel.setMethodCallHandler(this);
        gisView = MapView(context, mapOptions);

    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "getCameraPosition") {
            getCameraPosition(result = result)
        } else if (call.method == "setCameraPosition") {
            setCameraPosition(call = call)
        }
    }

    private fun setCameraPosition(call: MethodCall) {
        val args: Map<String, Any?> = call.arguments as Map<String, Any?>
        val cameraPosition = CameraPosition(
            GeoPoint(
                latitude = Latitude(value = args["latitude"] as Double),
                longitude = Longitude(value = args["longitude"] as Double)
            ),
            zoom = Zoom(value = (args["zoom"] as Double).toFloat()),
            bearing = Bearing(value = args["bearing"] as Double),
            tilt = Tilt(value = (args["tilt"] as Double).toFloat())
        )
        gisView.getMapAsync { map ->
            map.camera.move(
                cameraPosition,
                Duration.ofSeconds((args["duration"] as Int).toLong()),
                CameraAnimationType.LINEAR
            )
                .onResult {
                    Log.d("APP", "Перелёт камеры завершён.")
                }
        }
    }

    private fun getCameraPosition(result: MethodChannel.Result) {
        lateinit var cameraPosition: CameraPosition;
        gisView.getMapAsync { map ->
            cameraPosition = map.camera.position;
            val data = mapOf(
                "latitude" to cameraPosition.point.latitude.value,
                "longitude" to cameraPosition.point.longitude.value,
                "bearing" to cameraPosition.bearing.value,
                "tilt" to cameraPosition.tilt.value,
                "zoom" to cameraPosition.zoom.value,
            )
            result.success(data);
        }
    }


}