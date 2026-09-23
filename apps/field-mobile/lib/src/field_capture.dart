import 'dart:typed_data';

import 'package:geolocator/geolocator.dart';
import 'package:image_picker/image_picker.dart';
import 'package:signature/signature.dart';

class GnssCapture {
  const GnssCapture({required this.latitude, required this.longitude, required this.accuracyMeters});

  final double latitude;
  final double longitude;
  final double accuracyMeters;
}

class FieldCapture {
  FieldCapture({ImagePicker? imagePicker}) : _imagePicker = imagePicker ?? ImagePicker();

  final ImagePicker _imagePicker;

  Future<GnssCapture> captureGnss() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      throw StateError('Location service is disabled');
    }
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied || permission == LocationPermission.deniedForever) {
      throw StateError('Location permission is required for field capture');
    }
    final position = await Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(accuracy: LocationAccuracy.high),
    );
    return GnssCapture(
      latitude: position.latitude,
      longitude: position.longitude,
      accuracyMeters: position.accuracy,
    );
  }

  Future<XFile?> capturePhoto() => _imagePicker.pickImage(
        source: ImageSource.camera,
        imageQuality: 75,
        maxWidth: 2048,
      );

  Future<Uint8List?> captureSignature(SignatureController controller) => controller.toPngBytes();
}
