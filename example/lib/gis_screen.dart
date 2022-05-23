import 'package:flutter/material.dart';
import 'package:dgis_flutter/dgis_flutter.dart';

class GisScreen extends StatelessWidget {
  const GisScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
          child: GisWidget(
            directoryKey: 'rubyqf9316',
            mapKey: 'b7272230-6bc3-47e9-b24b-0eba73b12fe1',
          )),
    );
  }
}