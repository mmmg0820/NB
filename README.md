# 샤로먕 Android

HyperOS Design Standard 긴급 수정과 사주/타로 입력 및 결과 UX를 반영한 Android 소스입니다.

## APK

- 파일: `releases/2026-09-11-r3/sharomyang-correction-r3-debug.apk`
- SHA-256: `fda7a48369623256154bc39620b073df4fa28fd48ec1f263d191e912e2bba4ad`
- 패키지: `com.hoscat.mtj.dev`
- 버전: `0.1-dev` (`versionCode 1`)

## 검증

- 클린 빌드 및 단위 테스트 120건 통과
- Pixel 9a 대상 사주 재생성, 타로 재탭·셔플·1장 결과 스모크 통과
- 상세 결과: `evidence/correction-r3/REPORT.md`

사주 결과의 계산 시각은 `계산 기준` 상세 팝업에서 한국어 날짜와 시간대로 표시합니다. 결과 화면의 `검토 필요` 상태와 시간 모름 처리는 유지됩니다.

이전 `v0.1-dev-20260911` 태그의 APK는 결과 화면에 기술 메타데이터가 노출되는 문제가 있어 이번 수정본으로 대체되었습니다. 이전 파일과 태그는 이력 보존용입니다.

## 구조

- `android/`: Android 애플리케이션
- `saju/`: 사주 계산/계약 소스
- `tarot/`: 타로 계산/계약 소스
- `records/`: 기록 저장 계약 소스
- `design/native/`: 공용 Compose 디자인 컴포넌트

`ANDROID_USER_HOME`에 디버그 키스토어가 준비된 환경에서 Android Gradle 빌드를 실행합니다.

## 유지보수 문서와 현재 배포 근거

- [팀별 인수인계 색인](docs/README.md): 13개 팀 문서, 소유권, 데이터 계약, 후속 과제
- [현재 배포 근거](evidence/REPORT.md) 및 [현재 manifest](evidence/release-manifest.json): R2 APK와 검수 범위
- [문서 SHA 목록](docs/SHA256SUMS)과 [제출 원본/통합본 목록](docs/manifest.json)
- [미완료 작업](docs/BACKLOG.md): A-01/T-05/R-01은 별도 Android/QA 승인 전까지 미완료

R3 앱 기준 커밋은 `fb8db44f768199e7d507624c716eac291ae97fdb`이다. 이전 R1 보고서와 manifest는 [이력 보관소](evidence/history/r1/README.md)에 원문 바이트로 보존한다.
