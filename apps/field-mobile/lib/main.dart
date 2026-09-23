import 'package:flutter/material.dart';

import 'src/field_app.dart';
import 'src/offline_store.dart';
import 'src/survey_api.dart';
import 'src/sync_queue.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final store = await OfflineStore.open();
  final api = SurveyApi(baseUrl: 'http://10.0.2.2:8080');
  final queue = SyncQueue(store: store, api: api);
  runApp(FieldApp(store: store, queue: queue));
}
