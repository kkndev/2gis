import 'package:flutter/services.dart';
import 'package:image/image.dart' show Image, copyRotate;

Future<Uint8List> getPngFromAsset() async {
  return await rootBundle
      .load('assets/car.png')
      .then((data) => data.buffer.asUint8List());
}
