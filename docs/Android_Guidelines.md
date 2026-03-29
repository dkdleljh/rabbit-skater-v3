# Android 개발 가이드

이 문서는 `Rabbit Skater v3`를 안드로이드 프로젝트로 유지하고 배포할 때 필요한 실무 기준을 정리한 문서다.

## 현재 빌드 기준

- `compileSdk = 34`
- `minSdk = 24`
- `targetSdk = 34`
- Java/Kotlin 타깃: 17
- 기본 빌드 명령:

```bash
./gradlew assembleDebug
```

## 개발 환경 권장사항

- Android Studio 최신 안정 버전
- Android SDK 34 설치
- JDK 17
- `adb` 사용 가능 환경

## 필수 점검 항목

- 앱이 가로 화면 고정으로 동작하는지 확인
- 실제 기기에서 60FPS에 가까운 렌더링이 유지되는지 확인
- 에셋 로딩 시 메모리 사용량이 급증하지 않는지 확인
- Android 14 기기와 Android 7 이상 저사양 기기에서 기본 실행이 되는지 확인

## 공개 저장소 운영 기준

공개 저장소에는 빌드 캐시와 로컬 SDK 경로를 포함하지 않는다.

- 제외 대상:
  - `.gradle/`
  - `build/`
  - `app/build/`
  - `local.properties`

## 배포 전 체크리스트

- [ ] 버전 코드와 버전 이름을 갱신했는가
- [ ] 아이콘과 앱 이름이 최종 상태인가
- [ ] 크래시 없이 메인 화면이 열리는가
- [ ] 광고를 붙였다면 동의/정책 문서가 맞는가
- [ ] 디버그 설정이 릴리즈에 섞이지 않았는가

## 추천 파일 확인 순서

1. [app/build.gradle.kts](/home/zenith/Desktop/Rabbit_Skater_v3/app/build.gradle.kts)
2. [app/src/main/AndroidManifest.xml](/home/zenith/Desktop/Rabbit_Skater_v3/app/src/main/AndroidManifest.xml)
3. [gradle.properties](/home/zenith/Desktop/Rabbit_Skater_v3/gradle.properties)
4. [settings.gradle.kts](/home/zenith/Desktop/Rabbit_Skater_v3/settings.gradle.kts)
