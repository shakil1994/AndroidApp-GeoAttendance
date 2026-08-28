import 'package:flutter/material.dart';
import 'sync_engine.dart';

class UploadManagerScreen extends StatefulWidget {
  const UploadManagerScreen({Key? key}) : super(key: key);

  @override
  State<UploadManagerScreen> createState() => _UploadManagerScreenState();
}

class _UploadManagerScreenState extends State<UploadManagerScreen> {
  final SyncEngine _engine = SyncEngine();

  @override
  void initState() {
    super.initState();
    _engine.addListener(_onEngineChange);
  }

  @override
  void dispose() {
    _engine.removeListener(_onEngineChange);
    super.dispose();
  }

  void _onEngineChange() => setState(() {});

  Widget _buildStatusChip(QueueItem item) {
    switch (item.status) {
      case UploadStatus.pending:
        return const Text("WAITING FOR CONNECTION", style: TextStyle(color: Colors.amber, fontSize: 10));
      case UploadStatus.uploading:
        return const Text("UPLOADING...", style: TextStyle(color: Colors.lightBlue, fontSize: 10));
      case UploadStatus.failed:
        return Text("RETRYING... (ATTEMPT ${item.retryCount})", style: const TextStyle(color: Colors.redAccent, fontSize: 10));
      case UploadStatus.synced:
        return const Text("SYNCED", style: TextStyle(color: Colors.tealAccent, fontSize: 10));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text("Upload Manager"),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 16),
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.teal.withOpacity(0.2),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Row(
              children: [
                Icon(Icons.wifi, color: Colors.tealAccent, size: 14),
                SizedBox(width: 4),
                Text("STABLE LINK", style: TextStyle(color: Colors.tealAccent, fontSize: 10)),
              ],
            ),
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text("BATCH SYNC PROGRESS", style: TextStyle(color: Colors.grey, fontSize: 11)),
            const SizedBox(height: 8),
            LinearProgressIndicator(
              value: _engine.pendingQueue.isEmpty
                  ? 0
                  : _engine.pendingQueue.where((e) => e.status == UploadStatus.synced).length /
                  _engine.pendingQueue.length,
              backgroundColor: Colors.white10,
              color: Colors.blueAccent,
            ),
            const SizedBox(height: 20),
            Text("PENDING UPLOADS (${_engine.pendingQueue.length})",
                style: const TextStyle(color: Colors.grey, fontSize: 11)),
            const SizedBox(height: 10),
            Expanded(
              child: ListView.builder(
                itemCount: _engine.pendingQueue.length,
                itemBuilder: (context, index) {
                  final item = _engine.pendingQueue[index];
                  return Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E293B),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      children: [
                        Container(
                          width: 40,
                          height: 40,
                          decoration: BoxDecoration(
                            color: Colors.white10,
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: const Icon(Icons.insert_drive_file, color: Colors.white54),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(item.name,
                                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                              Text("${item.sizeMb.toStringAsFixed(1)} MB",
                                  style: const TextStyle(color: Colors.grey, fontSize: 12)),
                              const SizedBox(height: 4),
                              _buildStatusChip(item),
                            ],
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(bottom: 30.0), // Adjust margin height as needed
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blueAccent,
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                onPressed: _engine.isSyncing ? null : () => _engine.syncNow(),
                child: Text(_engine.isSyncing ? "SYNCING..." : "START NEW UPLOAD BATCH"),
              ),
            )
          ],
        ),
      ),
    );
  }
}