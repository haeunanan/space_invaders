# 🚀 스페이스 인베이더 (Space Invaders) 코드 개선 프로젝트

## 📝 프로젝트 설명
이 프로젝트는 고전 게임 '스페이스 인베이더'의 기본 소스 코드에 Firebase를 이용한 로그인 기능을 추가하여 개선한 버전입니다.

## ✨ 주요 기능
- Firebase Authentication을 이용한 사용자 로그인 기능
- (여기에 추가로 구현한 다른 기능이 있다면 작성)

## ✨ 아키텍처 (구조)
- **상태 관리**: `Game.java`의 `GameState` enum을 통해 게임의 상태(메인 메뉴, 로그인, 게임 중 등)를 관리합니다. `Game.java`의 `changeState()` 메소드를 호출하여 화면을 전환할 수 있습니다.
- **UI 패널**: 각 화면은 `StartMenuPanel`, `LoginPanel` 등 별도의 `JPanel`을 상속받는 클래스로 구현합니다. 이를 통해 화면별 UI 코드와 로직을 분리하여 관리합니다.

## 🛠️ 사용 기술
- **Language**: Java, Java Swing (for UI)
- **Database**: Firebase Authentication
- **Build Tool**: Maven

## ⚙️ 실행 방법
1.  프로젝트를 IntelliJ에서 엽니다.
2.  `src/main/resources/` 경로에 `space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json` 파일이 있는지 확인합니다. (제출된 .zip 파일에는 포함되어 있습니다.)
3.  `Game.java` 파일의 `main` 메소드를 실행합니다.