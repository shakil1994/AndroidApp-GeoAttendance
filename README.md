# Task 1: Geo-Fenced Attendance System (Native Android)
A location-aware Android application built with **Jetpack Compose** and **Google Play Services Location API** that allows users to set office GPS coordinates and dynamically enables attendance recording when within a 50-meter geo-fenced radius.

# Deliverables & Links
-> GitHub Repository: https://github.com/shakil1994/AndroidApp-GeoAttendance
-> Release APK: https://drive.google.com/drive/folders/1zcZwe1G_xVvmtGPSj9-33vR1zZ4lT8No?usp=sharing

# Project Structure & Architecture
This application strictly follows modern Android development practices using **MVVM (Model-View-ViewModel)** and unidirectional data flow.

## How to Run

### Prerequisites
* Android Studio Ladybug (or newer)
* Android SDK 35 (Minimum SDK 26)
* A physical device or Android Emulator with active Location/GPS permissions enabled.

### Steps
1. **Clone the Repository:**
   git clone https://github.com/shakil1994/AndroidApp-GeoAttendance.git

# Screenshots
https://drive.google.com/file/d/1kUiavuXrQCnbEUxe1hFZM7tHY40_zCAJ/view?usp=sharing

# Task 2: Advanced Camera & Sync Engine (Flutter)
A Flutter application featuring a custom hardware camera implementation paired with an offline-first resilient sync engine. The app supports pinch-to-zoom, dynamic lens switching/clamping, tap-to-focus animations, batch image queuing, and persistent background uploads via Workmanager.

# Deliverables & Links
-> GitHub Repository: https://github.com/shakil1994/AndroidApp-GeoAttendance
-> Release APK: https://drive.google.com/drive/folders/1zcZwe1G_xVvmtGPSj9-33vR1zZ4lT8No?usp=sharing

 # Project Structure & Architecture
This project follows a Layered Clean Architecture combined with a Singleton State Provider Engine pattern for seamless background synchronization.

	-> UI Layer (lib/camera_preview_screen.dart, lib/upload_manager_screen.dart): Handles custom camera control interactions (zoom gestures, tap-to-focus indicators, shutter triggers) and batch queue visualizations.

	-> Sync Engine Layer (lib/sync_engine.dart): Manages local queue persistence, state updates, low-bandwidth failover logic, and background task dispatching using Workmanager.

# Generative AI Usage
Generative AI was utilized as a dynamic code architecture partner during development to accelerate production and debug device hardware constraints.

	Essential Prompts Used:

	1. "Implement a custom Flutter CameraPreview with dynamic zoom presets (0.5x, 1x, 2x), vertical zoom slider, pinch-to-zoom gestures, and tap-to-focus visual indicators."

	2. "Create an offline-first Workmanager sync engine in Flutter that queues image files, handles simulated network failure, and automatically retries background uploads when internet connectivity restores."

	3. "Fix 0.5x zoom hardware error in Flutter camera package on devices where min zoom is 1.0."

# How to Run
	Prerequisites
		-> Flutter SDK (>=3.0.0)
		-> Android Studio / Xcode configured for native hardware testing
		-> Physical Android/iOS test device (Camera hardware requires physical testing)
	Steps
		1. Clone the Repository:
			git clone https://github.com/shakil1994/AndroidApp-GeoAttendance.git
		2. Install Dependencies:
			flutter pub get
		3. Run the Application:
			flutter run
		4. Build Release APK:
			flutter build apk --release

# Screenshots
-> Custom Camera UI (Zoom & Tap-to-Focus)
	https://drive.google.com/file/d/1lK9442NsNfuJB-hmIm4a0wfyo7EVbF7C/view?usp=sharing
-> Upload Manager (Sync Engine Queue)
	https://drive.google.com/file/d/1TEcTuRhUM_0fkR1k-oHEfxziA4lEmCnm/view?usp=sharing
	https://drive.google.com/file/d/1Mc-1jAfOR96cBIgukizwDUouQ86Z7fd4/view?usp=drive_link
