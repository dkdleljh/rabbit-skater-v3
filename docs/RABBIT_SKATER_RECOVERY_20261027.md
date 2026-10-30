# Rabbit Skater 복구·업데이트 조사

기준일 및 공식 문서 열람일: 2026-10-27 KST.

## 판정

**출시본의 에셋과 엔진 바이너리는 회수할 수 있지만, 현재 Kotlin 소스는 출시 앱의 원본 프로젝트와 동일하지 않습니다.**
같은 패키지 ID를 쓰므로 업데이트 후보로 개발할 수 있으나, 원본 복구와 기능 재구현을 구분해야 합니다.
Play에 제출할 수 있는 서명·동작 검증이 끝난 릴리스는 아직 없습니다. PR #1은 Draft로 유지합니다.

## 조사 범위와 전제

- `rabbit-skater-v3` main `3a70ee6`, 기존 PR #1 `9e84e75` 및 이번 보완 커밋을 확인했습니다.
- `rabbit-skater` main `56a1e32`의 `3.apk`, `3.aab`와 사용자가 제공한 `4.apk`, `4.aab.zip`을 직접 분석했습니다.
- 원본 파일을 변경하지 않았습니다. 파일명 숫자가 아닌 매니페스트의 versionCode로 버전을 식별했습니다.
- Git 전체 이력에서 `.bbdoc`, C/C++ 원본, 기존 Android Java 소스, 서명키 파일을 찾지 못했습니다.
- 접근 가능한 Library 검색에서 원본 프로젝트 후보가 확인되지 않았습니다. Google Drive의 관련 이름 검색도 결과가 없었으나, 해당 도구는 모든 바이너리 형식을 전수 검색하지 않으므로 백업 부재의 증거는 아닙니다.
- Play Console은 현재 브라우저에서 로그아웃 상태였습니다. 최신 정책 화면, 등록된 인증서, 전체 트랙의 최대 versionCode 및 개인정보처리방침은 이번에 직접 재확인하지 못했습니다.

## 실제 파일 비교

| 항목 | 기존 3.apk / 3.aab | 제공 4.apk / 4.aab.zip | 현재 Kotlin PR |
|---|---|---|---|
| 패키지 ID | com.devbyzenith.rabbitskater | 동일 | 동일 |
| versionCode / versionName | 3 / 1.2 | 4 / 1.3 | 5 / 3.0.2 |
| minSdk / targetSdk | 21 / 31 | 21 / 31 | 24 / 36 |
| 실행 진입점 | PTPlayer | PTPlayer | MainActivity |
| 게임 구조 | Buildbox + Cocos2d-x + libplayer.so | 동일 계열 | Kotlin Canvas/SurfaceView 구현 |
| 네이티브 라이브러리 | ARM64·ARMv7 libplayer.so | 두 파일 모두 v3와 SHA-256 일치 | 활성 모듈에 NDK 의존성 없음 |
| 광고 | AppLovin·Google Mobile Ads·Vungle | 동일 SDK 계열 | AdManager 임시 구현 |
| 사운드 | 음원 9개 포함 | 같은 음원 9개 | SoundManager 로딩 코드 미구현 |
| 빌드 설정 | AAB 메타데이터 기준 AGP 7.2.1 | AGP 7.2.1, bundletool 1.8.2 | AGP 8.9.2, Gradle 8.11.1, Kotlin 2.1.0 |

`4.aab.zip`은 AAB를 담은 외부 ZIP이 아니라 **그 파일 자체가 Android App Bundle**입니다.
내용을 다시 압축하지 않고 `.aab` 확장자의 사본을 만들었으며 SHA-256이 원본과 같습니다.
이 사본은 과거 버전 보존용이며 targetSdk 31의 기존 빌드를 새 업데이트처럼 제출할 수 있다는 의미는 아닙니다.

### 에셋 일치 범위

- 저장소 루트 `assets/`의 파일 104개는 `3.apk`의 `assets/data/`와 전부 바이트 단위로 일치합니다.
- `4.apk`와는 47개 일치, 57개 불일치입니다. 불일치 57개는 모두 `.pack` 파일입니다.
- PNG 14개, plist 5개, MP3 9개는 v3·v4·저장소 사이에서 동일합니다.
- `.pack`의 바이트 차이만으로 레벨이나 게임 규칙 변경을 확정하지 않습니다. 재직렬화·보호 처리 등 가능성을 포함하며 의미 해석은 미검증입니다.
- 제공 v4 APK와 AAB의 DEX·에셋·네이티브 라이브러리는 대응 경로에서 모두 일치합니다.
- AAB에서 에셋 105개(`default.png` 포함)와 네이티브 파일 2개, 총 107개를 원래 바이트 그대로 회수했습니다.

현재 Kotlin 게임은 `.pack`의 레벨·객체 모델을 실행하는 Buildbox 런타임을 사용하지 않습니다.
자체 장애물·코인 생성, 점프·상점·저장 로직이 있으므로 리소스가 같다는 사실을 플레이 동등성으로 해석할 수 없습니다.

### project/의 한계

`settings.gradle.kts`는 루트 `:app`만 포함합니다. `project/`는 활성 빌드가 아닙니다.
그 안에는 SDK 34, NDK 25, Cocos2d-x 3.17 및 광고·Firebase 의존성 선언이 있지만,
실행 가능한 레거시 소스·엔진 프로젝트가 없고 release를 debug 키로 서명하는 임시 설정도 있습니다.
이 설정을 출시본 원본이나 실제 사용 SDK의 증거로 사용하면 안 됩니다.

## 데이터 보안 신고

실제 v4 AAB 의존성 메타데이터와 APK의 DEX에서 확인한 주요 SDK는 다음과 같습니다.

| 구성요소 | 확인된 버전·근거 |
|---|---|
| Google Mobile Ads | play-services-ads / ads-base / ads-lite 21.3.0 |
| Google 광고 ID 라이브러리 | play-services-ads-identifier 18.0.1 |
| AppLovin | applovin-sdk 11.5.3 |
| Vungle | publisher-sdk-android 6.12.0; BuildConfig도 동일 |
| Google measurement 구성요소 | measurement-base / measurement-sdk-api 20.1.2 |
| Buildbox | 광고 어댑터·동의 화면 클래스 존재; 정확한 편집기 버전 미확정 |
| Firebase Analytics/Crashlytics | 이번 배포본에서 해당 SDK를 확정할 근거 없음. Firebase 공통 클래스 3개만으로 분석 수집을 단정하지 않음 |

매니페스트에는 `AD_ID`, 인터넷, 네트워크·Wi-Fi 상태, 대략적 위치, 외부 저장소 등의 권한이 있습니다.
Buildbox 광고 어댑터에서 Google Mobile Ads/AppLovin/Vungle 초기화 호출과 광고 ID 조회 호출도 확인했습니다.
단, 정적 코드 존재는 모든 경로가 실행되거나 모든 권한의 데이터가 전송된다는 증거가 아닙니다.

| 신고 검토 항목 | 현재 판단 | 제출 전 필요한 근거 |
|---|---|---|
| Device or other IDs | 이전 Play 경고와 광고 ID 권한·SDK·호출이 일치함. 최우선 정정 검토 대상 | 실제 SDK 초기화·동의 설정, 광고 ID 전송 여부, 목적·공유 여부 |
| 대략적 위치 | IP 기반 추정 또는 위치 권한 사용 가능성 | 해당 구버전 SDK 문서와 실제 동작 |
| 앱 활동·진단 | 광고 상호작용·측정 SDK로 검토 필요 | 수집 이벤트·진단 전송·비활성화 설정 |
| 암호화·선택권·삭제 | 정적 분석만으로 확정 불가 | 전체 SDK·서비스 처리 방식과 개인정보처리방침 |

공식 Google 문서는 최신 광고 SDK의 데이터 처리 안내이므로 21.3.0에 모든 동작을 그대로 대입하지 않습니다.
현재 Kotlin의 광고 임시 구현으로 과거 배포본을 '데이터 미수집'으로 신고하지 않습니다.
광고를 유지한다면 실제 구성에 맞춰 신고와 개인정보처리방침을 정합화해야 합니다.
광고를 제거하는 재구현을 선택하더라도 현재 배포 중인 다른 트랙·버전의 데이터 처리까지 확인해야 합니다.
이번에는 근거가 부족한 신고를 제출하거나 콘솔 값을 변경하지 않았습니다.

## Android 16 / API 36

공식 Play 안내는 2026-08-31부터 일반 Android 앱의 신규·업데이트 제출에 API 36 이상을 요구하며,
연장이 승인된 경우 2026-11-01까지 시간을 받을 수 있다고 설명합니다. 이 앱의 연장 승인 여부는 미확인입니다.
PR의 compileSdk/targetSdk 36, AGP 8.9.2, Gradle 8.11.1 변경 방향은 유효합니다.
하지만 출시본은 API 31에서 출발하므로 숫자 변경 외에 중간 버전의 동작 변경을 검토해야 합니다.

- edge-to-edge, 디스플레이 컷아웃, 시스템 바와 터치 좌표를 기기에서 확인해야 합니다.
- 게임 카테고리를 명시했습니다. Android 16의 큰 화면 방향 제한 변경에는 게임 예외가 있지만, 태블릿·멀티윈도우 표시 검증을 대체하지 않습니다.
- minSdk 21에서 24로 변경하면 Android 5·6 사용자에게 새 버전이 제공되지 않습니다. 출시 전 지원 범위를 결정해야 합니다.
- 실제 Play의 모든 트랙에서 사용된 최대 versionCode를 확인한 후 5를 그대로 사용할 수 있는지 결정해야 합니다.

## 16KB 페이지 크기

기존 엔진이 무조건 정렬 불합격이라는 결론은 근거가 없습니다.

- v4 ARM64 `libplayer.so`의 두 PT_LOAD p_align은 모두 65,536바이트로 16,384 이상입니다. offset·virtual address의 16KB 합동 조건도 만족합니다.
- ARMv7 파일은 4,096바이트 정렬입니다. 이를 ARM64의 16KB ELF 정렬 실패로 혼동하지 않습니다.
- 제공 APK에서는 두 라이브러리가 ZIP 압축되어 있으며, SDK 36 `zipalign -c -P 16 -v 4` 검사는 성공했습니다. 압축된 `.so`의 ZIP 오프셋이 16KB 배수가 아니라는 이유만으로 실패로 판정하지 않습니다.
- 기존 AAB는 native uncompression 설정이 있지만 명시적 PAGE_ALIGNMENT_16K 값이 없습니다. 최종 AAB에서 생성한 기기별 APK의 패키징 검사가 추가로 필요합니다.
- 엔진 내부의 4KB 가정·mmap 사용 및 실제 16KB 장치 실행은 미검증입니다.
- Kotlin 후보는 활성 빌드에서 네이티브 라이브러리가 없지만, 최종 배포 산출물과 향후 추가 SDK를 다시 검사해야 합니다.

공식 16KB 안내 페이지에는 열람 시 2027-02-01 차단 일정이 표시되어 있고, 과거 2025·2026 공지와 일정이 다릅니다.
개별 앱의 현재 기한은 Play Console에서 확인해야 하며 과거 공지의 기한만으로 이 앱의 차단 상태를 단정하지 않습니다.

## 업로드 서명키

- 두 기존 AAB는 같은 공개 인증서로 서명되어 있습니다: `CN=rabbitskater, O=rabbitskater`, RSA 2048비트.
- AAB 인증서 SHA-256: `3E:2D:29:DA:3D:46:CB:A0:6F:45:95:45:45:9A:78:BD:33:0E:83:9A:3C:FF:A2:6B:07:25:EC:55:DA:52:F1:87`.
- 기존 APK의 서명 검증은 v1·v2·v3 및 SourceStamp 검증을 통과했습니다. APK 인증서 SHA-256은 `20:D5:7A:D9:D9:81:E2:14:5B:3A:F7:0C:57:A7:DC:0A:6F:BA:90:F8:E5:04:6F:AA:EC:96:5D:E8:1F:E0:87:8C`입니다.
- AAB 업로드 인증서 후보와 배포 APK 인증서는 다릅니다. 현재 콘솔에 등록된 각각의 인증서와 대조해야 합니다.
- 공개 인증서와 APK/AAB에서 개인키·키스토어·비밀번호를 복원할 수 없습니다.
- Play App Signing에 등록되어 있다면 업로드 키 분실은 새 키 생성·공개 인증서 제출·재설정 승인으로 해결할 수 있습니다. 앱 서명키와 업로드 키의 역할을 구분해야 합니다.
- 이번에는 새 개인키 생성, 공개 저장소에 키 추가, 업로드 키 재설정 요청, 서명된 새 릴리스 제출을 하지 않았습니다.

## 이번에 진행한 보완

1. `assets.srcDirs("assets")`가 존재하지 않는 `app/assets`를 가리키던 문제를 저장소 루트 경로로 수정했습니다.
2. 원본 plist 5개의 잘못된 자체 닫힘 DOCTYPE을 파서에서만 처리하도록 했습니다. 원본 에셋의 바이트는 변경하지 않았습니다.
3. 실제 plist 5개, 주 아틀라스 151개 프레임, 게임에서 참조하는 스프라이트 및 일반 XML 입력에 대한 회귀 테스트를 추가했습니다.
4. `appCategory="game"`을 명시했습니다.
5. CI에 unsigned release AAB 빌드·release lint·APK/AAB 에셋 104개 해시 검사를 추가했습니다. 검사용 산출물은 14일간 보존합니다.
6. 과거 v4의 에셋·엔진 바이너리 107개와 원본 AAB의 동일 바이트 사본, 매니페스트·SDK·인증서·해시 증거를 복구 패키지에 보존했습니다.

## 검증 기록

검증한 소스 커밋: `77c5b7c699718adc79fbb365b2b8d0860b421b9d`.
[최신 실제 CI 실행](https://github.com/dkdleljh/rabbit-skater-v3/actions/runs/40431204683)은 2026-10-27 10:15 KST에 성공했습니다.

| 검사 | 결과 |
|---|---|
| testDebugUnitTest | 성공: 새 실제 에셋 파서 회귀 테스트 포함 |
| assembleDebug | 성공: 테스트용 debug APK 생성 |
| lintDebug / lintRelease | 성공 |
| bundleRelease | 성공: 업로드 서명이 없는 release AAB 생성 |
| APK·AAB 에셋 검사 | 두 파일 각각 104개 SHA-256 일치 |
| 활성 빌드의 네이티브 병합 | debug·release 모두 로그상 NO-SOURCE |
| 기존 v4 APK 서명·ZIP 정렬 | 성공 |
| 새 릴리스 업로드 키 서명 | 미검증 |
| 기기 설치·플레이·기존 앱 업데이트 | 미검증 |
| Android 16·16KB 런타임 | 미검증 |
| Play 데이터 신고·업로드 심사 | 미수행 |

[검사용 CI 산출물](https://github.com/dkdleljh/rabbit-skater-v3/actions/runs/40431204683/artifacts/12701971548)은 APK·미서명 AAB·테스트 결과·린트 보고서를 포함하며 2026-11-10까지 보존됩니다. 다운로드에 GitHub 로그인이 필요할 수 있습니다.

기존 v4 AAB는 bundletool 1.18.3의 구조 검증을 통과했습니다. 이 검사는 Play 정책 통과·런타임·업로드 키 유효성을 보장하지 않습니다.
로컬 Gradle 재현은 이 환경의 다운로드 연결 단계에서 실패했습니다. 소스 컴파일 여부는 GitHub Actions의 실제 실행 로그로 구분해 검증합니다.
기존 APK 서명 검증과 새 릴리스 서명 검증을 혼동하지 않습니다.

## 실제 배포 가능한 프로젝트로 가는 경로

| 경로 | 재사용할 수 있는 것 | 추가로 필요한 것 | 판정 |
|---|---|---|---|
| Buildbox 원본 복구 | 회수한 이미지·음원·레벨 pack·기존 동작 참고 | `.bbdoc` 또는 당시 Android 내보내기 전체 폴더, 호환 Buildbox/엔진, 최신 광고 SDK, API 36 빌드·서명·검증 | 원래 게임 보존에 가장 직접적인 경로 |
| 기존 export 복구 | Java 래퍼의 역컴파일 참고, `.so`와 에셋 | 누락된 빌드·브리지·JNI 계약, 엔진 호환성 및 재배포 권한 검증 | 실험 가능하나 원본 소스 복원이나 배포 가능성을 보장하지 못함 |
| Kotlin 재구현 | 현재 컴파일 가능한 게임·이미지·음원 | 기존 게임의 메뉴·레벨·난이도·물리·광고·오디오·저장·결제/게임서비스 기능 목록과 대조, 누락 기능 구현·스토어 설명 정합화 | 원본 복구가 아닌 새 구현의 업데이트 후보 |
| APK 매니페스트만 패치 | 기존 바이너리 | 현대 플랫폼 동작·SDK·서명·정책 문제를 해결하지 못함 | 배포 해결책으로 채택하지 않음 |

### 남은 실행 순서

1. `.bbdoc` 또는 당시 Android export 백업 유무를 확인합니다. 현재 회수 자료는 편집 가능한 Buildbox 프로젝트가 아닙니다.
2. 콘솔에 로그인해 현재 배포 트랙, 정책 경고, 앱 서명·업로드 인증서와 최대 versionCode를 대조합니다.
3. 원본 복구 또는 Kotlin 재구현 경로를 결정하고, 구버전과 화면·조작·진행·저장·광고 동작 비교표를 작성합니다.
4. 데이터 처리·SDK·개인정보처리방침을 맞추고 release AAB를 실제 업로드 키로 서명합니다.
5. 최종 AAB에서 APK를 생성해 서명·패키징·ELF를 검증합니다. Android 16의 4KB/16KB 환경, 가로 화면·태블릿, 백그라운드 복귀, 터치·오디오·저장·광고 동의를 테스트합니다.
6. 기존 Play 서명 앱에서 업데이트 설치와 저장 데이터 유지까지 검증한 뒤 테스트 트랙에 제출하고 사전 출시 보고서를 확인합니다.

## 재현 도구와 주요 명령

사용 도구: Python 3.12, androguard 4.1.4, pyelftools 0.33, bundletool 1.18.3, Android SDK Build Tools 36.0.0.

```bash
sha256sum 4.apk 4.aab.zip 3.apk 3.aab
java -jar bundletool.jar dump manifest --bundle=4.aab.zip
java -jar bundletool.jar dump config --bundle=4.aab.zip
java -jar bundletool.jar validate --bundle=4.aab.zip
keytool -printcert -jarfile 4.aab.zip
apksigner verify --verbose --print-certs 4.apk
zipalign -c -P 16 -v 4 4.apk
./gradlew --no-daemon testDebugUnitTest assembleDebug lintDebug bundleRelease lintRelease
python3 scripts/verify_game_assets.py app/build/outputs/apk/debug/app-debug.apk app/build/outputs/bundle/release/app-release.aab
```

## 참고 자료

아래 공식 문서는 2026-10-27 KST 열람했습니다. 갱신일이 표시되지 않은 문서는 열람일 기준 내용으로 사용했습니다.

- [Play 대상 API 요구사항](https://support.google.com/googleplay/android-developer/answer/11926878): 시행 기준 2026-08-31; 별도 갱신일 미표시.
- [Android 16 동작 변경](https://developer.android.com/about/versions/16/behavior-changes-16): 게임 카테고리 예외와 edge-to-edge 확인.
- [16KB 페이지 지원](https://developer.android.com/guide/practices/page-sizes): ELF·ZIP 정렬과 실행 테스트를 구분.
- [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756): 업로드 키와 앱 서명키, 재설정 절차.
- [Google 광고 SDK 데이터 공개](https://developers.google.com/admob/android/privacy/play-data-disclosure): 2026-10-26 UTC 갱신; 최신 SDK 기준임에 유의.
- [Vungle v6 고급 설정·데이터 공개](https://support.vungle.com/hc/en-us/articles/18092176310043-Advanced-Settings-for-Vungle-SDK-v-6-on-Android-and-Amazon).
- [Buildbox Android export 설명](https://help.buildbox.com/en/articles/1939625-creating-signed-apk-with-android-studio): BBDOC에서 Android 프로젝트를 내보내는 절차.
- [PR #1](https://github.com/dkdleljh/rabbit-skater-v3/pull/1).
