# R3 통합 검증 보고서

- 코드 커밋: `fb8db44f768199e7d507624c716eac291ae97fdb`
- APK: `releases/2026-09-11-r3/sharomyang-correction-r3-debug.apk`
- APK SHA-256: `fda7a48369623256154bc39620b073df4fa28fd48ec1f263d191e912e2bba4ad`
- 서명 인증서 SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`
- 패키지: `com.hoscat.mtj.dev`

## 포함 변경

- 사주 시간 미상 셀의 중복 문구와 강제 잘림 제거, 큰 글씨 2x2 배치 계약 유지
- 기록 화면의 `전체 / 사주 / 타로 / 기록` 필터와 로딩·오류·빈 상태 분리
- 타로 78장 선택 화면의 선택 대비, 접근성 의미, 셔플 아이콘 및 상태 회귀 검사
- 타로 결과의 카드 수별 크기, 저장/새로 뽑기 위계, 실시간·저장 기록 요약 통일

## 자동 검증

- Gradle `:app:clean :app:testDebugUnitTest :app:assembleDebug`: PASS
- 단위 테스트: 120건, 21개 스위트, 실패 0, 오류 0, 건너뜀 0
- `git diff --check`: PASS
- APK 서명 검증: PASS

## 기기 검증

Pixel 9a에서 설치된 base APK 해시 일치 후 다음 항목을 검증했다.

- 사주 시간 미상 결과의 다크→라이트 재생성 유지: PASS
- 타로 결과→뒤로가기→원시 탭 재선택 해제: PASS
- 접근성 클릭 재선택 해제: PASS
- 부분 선택 상태의 셔플 보존: PASS
- 1장 결과 요약·배치·하단 CTA와 내비게이션 비중첩: PASS
- 검수 구간의 앱 엄격 로그 오류: 0

상세 판정은 `device/QA-CLOSURE.md`와 `device/verdict.json`에 보존한다. 이번 판정은 지정된 Pixel 스모크 범위이며 전체 기기·내구성·스토어 출시 인증을 의미하지 않는다.
