import 'dart:async';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:workmanager/workmanager.dart';

const String syncTaskName = "com.app.uploadBatchTask";

@pragma('vm:entry-point')
void callbackDispatcher() {
  Workmanager().executeTask((task, inputData) async {
    switch (task) {
      case syncTaskName:
        return await SyncEngine.processQueueInBackground(inputData);
      default:
        return Future.value(true);
    }
  });
}

enum UploadStatus { pending, uploading, failed, synced }

class QueueItem {
  final String id;
  final String filePath;
  final String name;
  final double sizeMb;
  UploadStatus status;
  int retryCount;

  QueueItem({
    required this.id,
    required this.filePath,
    required this.name,
    required this.sizeMb,
    this.status = UploadStatus.pending,
    this.retryCount = 0,
  });
}

class SyncEngine extends ChangeNotifier {
  static final SyncEngine _instance = SyncEngine._internal();
  factory SyncEngine() => _instance;
  SyncEngine._internal();

  final List<QueueItem> pendingQueue = [];
  bool isSyncing = false;

  void initializeWorkmanager() {
    Workmanager().initialize(callbackDispatcher, isInDebugMode: false);
  }

  void addToQueue(List<File> images) {
    for (var file in images) {
      final size = (file.lengthSync() / (1024 * 1024));
      pendingQueue.add(
        QueueItem(
          id: DateTime.now().microsecondsSinceEpoch.toString(),
          filePath: file.path,
          name: file.path.split('/').last,
          sizeMb: size,
        ),
      );
    }
    notifyListeners();
    scheduleBackgroundSync();
  }

  void scheduleBackgroundSync() {
    Workmanager().registerOneOffTask(
      DateTime.now().millisecondsSinceEpoch.toString(),
      syncTaskName,
      constraints: Constraints(
        networkType: NetworkType.connected, // Automatic retry on network restoration
      ),
    );
  }

  // MOCK API CALL METHOD
  static Future<bool> mockApiUpload(String filePath) async {
    await Future.delayed(const Duration(seconds: 2)); // Simulate network latency

    // Simulating low-bandwidth / intermittent failure (70% success rate)
    final bool isSuccess = (DateTime.now().second % 10) < 7;

    /*
    // REAL IMPLEMENTATION LOOKS LIKE THIS:
    // var request = http.MultipartRequest('POST', Uri.parse('https://api.example.com/upload'));
    // request.files.add(await http.MultipartFile.fromPath('image', filePath));
    // var response = await request.send();
    // return response.statusCode == 200;
    */

    return isSuccess;
  }

  static Future<bool> processQueueInBackground(Map<String, dynamic>? data) async {
    // Background worker task loop
    try {
      // In a real app, load pending files from SQLite / SharedPrefs here
      bool success = await mockApiUpload("dummy_path");
      return success;
    } catch (e) {
      return false; // Tells WorkManager to retry based on policy
    }
  }

  Future<void> syncNow() async {
    if (isSyncing) return;
    isSyncing = true;
    notifyListeners();

    for (var item in pendingQueue) {
      if (item.status == UploadStatus.synced) continue;

      item.status = UploadStatus.uploading;
      notifyListeners();

      bool success = await mockApiUpload(item.filePath);

      if (success) {
        item.status = UploadStatus.synced;
      } else {
        item.status = UploadStatus.failed;
        item.retryCount++;
      }
      notifyListeners();
    }

    isSyncing = false;
    notifyListeners();
  }
}