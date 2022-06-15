package com.example.dgis_flutter

import android.graphics.BitmapFactory
import io.flutter.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import ru.dgis.sdk.Context
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.*
import ru.dgis.sdk.routing.*
import java.io.ByteArrayInputStream

class GisMapController(gv: MapView, ctx: Context, mom: MapObjectManager) {

    private var gisView = gv
    private var sdkContext = ctx
    private var mapObjectManager = mom

    fun setCameraPosition(call: MethodCall) {
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
                Duration.ofSeconds((args["duration"] as Double).toLong()),
                CameraAnimationType.LINEAR
            )
                .onResult {
                    Log.d("APP", "Перелёт камеры завершён.")
                }
        }
    }

    fun getCameraPosition(result: MethodChannel.Result) {
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

    fun updateMarkers(arguments: Any) {
        val args = arguments as Map<String, Any>;
        val markers = args["markers"] as List<Map<String, Any>>
        val objects: MutableList<SimpleMapObject> = ArrayList();
        for (i in markers) {
            val arrayInputStream = ByteArrayInputStream(i["icon"] as ByteArray?)
            val bitmap = BitmapFactory.decodeStream(arrayInputStream)
            val icon = imageFromBitmap(sdkContext, bitmap)
            val marker = Marker(
                MarkerOptions(
                    position = GeoPointWithElevation(
                        latitude = i["latitude"] as Double,
                        longitude = i["longitude"] as Double,
                    ),
                    icon = icon,
                    zIndex = ZIndex(i["zIndex"] as Int),
                    userData = i["id"],
                )
            )
            objects.add(marker)
        }
        mapObjectManager.removeAll()
        mapObjectManager.addObjects(objects.toList());

    }

    fun setRoute(arguments: Any) {
        val routeEditor = RouteEditor(sdkContext)
        val routeEditorSource = RouteEditorSource(sdkContext, routeEditor)
        gisView.getMapAsync { map ->
            map.addSource(routeEditorSource)
        }
        arguments as Map<String, Any>
        val startPoint = RouteSearchPoint(
            coordinates = GeoPoint(
                latitude = arguments["startLatitude"] as Double,
                longitude = arguments["startLongitude"] as Double
            )
        )
        val finishPoint = RouteSearchPoint(
            coordinates = GeoPoint(
                latitude = arguments["finishLatitude"] as Double,
                longitude = arguments["finishLongitude"] as Double
            )
        )
        routeEditor.setRouteParams(
            RouteEditorRouteParams(
                startPoint = startPoint,
                finishPoint = finishPoint,
                routeSearchOptions = RouteSearchOptions(CarRouteSearchOptions())
            )
        )
    }
}

