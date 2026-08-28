# Flutter Base Rules
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.embedding.** { *; }

# WorkManager Rules
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-dontwarn androidx.work.impl.**

# AndroidX Keep Rules
-keep class androidx.** { *; }
-dontwarn androidx.**

# Play Core / Deferred Components (Fixes R8 missing class errors)
-dontwarn com.google.android.play.core.splitcompat.**
-dontwarn com.google.android.play.core.splitinstall.**
-dontwarn com.google.android.play.core.tasks.**
-keep class com.google.android.play.core.** { *; }