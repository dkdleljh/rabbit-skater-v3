# Google Play Console readiness – 2026-10-26

## Scope
- App ID: `com.devbyzenith.rabbitskater`
- This branch upgrades the **current Kotlin game module**. The legacy `project/` Cocos2d-x build configuration is not changed.
- These source changes are **not** a Play Store submission or a verified production release.

## Source changes
- `compileSdk` and `targetSdk` 34 → 36 (Android 16).
- Android Gradle Plugin 8.2.0 → 8.9.2; Gradle Wrapper 8.5 → 8.11.1.
- `versionCode` 4 → 5, `versionName` 3.0.1 → 3.0.2.
- Manifest tooling target → API 36.
- GitHub Actions CI executes a debug build, unit tests and lint checks.

## Required verification before store submission
- [ ] GitHub Actions CI passes; debug APK installs and gameplay functions on an Android 16 device.
- [ ] Verify landscape gameplay, full-screen rendering, system bars, and large-screen (600dp+) behavior; API 36 changes orientation and aspect-ratio handling on larger devices.
- [ ] Generate a signed **release AAB** with a valid Play upload key, not the debug certificate. If the upload key was lost, request a reset in Play Console and confirm the new key before uploading.
- [ ] Check produced AAB with `bundletool dump config --bundle=<app.aab>` for 16 KB zip alignment if it contains native libraries.
- [ ] Analyze the final artifact for `.so` native libraries; the current Kotlin module does not declare an NDK dependency, but merged dependencies must still be verified.
- [ ] Run on a 16 KB memory-page test environment, especially if adding third-party native SDKs.
- [ ] Confirm the game is functionally equivalent to the version currently on Google Play and all store content remains accurate.

## Data safety: blocked pending shipped-bundle evidence
Play Console identifies an undeclared **Device or other IDs** data type in version code 4. The currently published release (last publication 2023-02-02) is distinct from this 2026 Kotlin source branch. The `project/` legacy Gradle draft includes AdMob, AppLovin and Firebase dependencies, while the live Kotlin module's `AdManager` is a stub. Do not use the stub implementation to make claims about the published bundle.

**Do not automatically answer “no user data collected,” invent SDK disclosures, or bypass Play's policy warning.** Inspect the actual published/release AAB, all included SDKs and privacy policy. If retaining data collection, accurately disclose all affected categories, purpose, sharing, encryption and user choices in Data safety. Alternatively publish a verified new build that genuinely removes the collection and complete the declaration accordingly.

## References
- Google Play target API policy: https://support.google.com/googleplay/android-developer/answer/11926878
- Android Gradle Plugin/API compatibility: https://developer.android.com/build/releases/about-agp
- 16 KB page compatibility: https://developer.android.com/guide/practices/page-sizes
- Android 16 behavior changes: https://developer.android.com/about/versions/16/behavior-changes-16
