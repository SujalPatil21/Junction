import 'package:flutter_test/flutter_test.dart';
import 'package:attendee_flutter_app/main.dart';
import 'package:attendee_flutter_app/services/backend_scenario_sync_service.dart';

void main() {
  testWidgets('Attendee app launches and displays Junction header', (WidgetTester tester) async {
    await tester.pumpWidget(const AttendeeApp());
    expect(find.text('JUNCTION'), findsOneWidget);
    BackendScenarioSyncService().stopLiveSync();
    await tester.pump(const Duration(milliseconds: 1500));
    await tester.pump(const Duration(milliseconds: 500));
  });
}
