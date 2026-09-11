# 현재 배포 근거: R2 개발판

이 경로는 현재 권장 개발 APK를 안내한다. 기본값은 R2이며 R1은 이력이다.

- 앱 코드 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- 태그: `v0.1-dev-20260911-r2`
- [APK](../releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk)
- APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- [현재 manifest](release-manifest.json): 모든 파일 경로는 저장소 루트 기준
- [동결 당시 manifest](saju-metadata-r2/release-manifest.json), [R2 상세 보고서](saju-metadata-r2/REPORT.md), [94개 테스트 XML](saju-metadata-r2/unit-results/), [QA 인수인계](../docs/qa/README.md)

R2는 사주 기본 결과에서 내부 계산 시각 문구를 제거하고 명시적인 계산 기준 팝업으로 옮겼다. 시간 유/무·라이트/다크 결과, 검토 배지·시주 의미·주 순서, 타로 선택 후 같은 카드 재탭에 대한 지정 회귀가 승인됐다. APK와 추출 설치본 해시가 일치했다.

승인은 debug 빌드의 S1 수정과 지정 회귀 범위다. 전체 제품·스토어·광범위한 접근성/반응형 인증은 아니다. 샤로먕 관련 엄격 로그 이벤트는 0이며, 전체 장치 로그에는 이전의 무관한 시스템 앱 ANR 줄 6개가 보존돼 있다.

## 보존된 이전 이력

[R1 원문 manifest와 보고서](history/r1/README.md)는 기존 루트 파일과 바이트가 같다. R1 문서의 당시 PASS 표기는 이후 독립 QA의 `FINAL_FAIL/HOLD`를 뒤집지 않는다. R1 APK와 태그는 삭제하거나 이동하지 않는다.

이번 O-01/문서 통합은 기본 포인터와 문서만 변경한다. A-01/T-05/R-01 코드는 포함하지 않았다.
