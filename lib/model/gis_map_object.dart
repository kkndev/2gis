import 'dart:typed_data';


class GisMapMarker {
  final double latitude;
  final double longitude;
  final Uint8List icon;
  GisMapMarker({
    required this.latitude, required this.longitude, required this.icon,
});

  Map<String, dynamic> toJson() => {
    'latitude' : latitude,
    'longitude' : longitude,
    'icon' : icon,
  };
}



