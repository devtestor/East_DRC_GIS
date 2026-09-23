import 'package:flutter/material.dart';

import 'field_capture.dart';
import 'models.dart';
import 'offline_store.dart';
import 'sync_queue.dart';

class FieldApp extends StatelessWidget {
  const FieldApp({super.key, required this.store, required this.queue});

  final OfflineStore store;
  final SyncQueue queue;

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'EDRC Field Survey',
        theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.green), useMaterial3: true),
        home: FieldHomePage(store: store, queue: queue),
      );
}

class FieldHomePage extends StatelessWidget {
  const FieldHomePage({super.key, required this.store, required this.queue});

  final OfflineStore store;
  final SyncQueue queue;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Field surveys')),
        body: ListView(
          padding: const EdgeInsets.all(20),
          children: const [
            Text('Offline survey workspace', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            SizedBox(height: 12),
            Text('Download assigned jobs, capture observations, and synchronize when connectivity returns.'),
            SizedBox(height: 20),
            Card(child: ListTile(leading: Icon(Icons.sync), title: Text('Synchronization queue'), subtitle: Text('No synchronization run yet'))),
            Card(child: ListTile(leading: Icon(Icons.gps_fixed), title: Text('GNSS capture'), subtitle: Text('High-accuracy coordinates are stored as field evidence'))),
            Card(child: ListTile(leading: Icon(Icons.camera_alt), title: Text('Field photos'), subtitle: Text('Photos remain queued until secure upload is available'))),
            Card(child: ListTile(leading: Icon(Icons.draw), title: Text('Witness signature'), subtitle: Text('Signature capture requires explicit witness action'))),
            SizedBox(height: 20),
            Text('Field drafts do not publish cadastral geometry or determine legal rights.', style: TextStyle(fontWeight: FontWeight.w600)),
          ],
        ),
      );
}
