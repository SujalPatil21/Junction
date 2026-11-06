# JUNCTION Attendee — Mobile Application

The official cross-platform mobile client for attendees of mega-events orchestrated by **JUNCTION**. Built with **Flutter**, supporting Android, iOS, Web, and Desktop.

## Capabilities
- **Real-Time Live Crowd Density & Pressure**: Live vector map rendering with real-time zone pressure overlays.
- **Dynamic Diversion Vouchers**: Partner restaurant coupons and vouchers dispatched during egress surges.
- **Integrated Routing**: Road-constrained evacuation and egress routing with step-by-step navigation.
- **Backend Scenario Synchronization**: Automatically polls and synchronizes with the Spring Boot backend (`http://localhost:8080/api/scenarios`) across LAN, Android Emulator (`10.0.2.2`), and desktop environments.

## Getting Started

### Prerequisites
- Flutter SDK (3.22+)
- Android Studio / Android SDK (for Android builds)

### Run Locally
```bash
cd mobile/attendee_flutter_app
flutter pub get
flutter run
```

### Run Tests
```bash
flutter test
```

### Build Android Standalone APK
```bash
flutter build apk --release
# Output: build/app/outputs/flutter-apk/app-release.apk
```
The pre-built release APK is distributed through the web platform at `/junction-attendee.apk`.
