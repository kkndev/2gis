package com.example.dgis_flutter

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.ApiKeys
import ru.dgis.sdk.DGis
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.*
import ru.dgis.sdk.positioning.registerPlatformMagneticSource
import java.io.ByteArrayInputStream
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ru.dgis.sdk.positioning.registerPlatformLocationSource

internal class NativeView(
    context: Context,
    id: Int,
    creationParams: Map<String, Any?>?,
    messenger: BinaryMessenger
) :
    PlatformView, MethodChannel.MethodCallHandler {
    private var methodChannel: MethodChannel;
    private lateinit var mapObjectManager: MapObjectManager;
    private var gisView: MapView
    private lateinit var controller : GisMapController


    private var sdkContext: ru.dgis.sdk.Context = DGis.initialize(
        context,
        ApiKeys(
            directory = creationParams?.get("directory") as String,
            map = creationParams["map"] as String
        ),
    )


    override fun getView(): MapView {
        return GisMapSession.getMapView() ?: gisView
    }

    override fun dispose() {}

    init {
        // Запрос разрешений, и регистрация сервисов локации
        registerServices(context)
        setupPermissions(context)

        // Инициализация камеры.
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

        // Создаем канал для общения..
        methodChannel = MethodChannel(messenger, "fgis")
        methodChannel.setMethodCallHandler(this)


        gisView = GisMapSession.getMapView() ?: MapView(context, mapOptions)
        GisMapSession.setMapView(gisView)
        gisView.getMapAsync { map ->
            mapObjectManager = MapObjectManager(map)
            controller = GisMapController(gisView, sdkContext, mapObjectManager)
            controller.createMarkers(creationParams)
            gisView.setTouchEventsObserver(object : TouchEventsObserver {
                override fun onTap(point: ScreenPoint) {
                    map.getRenderedObjects(point, ScreenDistance(1f))
                        .onResult { renderedObjectInfos ->
                            for (renderedObjectInfo in renderedObjectInfos) {
                                if (renderedObjectInfo.item.item.userData != null) {
                                    val args = mapOf(
                                        "id" to renderedObjectInfo.item.item.userData
                                    )
                                    Log.d(
                                        "DGIS",
                                        "Нажатие на маркер"
                                    )
                                    methodChannel.invokeMethod(
                                        "ontap_marker",
                                        args
                                    )
                                }
                            }
                        }
                    super.onTap(point)
                }
            })
            val source = MyLocationMapObjectSource(
                sdkContext,
                MyLocationDirectionBehaviour.FOLLOW_MAGNETIC_HEADING,
                createSmoothMyLocationController()
            )
            map.addSource(source)
        }
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getCameraPosition" -> {
                controller.getCameraPosition(result = result)
            }
            "setCameraPosition" -> {
                controller.setCameraPosition(call = call)
            }
            "createMarkers" -> {
                val args = call.arguments
                controller.createMarkers(args)
            }
        }
    }

    private fun registerServices(applicationContext : Context) {
        val compassSource = CustomCompassManager(applicationContext)
        registerPlatformMagneticSource(sdkContext, compassSource)

        val locationSource = CustomLocationManager(applicationContext)
        registerPlatformLocationSource(sdkContext, locationSource)
    }

    private fun setupPermissions(context: Context) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}
