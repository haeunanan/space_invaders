# 🚀 스페이스 인베이더 (Space Invaders) 코드 개선 프로젝트

## 📝 프로젝트 설명
이 프로젝트는 소스코드분석 과제 게임 '스페이스 인베이더'의 기본 소스 코드에 **실시간 1:1 PVP**를 포함하여, 다단계 스테이지, 보스전, 랭킹, 로그인, 인게임 상점 등의 기능을 더한 전체적인 아키텍처를 개선한 버전입니다.

---

## ✨ 주요 기능

### 공통 시스템
- **다중 화면 지원**: `CardLayout`을 활용하여 메인 메뉴, 로그인, 모드 선택, 마이페이지, 게임 플레이 등 모든 화면을 유연하게 전환합니다.
- **Firebase 기반 인증/데이터 관리**:
    - **Authentication**: 안전하고 빠른 회원가입 및 로그인 기능.
    - **Firestore**: 사용자별 닉네임 등 개인 데이터 저장.
    - **Realtime Database**: 실시간 PVP 데이터 동기화.
- **마이페이지**: 게임 내에서 닉네임을 확인하고 실시간으로 수정할 수 있습니다.

### 혼자하기 (Single Player)
- **다단계 스테이지**: 난이도가 점진적으로 상승하는 여러 스테이지를 제공합니다.
- **보스전**: 마지막 스테이지에 도달하면 고유한 패턴을 가진 강력한 보스가 등장합니다.
- **점수 및 랭킹**: 플레이어의 점수를 기록하고, 게임 오버 시 상위 랭킹을 로컬 파일(`ranking.dat`)에 저장하고 보여줍니다.
- **인게임 상점**: 외계인을 처치하여 얻은 코인으로 **공격 속도, 이동 속도, 미사일 개수**를 영구적으로 강화할 수 있습니다.

### 대결하기 (Player vs Player)
- **실시간 1:1 대전**: 두 명의 플레이어가 실시간으로 서로의 움직임을 보며 1:1 대전을 펼칩니다.
- **매치메이킹**: Firebase Realtime Database 기반의 대기열 시스템을 통해 다른 플레이어와 자동으로 매칭됩니다.
- **PVP 전용 시스템**:
    - **체력 시스템**: 여러 번 맞아야 죽는 체력(하트) 시스템을 도입하여 전략적인 플레이가 가능합니다.
    - **데이터 동기화**: 위치, 총알 발사, 데미지 등 모든 게임 상황이 실시간으로 동기화됩니다.

---

## 🛠️ 사용 기술 (Tech Stack)

- **Language**: Java (OpenJDK 11+)
- **UI Framework**: Java Swing
- **Database & Auth**: Firebase (Authentication, Firestore, Realtime Database)
- **Networking Library**: OkHttp
- **Build Tool**: Maven

---

## ⚙️ 실행 방법
1.  **프로젝트 복제 (Clone)**
2.  **프로젝트 열기**: IntelliJ IDEA에서 `pom.xml` 파일을 열어 Maven 프로젝트로 Import 합니다.
3.  **Firebase 설정**: `src/main/resources/` 경로에 `space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json` 파일이 있는지 확인합니다. (제출된 .zip 파일에는 포함되어 있습니다.)
4.  **게임 실행**: `org.newdawn.spaceinvaders.Game.java` 파일의 `main` 메소드를 실행합니다.
5.  **PVP 테스트**: 'Allow multiple instances' 설정을 켠 뒤, 프로그램을 두 번 실행하고 각각 다른 계정으로 로그인하여 테스트합니다.