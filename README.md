# 샤로먕 Android

HyperOS Design Standard 긴급 수정과 사주/타로 입력 및 결과 UX를 반영한 Android 소스입니다.

## APK

- 파일: `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`
- SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- 패키지: `com.hoscat.mtj.dev`
- 버전: `0.1-dev` (`versionCode 1`)

## 검증

- 클린 빌드 및 단위 테스트 94건 통과
- Pixel 10 AVD, Android API 37, 1080x2424, fontScale 1.0
- 설치된 base APK와 배포 APK SHA-256 일치
- 상세 결과와 캡처: `evidence/saju-metadata-r2/`

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

앱 기준 커밋은 `b4ffe8718bf01b02930468451baa4ecab45f1db3`이다. 문서 통합 커밋은 APK를 다시 빌드하거나 앱 태그를 이동하지 않는다. 이전 R1 보고서와 manifest는 [이력 보관소](evidence/history/r1/README.md)에 원문 바이트로 보존한다.
