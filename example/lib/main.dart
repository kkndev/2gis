import 'dart:convert';
import 'dart:developer';
import 'dart:ui';
import 'package:dgis_flutter/dgis_flutter.dart';
import 'package:dgis_flutter_example/assets_constant.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'help.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: GisScreen(),
    );
  }
}

class GisScreen extends StatefulWidget {
  const GisScreen({Key? key}) : super(key: key);

  @override
  State<GisScreen> createState() => _GisScreenState();
}

class _GisScreenState extends State<GisScreen> {
  final GisMapController controller = GisMapController();

  late final Future<List<GisMapMarker>> icons;
  List<GisMapMarker> list = [];
  int counter = 0;

  @override
  void initState() {
    icons = Future.wait([]);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SizedBox(
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height,
        child: ButtonMapWidget(
          list: list,
          controller: controller,
          child: FutureBuilder<List<GisMapMarker>>(
            future: icons,
            builder: (context, snapshot) {
              if (!snapshot.hasData) return const SizedBox();
              list = snapshot.data!;
              return GisMap(
                directoryKey: 'rubyqf9316',
                mapKey: 'b7272230-6bc3-47e9-b24b-0eba73b12fe1',
                useHybridComposition: true,
                controller: controller,
                onTapMarker: (marker) {
                  // ignore: avoid_print
                  print(marker.id);
                },
                startCameraPosition: const GisCameraPosition(
                  latitude: 52.29778,
                  longitude: 104.29639,
                  bearing: 85.0,
                  tilt: 25.0,
                  zoom: 14.0,
                ),
              );
            },
          ),
        ),
      ),
    );
  }
}

class ButtonMapWidget extends StatelessWidget {
  final Widget child;
  final GisMapController controller;
  final List<GisMapMarker> list;

  const ButtonMapWidget({
    Key? key,
    required this.child,
    required this.controller,
    required this.list,
  }) : super(key: key);

  void updateIcons(context) async {
    String data =
        await DefaultAssetBundle.of(context).loadString("assets/data.json");
    final jsonResult = jsonDecode(data); //latest Dart
    var icons = await Future.wait([getPngFromAsset()]);
    var items =
        jsonResult['action_result']['data']['parking_places'] as List<dynamic>;
    List<GisMapMarker> items2 = [];
    items.forEach((element) {
      items2.add(GisMapMarker(
          icon: icons[0],
          latitude: double.parse(element['longitude']),
          longitude: double.parse(element['latitude']),
          angle: double.parse(element['angle']),
          zIndex: 0,
          id: element['id'].toString()));
    });
    print(items);

    final status = await controller.updateMarkers(items2);
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        child,
        Align(
            alignment: Alignment.bottomCenter,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                FloatingActionButton(
                  child: const Icon(Icons.gps_fixed),
                  onPressed: () async {
                    final status = await controller.setCameraPosition(
                        latitude: 56.455114546767, longitude: 84.985119293168);
                    log(status);
                  },
                ),
                FloatingActionButton(
                    child: const Icon(Icons.add),
                    onPressed: () => updateIcons(context)),
              ],
            )),
      ],
    );
  }
}
