# fda7a483 독립 최종 스모크: 범위 한정 PASS

전체 Production 인증이 아니라 조정팀이 요청한 짧은 회귀 검수의 PASS다. 이전 후보의 사주 결과 복귀/타로 선택 취소 실패는 아래 동일 기능의 새 증거 범위에서 해소됐다.

## 대상
- 코드 `fb8db44f768199e7d507624c716eac291ae97fdb`.
- 산출물 `/tmp/nb-final-integration-20260911b/releases/2026-09-11-r3/sharomyang-correction-r3-debug.apk`.
- SHA256 `fda7a48369623256154bc39620b073df4fa28fd48ec1f263d191e912e2bba4ad`. 실제 설치 base.apk를 pull하여 독립 대조했다. QA 재설치 없음.
- package/focus `com.hoscat.mtj.dev`, Pixel_9a/emulator-5554/API37, 1080x2424, density420, fontScale1.0.
- 기존 owner의 카드1/2 선택 상태를 인계받았고, QA가 이후 결과 진입/Back/취소/셔플을 직접 수행했다. 질문 TAR1-final은 owner 입력값이며 QA의 신규 질문 입력 테스트로 주장하지 않는다.

## 판정과 증거
| 항목 | 결과 | 증거 | 실제 확인 |
|---|---|---|---|
| 설치 SHA/실행 상태 | PASS | installed.json, installed-base.apk, 01-current.* | 설치 파일 해시 일치, 대상 앱 focus |
| 사주 시간 모름 결과 dark→light | PASS | 18-saju-filled.*, 19-saju-unknown-dark.*, 20-saju-unknown-light.* | QAfda/19900123/양력/시간 모름. 재계산 없이 동일 명식, 시주 임의 산출 없음 |
| 결과→Back→터치 취소 | PASS | 03-picker3.*, 04-result-before.*, 05-back3.*, 06-raw-retap2.* | 카드1/2/77로 결과 진입. Back 후 카드77 중앙 (722,2100) 재탭: 3/3→2/3, 해당 선택/결과 서랍 해제 |
| 별도 접근성 취소 | PASS | 07-a11y-action.json, 07-a11y-retap1.* | 카드2 ACTION_CLICK exit0/true; 실제 화면도 2/3→1/3, 카드1만 선택 |
| 부분 셔플 위치·카드 유지 | PASS 표본 | 08-shuffle-partial1.*, 09-shuffled-complete3.*, 10-result-after-shuffle.*, 04-result-before.* | 카드1 위치 유지. 첫 카드 완드10 유지, 나머지 결과 카드는 변경되어 셔플 반영 육안 확인 |
| 78장/마지막 카드 | PASS 표본 | 02-picker-baseline.*, 14-one-picker0.*, 15-one-picker1.* | XML 카드1~78 존재, 마지막 카드78 실제 선택하여 1/1 |
| 1장/3장 결과 배치·CTA | PASS 표본 | 10-result-after-shuffle.*, 16-one-result.*, cta-bounds.json | 요약/질문/주요 카드 전체 이미지와 위치 라벨 분리, 저장/새로뽑기/카드별설명 노출. 새로뽑기 진입 동작 확인 |
| strict 로그 | 수집 구간 PASS | logcat-full.txt, logcat-strict.txt | 21:50:17~21:54:46 KST FATAL/ANR/OOM/am_crash/am_anr 0건 |

## 간격·이미지 해석
- 결과 두 화면 모두 저장/새로뽑기 clickable bottom2020, 카드별설명 bottom2147, nav top2193(px). nav overlap0.
- 위 값은 내부 클릭 영역 기준이며 action panel 외곽8dp 충족을 뜻하지 않는다. 이번 요청은 결과 배치/CTA 가림 확인이며 기존 다른 앱의 간격 게이트와 혼용하지 않는다.
- 결과 하단에 보이는 개별 상세 이미지 일부는 스크롤 viewport 경계에서 잘려 보인다. 주 결과 overview 카드 crop과 구분했다. overview의 1장/3장은 전체 카드 모습이 보인다.
- 저장 CTA는 배치/노출만 확인했고 저장·기록 영속성을 이번 실행에서 PASS로 주장하지 않는다.

## 인계와 한계
- 최초 lease 시작시각22:05는 실제 시각과 달라 조회/해시 확인 후 정정 요청했다. 조정팀이21:50:36 시작의 `lease-20260911T215036KST-qa-emulator-5554-fda7a483`로 정정한 뒤 UI 조작했다. session.json은 최초 read-only 조회 이력을 보존하며 actions.jsonl에 정정 내역이 있다.
- 시스템 dark(yes), size/density/fontScale 원래 값 복원 검증: closure-metadata.json. 임시 기기 QA jar 삭제, 앱 데이터 삭제/초기화 없음.
- QA 명령 종료. 공유 에뮬레이터는 유지하고 OPS에 lease 해제를 요청한다. 확정 여부는 registry 회신으로 구분한다.
- 제품 코드 수정/재빌드/stage/commit/push 없음. QA 도구와 보고서만 작성했다.
- 전체 기기·스프레드·폰트 크기, 20회 누적, 사주 계산 경계값 전수, 기록/마이그레이션/스토어/서명 검증은 이번 범위 밖이다.
- `verdict.json`은 실제 XML/행동 로그/해시에서 재검산한 기계 판독 결과다. 다른 빌드에 이 PASS를 전용하지 않는다.
