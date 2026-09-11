# 샤로먕 Android

HyperOS Design Standard 긴급 수정과 사주/타로 입력 및 결과 UX를 반영한 Android 소스입니다.

## APK

- 파일: `releases/2026-09-11/sharomyang-hyperos-final-debug.apk`
- SHA-256: `89d40ac05d74988c0d6e348b60199e91bd57b8e4800c3c6a81fc94d752ef2a7c`
- 패키지: `com.hoscat.mtj.dev`
- 버전: `0.1-dev` (`versionCode 1`)

## 검증

- 클린 빌드 및 단위 테스트 92건 통과
- Pixel 10 AVD, Android API 37, 1080x2424, fontScale 1.0
- 설치된 base APK와 배포 APK SHA-256 일치
- 상세 결과와 캡처: `evidence/`

## 구조

- `android/`: Android 애플리케이션
- `saju/`: 사주 계산/계약 소스
- `tarot/`: 타로 계산/계약 소스
- `records/`: 기록 저장 계약 소스
- `design/native/`: 공용 Compose 디자인 컴포넌트

`ANDROID_USER_HOME`에 디버그 키스토어가 준비된 환경에서 Android Gradle 빌드를 실행합니다.
