import 'dart:typed_data';


class GisMapMarker {
  final double latitude;
  final double longitude;
  final Uint8List icon;
  final String id;
  GisMapMarker({
    required this.latitude, required this.longitude, required this.icon, required this.id
});

  Map<String, dynamic> toJson() => {
    'latitude' : latitude,
    'longitude' : longitude,
    'icon' : icon,
    'id' : id
  };
}




