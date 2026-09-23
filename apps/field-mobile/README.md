# EDRC field mobile

Android-first offline survey client foundation.

Security boundaries:

- Survey jobs and observations are stored in an encrypted SQLCipher database.
- The sync queue uses client-generated observation UUIDs and server-side idempotency.
- GNSS, camera, and witness-signature capture are isolated in `FieldCapture`; captured evidence must be uploaded through an authenticated, enrolled device.
- Local records are operational drafts; they do not publish cadastral geometry or determine legal rights.
- Device enrollment, remote revocation, key rotation, and minimum-retention cleanup are required before pilot use.

Run locally after installing Flutter:

```bash
flutter pub get
flutter analyze
flutter test
flutter run
```

The default API is `http://10.0.2.2:8080` for the Android emulator. Configure a device-specific URL for physical devices.

Android release preparation must declare location and camera permissions, enforce secure-screen policy where required, and test permission denial and revocation paths.
