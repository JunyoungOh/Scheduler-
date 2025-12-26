# 일정관리 앱 (Schedule App)

Kotlin + Jetpack Compose 기반의 현대적인 안드로이드 일정관리 앱입니다.

## 주요 기능

### 📋 List (일정 목록)
- 시간순 또는 우선순위순 정렬
- 일정 추가, 수정, 삭제
- 우선순위 설정 (높음/보통/낮음)
- 완료 체크 기능
- 알람 설정

### 📅 Calendar (캘린더)
- 월간/주간/일간 뷰 전환
- 날짜별 일정, 노트, 사진 통합 보기
- 날짜 선택으로 빠른 일정 확인
- Calendar와 Note, Photo 연동

### 📝 Note (노트)
- 자유로운 노트 작성
- 노트에 날짜 연동 (캘린더에서 확인 가능)
- 노트 검색 기능
- 고정(Pin) 기능

### 📸 Photo (사진)
- 사진과 날짜 연계 기록
- 날짜별 사진 그룹화
- 메모 추가 기능
- 캘린더 연동

### ⚙️ Setting (설정)
- 비밀번호 잠금 기능
- 데이터 백업 (JSON 내보내기)
- 데이터 복원 (JSON 가져오기)

## 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **아키텍처**: MVVM + Clean Architecture
- **의존성 주입**: Hilt
- **데이터베이스**: Room
- **비동기 처리**: Kotlin Coroutines + Flow
- **이미지 로딩**: Coil
- **네비게이션**: Navigation Compose

## 디자인 시스템

K-App 스타일의 미니멀하고 세련된 디자인을 적용했습니다.

### Typography
- **폰트**: Pretendard
- **제목**: 18-22px (Bold/SemiBold)
- **본문**: 15-16px (Medium/Regular)
- **캡션**: 12-13px (Regular)

### Color Palette
- **Primary**: #3182F6 (Toss Blue)
- **Background**: #FFFFFF / #F2F4F6
- **Text**: #191F28 (Primary) / #4E5968 (Secondary)
- **Status**: Error #F04452, Success #00D082

### Spacing
- 8의 배수 시스템 (8dp, 16dp, 24dp, 32dp)
- Border-radius: 12px (버튼), 16-20px (카드)

## 프로젝트 구조

```
app/
├── src/main/java/com/scheduleapp/
│   ├── data/
│   │   ├── database/      # Room DB, DAO, Converters
│   │   ├── model/         # Data classes (Schedule, Note, Photo)
│   │   └── repository/    # Repository implementations
│   ├── di/                # Hilt DI modules
│   ├── navigation/        # Navigation setup
│   ├── ui/
│   │   ├── components/    # Reusable UI components
│   │   ├── screens/       # Screen composables
│   │   └── theme/         # Theme, Colors, Typography
│   ├── viewmodel/         # ViewModels
│   ├── MainActivity.kt
│   └── ScheduleApplication.kt
└── src/main/res/
    ├── font/              # Pretendard fonts
    ├── values/            # Colors, Strings, Themes
    └── xml/               # Backup rules, File paths
```

## 빌드 및 실행

### 요구 사항
- Android Studio Hedgehog | 2023.1.1 이상
- JDK 17
- Android SDK 34

### 폰트 설정
1. [Pretendard 폰트](https://github.com/orioncactus/pretendard)를 다운로드합니다.
2. 다음 파일들을 `app/src/main/res/font/` 폴더에 복사합니다:
   - `pretendard_regular.ttf`
   - `pretendard_medium.ttf`
   - `pretendard_semibold.ttf`
   - `pretendard_bold.ttf`

### 빌드
```bash
./gradlew assembleDebug
```

### 실행
Android Studio에서 프로젝트를 열고 Run 버튼을 클릭하거나:
```bash
./gradlew installDebug
```

## 라이선스

이 프로젝트는 개인/상업적 용도로 자유롭게 사용할 수 있습니다.

## 기여

버그 리포트, 기능 제안, PR을 환영합니다!
