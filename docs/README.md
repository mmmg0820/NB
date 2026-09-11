# 샤로먕 유지보수 문서

13개 팀의 인수인계 문서를 통합했다. 앱 기준은 `b4ffe8718bf01b02930468451baa4ecab45f1db3` / `v0.1-dev-20260911-r2`다. 문서 통합은 APK 변경이 아니다.

처음에는 [업무 운영](coordination/README.md) → [Android 플랫폼](android-platform/README.md) → 담당 기능 문서 → [QA](qa/README.md) → [릴리스 운영](release-operations/README.md) 순서로 읽는다. 문서의 로컬 절대경로는 당시 내부 근거 위치이며, 공개 링크가 아니므로 현재 존재와 정본 여부를 다시 확인한다.

| 영역 | 문서 |
| --- | --- |
| 업무 운영·소유권 | [조정](coordination/README.md) |
| Android 구조·상태·빌드 | [Android 플랫폼](android-platform/README.md) |
| 브랜드·제품 화면 | [제품 디자인](premium-design/README.md) |
| 토큰·공통 컴포넌트 | [디자인 시스템](design-system/README.md) |
| 질문·선택·복원 | [타로 흐름](tarot-flow/README.md) |
| 리딩 키·좌표·라벨 | [타로 스프레드](tarot-spreads/README.md) |
| 카드 이미지·접근성 | [타로 자산/접근성](tarot-assets-accessibility/README.md) |
| 사주 입력·결과 | [사주 UI](saju-ui/README.md) |
| 시간·계산·검증 상태 | [사주 엔진 계약](saju-engine-contract/README.md) |
| 저장·스키마·복구 | [기록 데이터](records-data/README.md) |
| 요구사항-코드-근거 | [추적성](traceability/README.md) |
| 검수 범위·실패 이력 | [QA](qa/README.md) |
| Git·배포·보관 | [릴리스 운영](release-operations/README.md) |

## 현재 상태와 통합 정정

현재 배포 근거는 [루트 evidence 안내](../evidence/REPORT.md)와 [manifest](../evidence/release-manifest.json)다. R2의 94개 테스트와 지정 회귀 PASS를 모든 제품 기능의 인증으로 확장하지 않는다.

[후속 목록](BACKLOG.md)의 A-01/T-05/R-01은 미완료다. 특히 타로 스프레드 제출 문서의 적응형 78장/5열 모드 설명은 기준 코드와 다르다. 78장은 기준 코드에서 8열×10행으로 고정되며, 해당 문서의 통합 정정 주석을 먼저 읽는다.

팀 문서의 “이번 작성에서 공유 소스를 수정하지 않았다” 등은 각 팀 원래 제출 시점의 설명이다. 통합 기록은 [manifest](manifest.json)와 [감사 기록](INTEGRATION-AUDIT.json)을 따른다. 릴리스 운영 문서 12절의 제안 경로와 달리 실제 통합은 위 제출 팀 디렉터리 이름을 유지했다.

## 무결성 확인

[manifest.json](manifest.json)은 13개 제출본 SHA와 통합본 SHA를 연결한다. [SHA256SUMS](SHA256SUMS)는 팀 문서·색인·후속 목록·manifest·감사 기록을 저장소 루트 상대경로로 나열한다. 목록 파일 자신의 해시는 자기 안에 넣지 않는다.

```bash
shasum -a 256 -c docs/SHA256SUMS
```

원문 정정이 필요한 팀은 후속 문서 변경과 이유를 남긴다. 문서 예제 명령은 이번 통합에서 실행한 테스트를 의미하지 않는다.
