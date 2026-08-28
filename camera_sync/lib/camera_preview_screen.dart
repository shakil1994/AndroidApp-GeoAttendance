import 'dart:io';
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'upload_manager_screen.dart';
import 'sync_engine.dart';

class CameraPreviewScreen extends StatefulWidget {
  final List<CameraDescription> cameras;

  const CameraPreviewScreen({Key? key, required this.cameras})
      : super(key: key);

  @override
  State<CameraPreviewScreen> createState() => _CameraPreviewScreenState();
}

class _CameraPreviewScreenState extends State<CameraPreviewScreen> {
  late CameraController _controller;
  double _currentZoom = 1.0;
  double _minZoom = 1.0;
  double _maxZoom = 1.0;
  double _baseScale = 1.0;

  // Tap-to-focus indicator positioning
  Offset? _tapPosition;
  bool _showFocusCircle = false;

  final List<File> _capturedBatch = [];

  @override
  void initState() {
    super.initState();
    _initCamera(widget.cameras.first);
  }

  Future<void> _initCamera(CameraDescription camera) async {
    _controller = CameraController(
      camera,
      ResolutionPreset.high,
      enableAudio: false,
    );

    await _controller.initialize();
    _minZoom = await _controller.getMinZoomLevel();
    _maxZoom = await _controller.getMaxZoomLevel();

    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _setZoom(double zoom) async {
    double clampedZoom = zoom.clamp(_minZoom, _maxZoom);
    await _controller.setZoomLevel(clampedZoom);
    setState(() => _currentZoom = clampedZoom);
  }

  Future<void> _onTapToFocus(
      TapDownDetails details, BoxConstraints constraints) async {
    if (!_controller.value.isInitialized) return;

    final Offset offset = Offset(
      details.localPosition.dx / constraints.maxWidth,
      details.localPosition.dy / constraints.maxHeight,
    );

    setState(() {
      _tapPosition = details.localPosition;
      _showFocusCircle = true;
    });

    await _controller.setFocusPoint(offset);
    await _controller.setExposurePoint(offset);

    Future.delayed(const Duration(milliseconds: 800), () {
      if (mounted) setState(() => _showFocusCircle = false);
    });
  }

  Future<void> _captureImage() async {
    if (!_controller.value.isInitialized || _controller.value.isTakingPicture)
      return;

    final XFile file = await _controller.takePicture();
    setState(() {
      _capturedBatch.add(File(file.path));
    });
  }

  @override
  Widget build(BuildContext me) {
    if (!_controller.value.isInitialized) {
      return const Scaffold(
        backgroundColor: Colors.black,
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Camera Preview with Pinch-to-Zoom & Tap-To-Focus
          LayoutBuilder(
            builder: (context, constraints) {
              return GestureDetector(
                onScaleStart: (details) => _baseScale = _currentZoom,
                onScaleUpdate: (details) {
                  _setZoom(_baseScale * details.scale);
                },
                onTapDown: (details) => _onTapToFocus(details, constraints),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    CameraPreview(_controller),
                    if (_showFocusCircle && _tapPosition != null)
                      Positioned(
                        left: _tapPosition!.dx - 25,
                        top: _tapPosition!.dy - 25,
                        child: Container(
                          width: 50,
                          height: 50,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            border: Border.all(color: Colors.amber, width: 2),
                          ),
                        ),
                      ),
                  ],
                ),
              );
            },
          ),

          // Header Actions
          Positioned(
            top: 50,
            left: 20,
            right: 20,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white),
                  onPressed: () {},
                ),
                const Text(
                  "VISUAL",
                  style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      letterSpacing: 2),
                ),
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.flash_on, color: Colors.white),
                      onPressed: () {},
                    ),
                    IconButton(
                      icon: const Icon(Icons.settings, color: Colors.white),
                      onPressed: () {},
                    ),
                  ],
                )
              ],
            ),
          ),

          // Vertical Zoom Slider (Right Side)
          Positioned(
            right: 15,
            top: 200,
            bottom: 250,
            child: RotatedBox(
              quarterTurns: 3,
              child: SliderTheme(
                data: SliderTheme.of(context).copyWith(
                  thumbColor: Colors.white,
                  activeTrackColor: Colors.white,
                  inactiveTrackColor: Colors.white30,
                  trackHeight: 2,
                ),
                child: Slider(
                  value: _currentZoom,
                  min: _minZoom,
                  max: _maxZoom,
                  onChanged: (val) => _setZoom(val),
                ),
              ),
            ),
          ),

          // Bottom Controls & Zoom Buttons
          Positioned(
            bottom: 40,
            left: 20,
            right: 20,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Preset Zoom Quick Buttons
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [0.5, 1.0, 2.0].map((zoom) {
                    bool isSelected = (_currentZoom - zoom).abs() < 0.2;
                    return GestureDetector(
                      onTap: () => _setZoom(zoom),
                      child: Container(
                        margin: const EdgeInsets.symmetric(horizontal: 6),
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color:
                              isSelected ? Colors.white24 : Colors.transparent,
                        ),
                        child: Text(
                          "${zoom}x",
                          style: TextStyle(
                            color: isSelected ? Colors.amber : Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 20),

                // Capture Controls Row
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    // Pending Batch Thumbnail Counter
                    GestureDetector(
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) =>
                                  const UploadManagerScreen()),
                        );
                      },
                      child: Stack(
                        clipBehavior: Clip.none,
                        children: [
                          Container(
                            width: 48,
                            height: 48,
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(10),
                              border: Border.all(color: Colors.white38),
                              image: _capturedBatch.isNotEmpty
                                  ? DecorationImage(
                                      image: FileImage(_capturedBatch.last),
                                      fit: BoxFit.cover,
                                    )
                                  : null,
                            ),
                          ),
                          if (_capturedBatch.isNotEmpty)
                            Positioned(
                              top: -5,
                              right: -5,
                              child: CircleAvatar(
                                radius: 10,
                                backgroundColor: Colors.blue,
                                child: Text(
                                  "${_capturedBatch.length}",
                                  style: const TextStyle(
                                      fontSize: 10, color: Colors.white),
                                ),
                              ),
                            )
                        ],
                      ),
                    ),

                    // Shutter Button
                    GestureDetector(
                      onTap: _captureImage,
                      child: Container(
                        width: 75,
                        height: 75,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 4),
                        ),
                        child: Center(
                          child: Container(
                            width: 60,
                            height: 60,
                            decoration: const BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                    ),

                    // Switch Camera Button
                    IconButton(
                      icon: const Icon(Icons.cameraswitch,
                          color: Colors.white, size: 28),
                      onPressed: () {
                        final nextCam = widget.cameras.firstWhere(
                          (c) => c != _controller.description,
                          orElse: () => widget.cameras.first,
                        );
                        _initCamera(nextCam);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 15),

                // Commit Batch to Upload Sync Queue
                if (_capturedBatch.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 10.0),
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blueAccent,
                        minimumSize: const Size(double.infinity, 48),
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(8)),
                      ),
                      onPressed: () {
                        SyncEngine().addToQueue(_capturedBatch);
                        setState(() => _capturedBatch.clear());
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) =>
                                  const UploadManagerScreen()),
                        );
                      },
                      child: Text("UPLOAD BATCH (${_capturedBatch.length})"),
                    ),
                  )
              ],
            ),
          )
        ],
      ),
    );
  }
}
