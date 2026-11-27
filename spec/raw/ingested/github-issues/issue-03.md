# #3 Flutter projects: native platform code shows up as an unhelpful 'app' module

https://github.com/flbaue/depviewer/issues/3
State on export (2026-11-27): open, opened 2026-11-25

In a Flutter app, the native platform code under `android/` (Kotlin, `GeneratedPluginRegistrant.java`) is picked up as a separate JVM module named after its Gradle folder - usually just `app`. That name says little next to the Dart packages, and the generated Java file adds dozens of unresolved-reference errors because the Android/Flutter jars are not on the classpath.

**Options to spec:**
- name native modules after their platform and parent project (e.g. `android (my_app)`),
- skip known generated files (`GeneratedPluginRegistrant.java`),
- an option to leave out native platform code entirely (`--no-native`?).

Also check `ios/` (Swift/Objective-C are not scanned today) so the behaviour is documented.
