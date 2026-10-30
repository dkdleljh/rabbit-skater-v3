# Google Play 2026-10-29 정책 영향 검증

검증 기준일·공식 문서 열람일: 2026-10-30 KST.

## 결론과 증거 범위

- 거래자/비거래자 지위 신고는 전 세계 활성 계정의 의무다. 2026년 11월은 시작 시점이고 기존 계정 마감은 2027-02-01이다. 이 계정의 완료 여부와 신고 유형은 미확인이다.
- 현재 브라우저에서 지정 Play Console 주소를 열었으나 Google 계정 선택 화면으로 이동했고 표시된 계정들은 로그아웃 상태였다. 현재 트랙·경고·신고·스토어 분류·배포 국가·Android vitals를 읽지 못했다.
- main `3a70ee69f942fae85a97460c4ee4ad07af2a1e59`는 API 34/versionCode 4, PR #1의 조사 시작 head `bebd08add6eb95273db154e1f68dc9c5c12dbbae`는 API 36/versionCode 5다. PR은 open·Draft·미병합이다.
- 보존한 기존 versionCode 4 AAB와 그때 추출한 manifest/SDK 증거를 재검사했다. 이 파일이 **지금 Production에 제공되는 최신 파일인지**는 Console 확인 전까지 미확인이다. Kotlin 소스·비활성 project/ 설정·기존 AAB를 서로 다른 증거로 취급한다.
- 이메일 발신일은 2026-10-29 12:11:19 PDT이며 KST 환산 수신 시각은 **2026-10-30 04:11:19**이다. 정책 공지일 자체는 10월 29일이다. Gmail 인증 헤더의 DKIM/SPF/DMARC pass 및 공식 정책 공지의 대응 내용을 확인했다.

## 시행일과 실제 적용

| 항목 | 공식 일정 | 이 앱에 대한 판단 |
|---|---|---|
| 거래자 지위 | 2026-11 시작, 기존 계정 2027-02-01 마감 | 계정 신고 필요. 이미 완료됐는지는 미확인 |
| 로컬 네트워크 ACCESS_LOCAL_NETWORK | 2027-08-25; API 37+ | Kotlin 직접 선언·보존 v4 manifest에 없음. 확인한 버전은 비대상; 다른 활성 트랙은 미확인 |
| 연락처 관리 MANAGE_CONTACTS | 2027-08-25 | 두 증거에 없음. 비대상. 정책 안내 요약은 API 38+를 명시하지만 본문의 신고 문구는 모든 사용 앱이라고 기술하므로 향후 도입 시 재확인 |
| 기존 연락처 읽기 READ_CONTACTS | 2027-01-27; API 37+의 광범위 접근·신고 | 두 증거에 없음. 비대상. 위 MANAGE_CONTACTS 신규 정책과 구분 |
| 위치 최소 범위·정밀 위치 신고 | 2027-01-27; location button은 API 37+ | Kotlin 위치 권한 없음. v4는 COARSE만 있고 FINE/BACKGROUND 없음. 정밀 위치 신고·버튼 구현은 해당 증거에서 비대상. COARSE 접근 필요성·동의·데이터 공개 검토는 여전히 필요 |
| SMS/통화기록·지오펜싱 FGS | 2027-01-27 | v4에 READ_CALL_LOG/SMS/위치 FGS 지정 없음. 일반 FOREGROUND_SERVICE 선언만으로 지오펜싱 사용을 단정하지 않음 |
| AppFunctions | 2027-04-14 | 확인한 manifest에 서비스 선언 없음. 사용 증거 없음 |
| 자격 기반 할인 구독 | 2027-01-27 | Kotlin 의존성·보존 manifest에 결제 권한 없음. 구독 사용 증거 없음. 계정 다른 앱까지 면제되는 것은 아님 |
| 메모리 품질 | 2027-02, 구체적 일자는 문서 미표시 | 모바일·태블릿 게임에 적용. Android 13+ 측정 데이터, 최근 28일 P90 확인 필요. API 36이라고 면제되지 않음 |
| DEX 최적화·난독화·축소 | 2027-02; 게임 DEX >50MB, 일반 앱 >10MB에서 각 25% | 보존 v4 DEX 약 14.86MB. **Console도 게임 분류라면** 크기 기준상 비대상. 일반 앱 분류라면 해당할 수 있음. 실제 Console 지표로 확정 |
| Zero-Tap 로그인 복원 | 2027-04 | 현재 게임은 범위 밖. Play Games 인증 라이브러리 존재만으로 새 로그인 구현을 강제하지 않음 |
| Families 광고 SDK | 기존 정책, 10/29는 안내 보완 | 대상 연령·어린이/연령 미확인 이용자 광고 제공 여부에 따라 적용. 귀여운 캐릭터나 콘텐츠 등급만으로 대상 연령 신고를 추정하지 않음 |
| 일본 판매 관련 승인 | 공지상 2026-12-01 | 일본을 통한 유료 판매·결제 계정 여부 미확인. 일본 배포 또는 광고 SDK 존재만으로 지급 보류 대상이라고 판정하지 않음 |
| 자동차·XR | 해당 폼팩터 제공 시 | 현 Kotlin 앱에 해당 구현 증거 없음. 현 단계 별도 작업 근거 없음 |

## 거래자 신고: 근거를 먼저 확인

신고 의무와 'trader 선택'은 다르다. 개인 개발자·한국 거주·무료 앱이라는 이유만으로 non-trader를 선택하지 않는다.

1. 계정 관리 > 계정 세부정보 > 내 정보(About you): 신고 상태, 계정 유형, 법적 이름·연락처를 읽는다.
2. Rabbit Skater 및 계정 내 다른 앱: EEA/UK 배포 국가, 유료/IAP/정기 구독/광고 수익화 상태를 확인한다.
3. 정기적·상업적 수익 목적, 실제 운영 형태, 거래 이력과 사용자 설명을 근거로 유형을 확정한다. 광고 SDK가 패키징됐다는 사실만으로 실제 광고 수익이나 상업적 운영을 확정하지 않는다.
4. 공식 문서는 EEA/UK에 배포하지 않는 경우 non-trader 신고가 가능하다고 안내한다. **계정 내 모든 관련 앱의 배포 상태**로 판단한다.
5. trader이면 계정 신고 후 앱 콘텐츠에서 앱별 확인 및 이용약관 URL을 준비한다. 개인 계정의 전화번호·주소 공개와 법 준수 자기확약이 수반될 수 있다. 공개할 정보와 최종 확약을 검토한 뒤 제출한다.

현재는 계정 증거가 부족하므로 어떤 지위도 대신 선택·제출하지 않았다. 개발자 신원 인증 완료가 거래자 신고 완료를 의미하지 않는다.

## 보존 v4 AAB의 재현 가능한 측정

파일 SHA-256:
`7fec20473a7c92eeb214ac56bc74cf4bdc9630f02cd6962c7b504e1ea6336e6e`

보존 패키지의 원본 체크섬과 일치함을 재확인했다.

| 항목 | 결과 |
|---|---|
| applicationId / versionCode | com.devbyzenith.rabbitskater / 4 |
| minSdk / targetSdk | 보존 manifest 기준 21 / 31 |
| DEX classes.dex | 7,480,964 bytes |
| DEX classes2.dex | 7,377,920 bytes |
| 합계 | 14,858,884 bytes = 14.858884 decimal MB = 14.170536 MiB |
| 네이티브 | ARM64 및 ARMv7 libplayer.so |
| 광고 SDK 증거 | Google Mobile Ads 21.3.0, AppLovin 11.5.3, Vungle 6.12.0, UMP 2.0.0 |
| 광고 ID | com.google.android.gms.permission.AD_ID |
| 위치 | ACCESS_COARSE_LOCATION만 있음 |
| 기타 | INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, WRITE_EXTERNAL_STORAGE, BIND_APPHUB_SERVICE, WAKE_LOCK, RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE |
| 미선언 | ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION, READ_CONTACTS, MANAGE_CONTACTS, ACCESS_LOCAL_NETWORK, MANAGE_EXTERNAL_STORAGE, READ_CALL_LOG |

Manifest·SDK 목록은 보존 패키지의 추출 증거다. DEX 총량과 AAB 해시는 이번에 보존 바이너리에서 다시 계산했다. IP 기반 위치 추정은 위치 권한 없이도 가능하므로 'FINE 없음'을 '위치 데이터 미수집'으로 해석하지 않는다.

현재 활성 Kotlin 모듈에는 직접 권한 INTERNET/ACCESS_NETWORK_STATE, 광고 SDK 의존성 없음, AdManager 임시 구현이 확인된다. project/의 FINE·미디어 권한과 광고/Firebase 의존성은 활성 빌드나 실제 배포본의 증거가 아니다.

## 개인정보 공개·광고 SDK

이 항목은 이번 공지로 처음 생긴 의무가 아니라 기존의 준수 항목이다.

- 데이터 안전 신고, 광고 포함 여부, 광고 ID 신고, 개인정보처리방침을 모든 활성 버전·SDK 동작과 대조한다.
- 보존 v4의 SDK·초기화/ID 조회 증거는 이전 복구 보고서에 있다. 실제 활성화·동의·전송을 확인하지 않은 항목은 '검토 대상'이다. SDK 존재만으로 수집·공유·필수/선택·TLS·삭제 가능 여부를 대신 확정하지 않는다.
- 검토할 데이터 유형: 기기/기타 ID, 대략적 위치(IP 추정 포함), 앱 활동/광고 상호작용, 진단. 최신 Google 문서는 최신 SDK 25.5.0 기준으로, 구버전 21.3.0에 모든 동작을 그대로 대입하지 않는다.
- 현재 개인정보처리방침 URL·본문, 앱 내부 공개, 실제 광고 동의 화면, 연령 설정은 Console/기기 미접근으로 미검증이다. 확인되지 않은 문구를 게시하지 않았다.
- 모든 앱은 Console 및 앱 내부에 개인정보처리방침이 필요하다. 공개 URL, 앱/개발자 식별, 문의 경로, 수집·이용·공유·보유·삭제 방침과 실제 구현을 맞춘다.
- 어린이 또는 연령 미확인 사용자에게 광고를 제공하면 해당 SDK 버전·중개 광고 소스의 Families 자격과 연령 분기·개인화 제한을 확인한다. 공식 목록은 AppLovin의 프로그램 탈퇴를 안내하므로 어린이 광고 경로에 사용 중이면 교체/차단이 필요하다.
- 광고 유지/제거는 실제 제품 동작을 바꾸므로 아직 선택하지 않았다. 비활성 project/의 버전 번호만 올려 해결했다고 보고하지 않는다.

## PR #1 기존 작업과 새 조치

| 기존 Android 16/복구 작업 | 이번 정책 감사에서 추가한 작업 |
|---|---|
| compileSdk/targetSdk 36, versionCode 5 후보 | 정확한 신규 정책별 일정·대상 구분 |
| AGP·Gradle 변경, 에셋 패키징·파서 수정 | 거래자 계정/앱별 신고 근거 체크리스트 |
| 빌드·린트·104개 에셋 해시 CI | 보존 v4 DEX 실측, 향후 CI DEX·네이티브 목록 기록 |
| 서명·게임 동등성·업데이트/기기·16KB 검증은 미완료 | 데이터 공개·어린이 광고·메모리 P90 확인 과제 정리 |

API 36은 2026-08-31부터 일반 신규 앱·업데이트 제출 기준이며 연장은 승인 시 2026-11-01까지다. 이 앱의 연장 승인은 미확인이다. 이는 10/29 새 권한 정책과 별개다.
16KB 호환성 문서는 열람일 현재 2027-02-01 업데이트 차단 기준을 안내한다. 이는 2월 메모리 P90·DEX 최적화 정책과도 별개다. 최종 AAB 파생 APK와 16KB 기기 테스트가 필요하다. 기존 ARM64 정적 정렬이 양호하다는 이유로 런타임까지 통과했다고 판단하지 않는다.

이번에는 릴리스 최적화를 무조건 켜지 않았다. 게임 DEX 50MB 조건은 보존 v4에서 충족되지 않으며, 실제 Kotlin 릴리스 크기와 Console 최적화 비율을 먼저 확인한다. isMinifyEnabled=false만으로 즉시 정책 위반을 선언하지 않는다.

## 우선순위·완료 조건

| 우선 | 항목 | 상태 / 완료 조건 |
|---|---|---|
| P0 | Console 로그인 및 현재 정책 상태·API 연장·트랙·앱 등록 대조 | 로그인 필요. 현재 화면 읽기 전까지 경고 해소/정상 계정이라고 보고하지 않음 |
| P0 | Data safety·광고 ID·개인정보 공개 | 광고 ID/SDK 증거 재확인 완료. 현재 신고·실제 동의/전송 검증 및 근거 있는 정정 제출은 미완료 |
| P0 | 출시 후보 서명·동등성·기기·업데이트 검증 | 기존 PR 차단 유지. 병합·업로드·배포 미수행 |
| P1 | 거래자 지위 | 일정·절차 확인 완료. 2027-02-01 전 근거 확인 후 계정/앱별 신고 |
| P1 | 일본 판매 승인 | 2026-12-01 조건부. 판매·결제 계정 근거 확인 |
| P1 | 어린이 광고 | 대상 연령/노출 경로 확인. 적용 시 SDK·중개 네트워크·동의 동작 수정 |
| P1 | 메모리·bitmap P90 | 2027-02 전 Console의 28일 지표/기기 RAM 구간별 검사 및 프로파일링 |
| P2 | 신규 권한 정책 | 확인한 버전 비대상. 최종 merged manifest와 모든 활성 트랙에서 재확인 |
| 완료 | 문서 정비·DEX 자동 측정 | 체크리스트의 고정 SDK 34 표기를 분리하고 CI에 산출물 크기/해시 목록 추가 |

## 재현과 자동 점검

```bash
python3 scripts/inspect_play_bundle.py Rabbit_Skater_4_Original.aab --category game
python3 scripts/inspect_play_bundle.py app/build/outputs/bundle/release/app-release.aab --category game
```

스크립트는 압축 전 DEX 바이트, MB/MiB, SHA-256, 네이티브 목록만 출력한다. 단위 경계는 Console 확인 대상으로 표시하며 정책 합격을 선언하지 않는다. 게임 분류 인자는 로컬 분석 가정이며 Console 분류를 검증하지 않는다. 메모리·권한·SDK 데이터 처리·최적화 비율을 이 도구로 판정하지 않는다.

## 공식 근거

모든 링크는 2026-10-30 KST 열람. 별도 갱신일 미표시 문서는 열람 당시 내용 기준.
- [10/29 정책 공지](https://support.google.com/googleplay/android-developer/answer/18100766) — 공지 2026-10-29.
- [Policy Deadlines](https://support.google.com/googleplay/android-developer/table/12921780?hl=en).
- [Trader status](https://support.google.com/googleplay/android-developer/answer/17594752).
- [권한 정책 사전 안내](https://support.google.com/googleplay/android-developer/answer/16909972).
- [기술 품질 기준](https://support.google.com/googleplay/android-developer/answer/17492799) — 원 발표 2026-08-26, 시행 2027-02.
- [User Data](https://support.google.com/googleplay/android-developer/answer/10144311).
- [광고 SDK 데이터 공개](https://developers.google.com/admob/android/privacy/play-data-disclosure?hl=en) — 갱신 2026-10-28 UTC, 최신 SDK 기준.
- [Families SDK 목록](https://support.google.com/googleplay/android-developer/answer/12955712), [Families 정책](https://support.google.com/googleplay/android-developer/answer/9893335).
- [대상 API](https://support.google.com/googleplay/android-developer/answer/11926878).
- [16KB 지원](https://developer.android.com/guide/practices/page-sizes).
- [기존 복구 증거](RABBIT_SKATER_RECOVERY_20261027.md), [PR #1](https://github.com/dkdleljh/rabbit-skater-v3/pull/1).

Families SDK 목록 상단은 신규 신청 중단이라고 쓰고 10/29 공지는 재개를 안내하여 갱신 차이가 있다. 이는 SDK 공급자 신규 신청 상태의 차이이며, 이 앱의 광고 SDK 인증 여부를 자동으로 변경하지 않는다.

