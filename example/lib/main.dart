import 'package:dgis_flutter/dgis_flutter.dart';
import 'package:dgis_flutter/gis_map_controller.dart';
import 'package:dgis_flutter/model/gis_camera_position.dart';
import 'package:flutter/material.dart';

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
    return MaterialApp(
      home: GisScreen(),
    );
  }
}

class GisScreen extends StatelessWidget {
  GisScreen({Key? key}) : super(key: key);
  final GisMapController controller = GisMapController();

  @override
  Widget build(BuildContext context) {
    return  Scaffold(
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: ()async{
          await controller.increaseZoom(duration: 0);
        },
      ),
      body: Center(
          child: GisMap(
            directoryKey: 'you api directory key',
            mapKey: 'you api map key',
            controller: controller,
            startCameraPosition: const GisCameraPosition(
              latitude: 52.29778,
              longitude: 104.29639,
              bearing: 85.0,
              tilt: 25.0,
              zoom: 14.0,
            ),
          )),
    );
  }
}
