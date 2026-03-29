# Rabbit Skater v3

`Rabbit Skater v3`는 토끼 캐릭터가 스케이트보드를 타고 장애물을 피하는 안드로이드 러닝 게임 프로젝트다. 현재 저장소에는 게임 소스, 에셋, Android Gradle 빌드 설정, 정책 검토용 문서가 함께 들어 있다.

## 프로젝트 구성

- 앱 성격: 가로 화면 기반 캐주얼 러닝 게임
- 빌드 도구: Gradle 8 계열, Kotlin, Android SDK 34
- 최소 SDK: Android 7.0 이상 수준인 `minSdk 24`
- 대상 SDK: `targetSdk 34`
- 앱 이름: `Rabbit Skater v3`

## 저장소에서 바로 확인할 수 있는 것

- `app/`: 현재 Android 앱 모듈
- `assets/`: 게임에 사용되는 스프라이트, 사운드, 레벨 데이터
- `docs/`: 광고 연동, Android 정책, 디자인 시스템, UI/UX 개선, Google Play 체크리스트
- `project/`: 이전 빌드 산출물 또는 레거시 프로젝트 설정 흔적

## 빌드 방법

```bash
cd ~/Desktop/Rabbit_Skater_v3
./gradlew assembleDebug
```

성공하면 기본 디버그 APK는 아래 경로에 생성된다.

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 설치 방법

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

기기에 기존 버전이 깔려 있다면 삭제 후 다시 설치하거나 `adb install -r`로 덮어쓸 수 있다.

## 개발 시 먼저 볼 파일

- [app/build.gradle.kts](/home/zenith/Desktop/Rabbit_Skater_v3/app/build.gradle.kts): 앱 모듈 빌드 설정
- [app/src/main/AndroidManifest.xml](/home/zenith/Desktop/Rabbit_Skater_v3/app/src/main/AndroidManifest.xml): 현재 앱 매니페스트
- [docs/Android_Guidelines.md](/home/zenith/Desktop/Rabbit_Skater_v3/docs/Android_Guidelines.md): 안드로이드 빌드와 배포 기준
- [docs/Design_System.md](/home/zenith/Desktop/Rabbit_Skater_v3/docs/Design_System.md): UI 스타일 기준

## 광고와 정책 상태

현재 프로젝트에는 광고 관련 메타데이터와 문서가 들어 있지만, 실제 앱 모듈 의존성에는 광고 SDK가 완전히 연결된 상태는 아니다. 따라서 문서에서는 "즉시 배포 가능한 상태"보다 "공개 배포 전에 확인해야 할 기준"을 중심으로 설명한다.

## 공개 저장소 주의사항

이 저장소는 공개용으로 정리되며 아래 항목은 Git에서 제외한다.

- `.gradle/`, `build/`, `app/build/`
- `local.properties`
- Eclipse/IDE 메타 파일

즉, 빌드 환경과 로컬 경로는 제외하고, 소스와 에셋, 정책 문서만 공유하는 구성을 기본으로 한다.
