package com.example.dgis_flutter

import android.Manifest
import android.content.Context
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.ApiKeys
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.map.*
import ru.dgis.sdk.positioning.registerPlatformMagneticSource
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.positioning.LocationChangeListener
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import ru.dgis.sdk.routing.RouteEditor

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
    private var controller : GisMapController


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
        val routeEditor = RouteEditor(sdkContext)
        controller = GisMapController(gisView, sdkContext, mapObjectManager, routeEditor)
        gisView.getMapAsync { map ->
            mapObjectManager = MapObjectManager(map)
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
            val navigationManager = NavigationManager(sdkContext)
            navigationManager.start()
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
            "updateMarkers" -> {
                val args = call.arguments
                controller.updateMarkers(arguments = args)
            }
            "setRoute" -> {
                controller.setRoute(arguments = call.arguments)
            }
            "removeRoute" -> {
                controller.removeRoute()
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
