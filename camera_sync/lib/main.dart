import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'camera_preview_screen.dart';
import 'sync_engine.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final cameras = await availableCameras();

  // Initialize Workmanager for persistent network monitoring
  SyncEngine().initializeWorkmanager();

  runApp(MaterialApp(
    debugShowCheckedModeBanner: false,
    theme: ThemeData.dark(),
    home: CameraPreviewScreen(cameras: cameras),
  ));
}
