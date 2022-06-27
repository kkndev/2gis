import 'dart:async';
import 'dart:developer';

import 'package:dgis_flutter/model/gis_camera_position.dart';
import 'package:dgis_flutter/model/gis_map_object.dart';
import 'package:flutter/services.dart';
import 'model/gis_point.dart';

class GisMapController {
  GisMapController();

  List<GisMapMarker> listMarker = [];
  final _platform = const MethodChannel('fgis');

  GisCameraPosition? position;

  Future<void> init() async {
    position = await getCameraPosition();
  }

  Future<GisCameraPosition> getCameraPosition() async {
    try {
      final result = await _platform.invokeMethod('getCameraPosition');
      return GisCameraPosition.fromJson(result);
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption getCameraPosition() message: $e');
    }

    return const GisCameraPosition(latitude: 0.0, longitude: 0.0);
  }

  Future<void> setCameraPosition(
      {required GisCameraPosition position, double? duration}) async {
    try {
      await _platform.invokeMethod('setCameraPosition',
          position.toNativeMap()..addAll({'duration': duration ?? 2}));
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
    }
  }

  Future<void> increaseZoom({double? duration, int? size}) async {
    try {
      final position = await getCameraPosition();
      await _platform.invokeMethod(
          'setCameraPosition',
          position.copyWith(zoom: position.zoom + (size ?? 1)).toNativeMap()
            ..addAll({'duration': duration ?? 2}));
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
    }
  }

  Future<void> reduceZoom({double? duration, int? size}) async {
    try {
      final position = await getCameraPosition();
      await _platform.invokeMethod(
          'setCameraPosition',
          position
              .copyWith(
                  zoom: position.zoom - (size ?? 1) < 0 ? 3.0 : position.zoom - (size ?? 1))
              .toNativeMap()
            ..addAll({'duration': duration ?? 2}));
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
    }
  }

  Future<void> updateMarkers(List<GisMapMarker> markers) async {
    try {
      listMarker = markers;
      await _platform.invokeMethod('updateMarkers',
          {"markers": markers.map((e) => e.toJson()).toList()});
    } on PlatformException catch (e) {
      log('Platform exeption updateMarkers() message: $e');
    }
  }

  Future<void> setRoute(RoutePosition position) async {
    try {
      await _platform.invokeMethod('setRoute', position.toJson());
    } on PlatformException catch (e) {
      log('Platform exeption setRoute() message: $e');
    }
  }

  Future<void> removeRoute() async {
    try {
      await _platform.invokeMethod('removeRoute');
    } on PlatformException catch (e) {
      log('Platform exeption removeRoute() message: $e');
    }
  }

  Future<String> setPolyline(List<GisPoint> points) async {
    try{
      String status = await _platform.invokeMethod('setPolyline', {
        'points' : points.map((e) => e.toNativeMap()).toList()
      });
      return status;
    }on PlatformException catch(e){
      log('Platform exeption setPolyline() message: $e');
      return 'ERROR';
    }
  }

  Future<String> removePolyline() async {
    try{
      String status = await _platform.invokeMethod('removePolyline');
      return status;
    }on PlatformException catch(e){
      log('Platform exeption removePolyline() message: $e');
      return 'ERROR';
    }
  }
}
