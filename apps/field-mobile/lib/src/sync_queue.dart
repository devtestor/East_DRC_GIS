import 'models.dart';
import 'offline_store.dart';
import 'survey_api.dart';

class SyncQueue {
  SyncQueue({required this.store, required this.api});

  final OfflineStore store;
  final SurveyApi api;

  Future<void> syncPending({required String authHeader}) async {
    // Queue replay is intentionally serialized. The server deduplicates by clientObservationId.
    for (final observation in await store.pendingObservations()) {
      await api.submitObservation(
        surveyId: observation.surveyId,
        observation: observation,
        authHeader: authHeader,
      );
      await store.removePending(observation.clientObservationId);
    }
  }
}
