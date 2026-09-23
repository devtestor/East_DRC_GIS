import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:sqflite_sqlcipher/sqflite.dart';

import 'models.dart';

class OfflineStore {
  OfflineStore._(this._database);

  final Database _database;
  static const _secureStorage = FlutterSecureStorage();

  static Future<OfflineStore> open() async {
    final key = await _secureStorage.read(key: 'field_database_key') ??
        DateTime.now().microsecondsSinceEpoch.toString();
    await _secureStorage.write(key: 'field_database_key', value: key);
    final database = await openDatabase(
      'edrc_field.sqlite',
      password: key,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE survey_jobs (
            id TEXT PRIMARY KEY,
            parcel_id TEXT NOT NULL,
            status TEXT NOT NULL,
            purpose TEXT NOT NULL,
            observation_count INTEGER NOT NULL,
            cached_at TEXT NOT NULL
          )
        ''');
        await db.execute('''
          CREATE TABLE observation_queue (
            client_observation_id TEXT PRIMARY KEY,
            survey_id TEXT NOT NULL,
            payload TEXT NOT NULL,
            created_at TEXT NOT NULL,
            attempts INTEGER NOT NULL DEFAULT 0,
            last_error TEXT
          )
        ''');
      },
    );
    return OfflineStore._(database);
  }

  Future<void> cacheJobs(List<SurveyJob> jobs) async {
    final batch = _database.batch();
    for (final job in jobs) {
      batch.insert('survey_jobs', {
        'id': job.id,
        'parcel_id': job.parcelId,
        'status': job.status,
        'purpose': job.purpose,
        'observation_count': job.observationCount,
        'cached_at': DateTime.now().toUtc().toIso8601String(),
      }, conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await batch.commit(noResult: true);
  }

  Future<List<SurveyJob>> jobs() async {
    final rows = await _database.query('survey_jobs', orderBy: 'cached_at DESC');
    return rows.map((row) => SurveyJob(
      id: row['id']! as String,
      parcelId: row['parcel_id']! as String,
      status: row['status']! as String,
      purpose: row['purpose']! as String,
      observationCount: row['observation_count']! as int,
    )).toList();
  }

  Future<void> enqueue(PendingObservation observation) async {
    await _database.insert('observation_queue', {
      'client_observation_id': observation.clientObservationId,
      'survey_id': observation.surveyId,
      'payload': jsonEncode(observation.toJson()),
      'created_at': DateTime.now().toUtc().toIso8601String(),
    }, conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  Future<List<PendingObservation>> pendingObservations() async {
    final rows = await _database.query('observation_queue', orderBy: 'created_at ASC');
    return rows.map((row) {
      final payload = jsonDecode(row['payload']! as String) as Map<String, dynamic>;
      return PendingObservation(
        clientObservationId: row['client_observation_id']! as String,
        surveyId: row['survey_id']! as String,
        observationType: payload['observationType'] as String,
        latitude: (payload['latitude'] as num?)?.toDouble(),
        longitude: (payload['longitude'] as num?)?.toDouble(),
        accuracyMeters: (payload['accuracyMeters'] as num?)?.toDouble(),
        note: payload['note'] as String?,
      );
    }).toList();
  }

  Future<void> removePending(String clientObservationId) async {
    await _database.delete('observation_queue', where: 'client_observation_id = ?', whereArgs: [clientObservationId]);
  }

  Future<void> close() => _database.close();
}
