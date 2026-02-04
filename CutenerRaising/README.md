# 🎮 Pocket Arena (큐트너 레이징)

**다마고찌/디지몬 스타일 육성 시뮬레이션 게임**

Android 네이티브 앱으로 제작된 캐릭터 육성 및 배틀 게임입니다.

## ✨ 디자인 특징

### Neo-Brutalism UI 스타일
- **Bold Borders**: 검은색 굵은 테두리 (3-4dp)
- **Hard Shadows**: 딱딱한 오프셋 그림자 (blur 없음)
- **Vivid Colors**: 쨍한 노랑(#FFE500), 핫핑크(#FF2D78), 민트(#00D9C0)
- **High Contrast**: 높은 대비의 색상 조합

### 카툰 스타일 픽셀아트 캐릭터
- 24x24 픽셀 그리드의 상세한 캐릭터 디자인
- 다마고찌/디지몬 디바이스 스타일 영감
- 6종류의 독특한 캐릭터 클래스:
  - ⚔️ **Warrior** - 파란색 전사
  - 🔮 **Mage** - 보라색 마법사
  - ✨ **Paladin** - 황금빛 성기사
  - 🌑 **Dark Knight** - 어둠의 기사
  - 🗡️ **Rogue** - 녹색 로그
  - 🏹 **Archer** - 갈색 궁수

### 풍부한 애니메이션
- Idle 바운스 애니메이션
- 눈 깜빡임 효과
- 공격/피격 애니메이션
- 수면/식사/훈련 상태 효과
- 승리/패배 이펙트

## 🎯 게임 기능

### 캐릭터 육성
- **Train** 🏋️: 능력치 향상
- **Feed** 🍖: HP 회복
- **Rest** 😴: 에너지 회복

### 스탯 시스템
- ❤️ HP (체력)
- ⚡ Energy (에너지)
- ⭐ EXP (경험치)
- 💪 STR (힘)
- 🧠 INT (지능)
- 🎯 DEX (민첩)

### 배틀 시스템
- Bluetooth 기반 P2P 대전
- 실시간 배틀 애니메이션
- 데미지/크리티컬/미스 시스템

## 🛠 기술 스택

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room Database
- **DI**: Hilt
- **Background**: WorkManager
- **P2P**: Bluetooth Classic

## 📦 빌드

```bash
./gradlew assembleDebug
```

## 📱 실행

디버그 APK: `app/build/outputs/apk/debug/app-debug.apk`

## 🎨 디자인 시스템

### 커스텀 컴포넌트
- `NeoBrutalistButton` - 굵은 테두리 + 하드 섀도우 버튼
- `NeoBrutalistCard` - 비비드 배경 카드
- `NeoBrutalistProgressBar` - 볼드한 프로그레스바
- `NeoBrutalistTextField` - 스타일리시한 입력 필드
- `NeoBrutalistStatBar` - 스탯 표시 바
- `NeoBrutalistBadge` - 레벨/상태 뱃지
- `CharacterRenderer` - 픽셀아트 캐릭터 렌더러

### 컬러 팔레트
```kotlin
VividYellow = #FFE500  // 메인 강조색
HotPink = #FF2D78      // 액션/HP
ElectricBlue = #00BFFF // 정보/훈련
LimeGreen = #BFFF00    // 성공/시작
MintGreen = #00D9C0    // 보조 강조
BrightOrange = #FF6B35 // 경고/피드
VividPurple = #9B5DE5  // 휴식/마법
```

## 📄 라이선스

MIT License
