# 🚀 스페이스 인베이더 (Space Invaders) 코드 개선 프로젝트

## 📝 프로젝트 설명
이 프로젝트는 소스코드분석 과제 게임 '스페이스 인베이더'의 기본 소스 코드에 Firebase를 이용한 로그인/회원가입 기능을 추가하고, 향후 상점, 랭킹 등 추가 기능 확장을 용이하게 하기 위해 전체적인 아키텍처를 개선한 버전입니다.

## ✨ 아키텍처 (구조)
- **상태 관리**: `Game.java`의 `GameState` enum을 통해 게임의 상태(메인 메뉴, 로그인, 게임 중 등)를 관리합니다. `Game.java`의 `changeState()` 메소드를 호출하여 화면을 전환할 수 있습니다.
- **UI 패널 시스템**: 화면 전환을 위해 `CardLayout`을 사용합니다. 각 화면은 `StartMenuPanel`, `SignInPanel` 등 별도의 `JPanel`을 상속받는 클래스로 구현합니다. 이를 통해 화면별 UI 코드와 로직을 명확하게 분리하여 관리합니다.
- **멀티 스레딩**: UI 반응성을 유지하기 위해 UI 스레드와 게임 로직을 처리하는 별도의 게임루프 스레드를 분리하여 실행합니다.

## 🛠️ 사용 기술
- **Language**: Java, Java Swing (for UI)
- **Authentication**: Firebase Authentication (via REST API)
- **Build Tool**: Maven

## ⚙️ 실행 방법
1.  프로젝트를 IntelliJ IDEA에서 엽니다.
2.  `src/main/resources/` 경로에 `space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json` 파일이 있는지 확인합니다. (제출된 .zip 파일에는 포함되어 있습니다.)
3.  `pom.xml`에 명시된 의존성 라이브러리들이 모두 설치되어있는지 확인합니다.
4.  `Game.java` 파일의 `main` 메소드를 실행합니다.