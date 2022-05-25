import 'package:dgis_flutter/gis_map_controller.dart';
import 'package:dgis_flutter/model/gis_camera_position.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class GisMap extends StatelessWidget {
  final String directoryKey;
  final String mapKey;
  final GisCameraPosition startCameraPosition;
  final GisMapController controller;
  const GisMap(
      {Key? key,
      required this.mapKey,
      required this.directoryKey,
      required this.startCameraPosition,
      required this.controller})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = '<gis-view>';
    // Pass parameters to the platform side.
    Map<String, dynamic> creationParams = {
      'directory': directoryKey,
      'map': mapKey,
      'latitude': startCameraPosition.latitude,
      'longitude': startCameraPosition.longitude,
      'zoom': startCameraPosition.zoom,
      'tilt': startCameraPosition.tilt,
      'bearing': startCameraPosition.bearing
    };

    return PlatformViewLink(
      viewType: viewType,
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}
