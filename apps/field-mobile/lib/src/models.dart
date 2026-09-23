class SurveyJob {
  const SurveyJob({
    required this.id,
    required this.parcelId,
    required this.status,
    required this.purpose,
    required this.observationCount,
  });

  final String id;
  final String parcelId;
  final String status;
  final String purpose;
  final int observationCount;

  factory SurveyJob.fromJson(Map<String, dynamic> json) => SurveyJob(
        id: json['id'] as String,
        parcelId: json['parcelId'] as String,
        status: json['status'] as String,
        purpose: json['purpose'] as String,
        observationCount: (json['observationCount'] as num?)?.toInt() ?? 0,
      );
}

class PendingObservation {
  const PendingObservation({
    required this.clientObservationId,
    required this.surveyId,
    required this.observationType,
    required this.deviceId,
    this.latitude,
    this.longitude,
    this.accuracyMeters,
    this.note,
  });

  final String clientObservationId;
  final String surveyId;
  final String observationType;
  final String deviceId;
  final double? latitude;
  final double? longitude;
  final double? accuracyMeters;
  final String? note;

  Map<String, dynamic> toJson() => {
        'observationType': observationType,
        'latitude': latitude,
        'longitude': longitude,
        'accuracyMeters': accuracyMeters,
        'note': note,
        'clientObservationId': clientObservationId,
        'deviceId': deviceId,
        'captureSource': latitude == null || longitude == null ? 'MANUAL' : 'GNSS',
        'gnssFixQuality': accuracyMeters == null ? null : 'ACCURATE',
        'signatureStatus': 'NOT_CAPTURED',
  };
}
