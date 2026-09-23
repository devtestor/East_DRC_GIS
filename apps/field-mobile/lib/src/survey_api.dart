import 'dart:convert';

import 'package:http/http.dart' as http;

import 'models.dart';

class SurveyApi {
  SurveyApi({required this.baseUrl});

  final String baseUrl;

  Future<List<SurveyJob>> assignedJobs({required String authHeader}) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/v1/surveys'),
      headers: {'Authorization': authHeader, 'X-Correlation-Id': _correlationId()},
    );
    if (response.statusCode != 200) throw Exception('Survey jobs unavailable');
    return (jsonDecode(response.body) as List<dynamic>)
        .map((item) => SurveyJob.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<void> submitObservation({required String surveyId, required PendingObservation observation, required String authHeader}) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/v1/surveys/$surveyId/observations'),
      headers: {'Authorization': authHeader, 'Content-Type': 'application/json', 'X-Correlation-Id': _correlationId()},
      body: jsonEncode(observation.toJson()),
    );
    if (response.statusCode < 200 || response.statusCode >= 300) throw Exception('Observation sync failed');
  }

  String _correlationId() => DateTime.now().microsecondsSinceEpoch.toString();
}
