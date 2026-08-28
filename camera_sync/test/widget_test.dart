import 'package:camera/camera.dart';
import 'package:camera_sync/camera_preview_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Camera app smoke test', (WidgetTester tester) async {
    // 1. Mock a dummy CameraDescription for testing
    const mockCamera = CameraDescription(
      name: '0',
      lensDirection: CameraLensDirection.back,
      sensorOrientation: 0,
    );

    // 2. Pump the widget directly passing the mock camera list
    await tester.pumpWidget(
      MaterialApp(
        theme: ThemeData.dark(),
        home: CameraPreviewScreen(cameras: const [mockCamera]),
      ),
    );

    // 3. Verify initial UI state
    expect(find.text('VISUAL'), findsOneWidget);
  });
}
