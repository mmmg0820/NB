# 현재 배포 근거: R3 통합 개발판

이 경로는 현재 권장 개발 APK를 안내한다. 기본값은 R3이며 R2와 R1은 이력이다.

- 앱 코드 커밋: `fb8db44f768199e7d507624c716eac291ae97fdb`
- 태그: 없음
- [APK](../releases/2026-09-11-r3/sharomyang-correction-r3-debug.apk)
- APK SHA-256: `fda7a48369623256154bc39620b073df4fa28fd48ec1f263d191e912e2bba4ad`
- [현재 manifest](release-manifest.json): 모든 파일 경로는 저장소 루트 기준
- [R3 상세 보고서](correction-r3/REPORT.md), [R2 상세 보고서](saju-metadata-r2/REPORT.md), [QA 인수인계](../docs/qa/README.md)

R3는 승인된 사주 시간 미상, 기록 필터·상태, 타로 선택 접근성 및 결과 반응형 수정을 통합했다. 기기 스모크에서 발견된 사주 재생성 및 타로 재탭 회귀도 수정했다. 클린 빌드와 단위 테스트 120건이 통과했다.

Pixel 9a에서 동일 APK 해시로 사주 시간 미상 테마 재생성, 타로 원시 탭·접근성 재탭, 셔플 보존 및 1장 결과를 검증했다. 이 산출물은 debug 검수본이며 전체 제품·스토어 출시 인증이 아니다.

## 보존된 이전 이력

[R1 원문 manifest와 보고서](history/r1/README.md)는 기존 루트 파일과 바이트가 같다. R1 문서의 당시 PASS 표기는 이후 독립 QA의 `FINAL_FAIL/HOLD`를 뒤집지 않는다. R1 APK와 태그는 삭제하거나 이동하지 않는다.

R2 태그와 APK는 이력 보존용으로 유지한다.
