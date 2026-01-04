# 🎮 큐트너 레이징 (Cutener Raising)

다마고치 스타일의 육성 시뮬레이션 게임입니다. 귀여운 캐릭터를 키우고, 블루투스를 통해 가까운 친구와 대결하세요!

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)

## ✨ 주요 기능

### 🐣 육성 시스템
- **3종의 초기 캐릭터**: 불꽃이🔥, 물방울💧, 새싹이🌿
- **5단계 성장**: 유아기 → 성장기 → 성숙기 → 완숙기 → 절정기
- **분기 진화**: 플레이 스타일에 따라 다양한 진화 경로
- **실시간 상태 변화**: 앱을 끄고 있어도 시간이 흐릅니다

### 🎯 케어 액션
- 🍔 **밥 주기** - 배고픔 해소
- 🎮 **놀아주기** - 행복도 증가
- 🧹 **청소하기** - 청결도 유지
- 💤 **재우기** - 피로도 회복
- 💪🛡️⚡ **훈련하기** - 전투 스탯 강화
- 💊 **치료하기** - 아픔 상태 치료

### ⚔️ 블루투스 대결
- **Nearby Connections API** 사용
- 가까운 친구와 **실시간 P2P 대결**
- 스탯 기반 **자동 배틀 시스템**
- 디버그 APK에서도 **테스트 가능**

### 🎨 도트 그래픽
- **레트로 감성**의 픽셀 아트 캐릭터
- **Compose Canvas**로 실시간 렌더링
- 상태별 **다양한 애니메이션**

## 🛠 기술 스택

| 분야 | 기술 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 아키텍처 | MVVM + Clean Architecture |
| DI | Hilt |
| 로컬 DB | Room |
| 백그라운드 | WorkManager |
| 블루투스 | Nearby Connections API |
| 그래픽 | Compose Canvas |

## 📁 프로젝트 구조

```
CutenerRaising/
├── app/src/main/java/com/cutener/raising/
│   ├── bluetooth/          # Nearby Connections 블루투스 연결
│   ├── data/
│   │   ├── database/       # Room DB (DAO, Converters)
│   │   ├── model/          # 데이터 모델 (Pet, Battle, Evolution)
│   │   └── repository/     # Repository 패턴
│   ├── di/                 # Hilt 의존성 주입
│   ├── game/
│   │   ├── engine/         # 게임 엔진 (WorkManager, 시간 경과)
│   │   └── sprite/         # 도트 스프라이트 시스템
│   ├── navigation/         # Compose Navigation
│   ├── ui/
│   │   ├── screens/        # 화면 Composables
│   │   └── theme/          # Material 3 테마
│   └── viewmodel/          # ViewModels
```

## 🚀 빌드 방법

### 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17
- Android SDK 34

### 빌드

```bash
cd CutenerRaising
./gradlew assembleDebug
```

APK 위치: `app/build/outputs/apk/debug/app-debug.apk`

## 📱 설치 및 테스트

### 디버그 APK 설치
1. GitHub Actions에서 `cutener-debug` 아티팩트 다운로드
2. 안드로이드 기기에 APK 설치
3. 설치 시 "알 수 없는 앱" 허용 필요

### 블루투스 대결 테스트
1. 두 대의 안드로이드 기기에 APK 설치
2. 양쪽 모두 블루투스 및 위치 권한 허용
3. 동시에 "블루투스 대결" 버튼 클릭
4. 자동으로 매칭되어 대결 시작

## 🎮 게임 플레이 가이드

### 시작하기
1. 앱 실행 후 3종 캐릭터 중 하나 선택
2. 이름을 지어주고 "시작하기" 클릭
3. 귀여운 파트너와 함께 모험 시작!

### 케어 팁
- 🕐 **정기적인 케어**: 30분~1시간마다 확인 권장
- 🍖 **배고픔 관리**: 80% 이상이면 체력 감소
- 😊 **행복도 유지**: 자주 놀아주세요
- 💪 **훈련 균형**: 한 스탯만 올리지 말고 균형있게
- 😴 **충분한 수면**: 피로도가 높으면 재워주세요

### 진화 가이드
| 진화 경로 | 조건 |
|-----------|------|
| 행복 진화 | 놀아주기 50회 이상 |
| 강인 진화 | 훈련 30회 이상 |
| 현명 진화 | 균형 잡힌 케어 |
| 방치 진화 | 방치 10회 이상 (주의!) |

## 📋 로드맵

### v1.0 (현재)
- [x] 기본 육성 시스템
- [x] 3종 초기 캐릭터
- [x] 5단계 성장 시스템
- [x] 도트 스프라이트 렌더링
- [x] 블루투스 대결 (데모)
- [x] 시간 경과 시스템

### v1.1 (예정)
- [ ] 완성된 블루투스 실시간 대결
- [ ] 더 많은 캐릭터 진화 형태
- [ ] 미니게임 추가
- [ ] 알림 시스템 개선

### v2.0 (예정)
- [ ] 새로운 캐릭터 종류 추가
- [ ] 아이템 시스템
- [ ] 업적 시스템
- [ ] 수익화 (인앱 구매, 광고)

## 🤝 기여

버그 리포트, 기능 제안, PR 모두 환영합니다!

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

---

**Made with ❤️ for 다마고치 fans**
