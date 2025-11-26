package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import org.newdawn.spaceinvaders.stage.NeptuneStage;
import org.newdawn.spaceinvaders.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 *
 * @author Kevin Glass
 */

public class Game
{
	public enum GameState {
		START_MENU, // 시작 메뉴
		SIGN_IN,    // 로그인 화면
		SIGN_UP,    // 회원가입 화면
		PVP_MENU,   // PvP 모드 선택 화면
		PVP_LOBBY,  // 매치메이킹 대기 화면
		PLAYING_SINGLE,    // 기존 혼자하기 모드
		PLAYING_PVP, // PvP 게임 플레이 중
		MY_PAGE,
		COOP_LOBBY,
		PLAYING_COOP
	}
	private GameState currentState;

	private StartMenuPanel startMenuPanel;
	private SignInPanel signInPanel;
	private SignUpPanel signUpPanel;
	private GamePlayPanel gamePlayPanel;
	private PvpMenuPanel pvpMenuPanel;
	private PvpLobbyPanel pvpLobbyPanel;
	private MyPagePanel myPagePanel;
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
    public PlayerStats playerStats = new PlayerStats();
	/** The entity representing the player */
	private Entity ship;
	private Entity opponentShip;
	/** The speed at which the player's ship should move (pixels/sec) */
	private double moveSpeed = 300;
	/** The time at which last fired a shot */
    public long lastFire = 0;
	/** The interval between our players shot (ms) */
    public long firingInterval = 500;
	/** The number of aliens left on the screen */
	private int alienCount;
	// shop 브랜치에서 복사해올 변수들
	public boolean shopOpen = false;
	public final int UPGRADE_COST = 10;
	public final int MAX_UPGRADES = 6;
	public RankingManager rankingManager;
	public int currentLevel = 1;
	public static final int BOSS_LEVEL = 5;
	private Thread matchmakingThread;
	private String currentMatchId;
	private volatile String player1_uid;
	private volatile String player2_uid;
	private Thread networkThread;
	private volatile Map<String, Object> lastOpponentState;

	/** The message to display which waiting for a key press */
	private String message = "";
	/** True if we're holding up game play until a key has been pressed */
	private boolean waitingForKeyPress = true;
	/** True if game logic needs to be applied this loop, normally as a result of a game event */
	private boolean logicRequiredThisLoop = false;
	/** The last time at which we recorded the frame rate */
	private long lastFpsTime;
	/** The current number of frames recorded */
	private int fps;
	/** The normal title of the game window */
	private String windowTitle = "Space Invaders 102";
	private JFrame container;
	private CardLayout cardLayout;
	private JPanel mainPanel;
	// ===== Stage System (Missing fields added) =====
	private int stageIndex = 1;
	private Stage currentStage;
	private final int MAX_STAGE = 6;
	private boolean transitionRequested = false; // 다음 스테이지 전환 요청 플래그
	private long slowTimer = 0;
	public boolean reverseControls = false; // 조작 반전 상태 플래그
    private InputManager inputManager;
    public EntityManager entityManager;
    private static final String CARD_PLAYING_SINGLE = "PLAYING_SINGLE";
    private static final String CARD_PVP_LOBBY = "PVP_LOBBY";
    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
	// ===============================================

	/**
	 * Construct our game and set it running.
	 */
	public GameState getCurrentState() {
		return currentState;
	}
	public Entity getShip() {
		return this.ship;
	}
	public Entity getOpponentShip() {
		return this.opponentShip;
	}
	public JFrame getContainer() {
		return this.container;
	}
	public boolean amIPlayer1() {
		// 현재 로그인한 사용자의 UID를 가져옵니다.
		String myUid = CurrentUserManager.getInstance().getUid();

        // player1_uid나 myUid가 아직 설정되지 않은 경우(null)를 대비한 안전장치입니다.
        // 두 값 모두 유효할 때만 비교를 수행합니다.
        if (myUid != null && player1_uid != null) {
            return myUid.equals(player1_uid);
        }

        // 정보가 불완전하면 기본값으로 false를 반환하여 오류를 방지합니다.
        return false;
    }

    public void resetSinglePlayerState() {
        this.currentLevel = 1;
        playerStats.resetScore();
    }

    public Stage getCurrentStage() {
        return currentStage;
    }


    public Game() {
        currentState = GameState.START_MENU;

        FirebaseInitializer firebase = new FirebaseInitializer();
        firebase.initialize();

        // create a frame to contain our game
        container = new JFrame("Space Invaders 102");

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        entityManager = new EntityManager(this);

        startMenuPanel = new StartMenuPanel(this);
        signInPanel = new SignInPanel(this);
        signUpPanel = new SignUpPanel(this);
        gamePlayPanel = new GamePlayPanel(this);
        pvpMenuPanel = new PvpMenuPanel(this);
        pvpLobbyPanel = new PvpLobbyPanel(this);
        myPagePanel = new MyPagePanel(this);
        // dev 브랜치의 Game() 생성자 안에 추가
        rankingManager = new RankingManager();
        playerStats.resetScore();

        mainPanel.add(startMenuPanel, "START");
        mainPanel.add(signInPanel, "SIGN_IN");
        mainPanel.add(signUpPanel, "SIGN_UP");
        mainPanel.add(gamePlayPanel, CARD_PLAYING_SINGLE);
        mainPanel.add(pvpMenuPanel, "PVP_MENU");
        mainPanel.add(pvpLobbyPanel, CARD_PVP_LOBBY);
        mainPanel.add(myPagePanel, "MY_PAGE");

        container.getContentPane().add(mainPanel);

        changeState(GameState.START_MENU);

        inputManager = new InputManager(this); // 객체 생성
        gamePlayPanel.addKeyListener(inputManager); // 리스너 등록
        gamePlayPanel.addMouseListener(new MouseAdapter() {
            private void handleGameMouseClick(int mx, int my) {
                // '메뉴로 돌아가기' 버튼 처리
                if (isWaitingForKeyPress() && getCurrentState() == GameState.PLAYING_SINGLE) {
                    if (mx >= 325 && mx <= 475 && my >= 550 && my <= 590) {
                        showReturnToMenuDialog();
                        return;
                    }
                }

                // 상점 버튼 처리
                if (mx >= 720 && mx <= 780 && my >= 10 && my <= 40) {
                    shopOpen = !shopOpen;
                    return;
                }

                // 상점 내부 아이템 구매 처리
                if (shopOpen) {
                    handleShopPurchase(mx, my);
                }
            }
        });

        gamePlayPanel.setFocusable(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setLocationRelativeTo(null);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                gameRunning = false;
                System.exit(0);
            }
        });

        Thread gameThread = new Thread(()-> {
            this.gameLoop();
        });
        gameThread.start();
    }

    private void showReturnToMenuDialog() {
        int choice = JOptionPane.showConfirmDialog(
                container,
                "진행 상황이 저장되지 않습니다. 메뉴로 돌아가시겠습니까?\n(레벨 1부터 다시 시작합니다)",
                "메뉴로 돌아가기",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetSinglePlayerState(); // 상태 초기화
            changeState(GameState.PVP_MENU); // 메뉴로 이동
        }
        // '아니오'를 누르면 아무것도 하지 않음
    }

    private void handleShopPurchase(int mx, int my) {
        // 상점 패널 위치 및 크기 계산 (하드코딩된 값들)
        int overlayX = 40;
        int overlayY = 40;
        int overlayW = 720;
        int overlayH = 520;
        int pad = 20;
        int panelW = (overlayW - pad * 4) / 3; // 패널 3개 나란히 배치
        int panelH = overlayH - 120;
        int panelY = overlayY + 60;

        // 3개의 아이템 슬롯을 반복문으로 체크
        for (int i = 0; i < 3; i++) {
            int px = overlayX + pad + i * (panelW + pad);
            int py = panelY;

            // 마우스 클릭이 i번째 아이템 패널 안에 있는지 확인
            if (mx >= px && mx <= px + panelW && my >= py && my <= py + panelH) {
                if (i == 0) purchaseAttackSpeed();
                if (i == 1) purchaseMoveSpeed();
                if (i == 2) purchaseMissileCount();
            }
        }
    }

    public void changeState(GameState newState) {
        currentState = newState;

        if (matchmakingThread != null && matchmakingThread.isAlive()) {
            matchmakingThread.interrupt();
        }
        if (networkThread != null && networkThread.isAlive()) {
            networkThread.interrupt();
        }

		switch (currentState) {
			case START_MENU:
				cardLayout.show(mainPanel, "START");
				break;
			case SIGN_IN:
				cardLayout.show(mainPanel, "SIGN_IN");
				break;
			case SIGN_UP:
				cardLayout.show(mainPanel, "SIGN_UP");
				break;
			case PVP_MENU:
				cardLayout.show(mainPanel, "PVP_MENU");
				break;
			case PVP_LOBBY:
				cardLayout.show(mainPanel, CARD_PVP_LOBBY);
				startMatchmakingLoop();
				break;
			case PLAYING_SINGLE:
                cardLayout.show(mainPanel, CARD_PLAYING_SINGLE);
				gamePlayPanel.requestFocusInWindow();
				startGame();
				break;
			case PLAYING_PVP:
				waitingForKeyPress = false;
                cardLayout.show(mainPanel, CARD_PLAYING_SINGLE);
				SwingUtilities.invokeLater(() -> {
					gamePlayPanel.requestFocusInWindow();
				});
				startPvpGame();
				break;
			case MY_PAGE:
				cardLayout.show(mainPanel, "MY_PAGE");
				myPagePanel.updateUser();
				break;
			case COOP_LOBBY:
				cardLayout.show(mainPanel, CARD_PVP_LOBBY); // UI는 PVP 로비 재사용 (텍스트만 바꾸면 됨)
				startCoopMatchmakingLoop(); // 협동 매칭 루프 시작
				break;
			case PLAYING_COOP: // 협동 게임 시작
                cardLayout.show(mainPanel, CARD_PLAYING_SINGLE);
				SwingUtilities.invokeLater(() -> gamePlayPanel.requestFocusInWindow());
				startCoopGame(); // 협동 게임 초기화
				break;
		}
	}

	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame() {
		// 모든 엔티티 초기화
        entityManager.clear();

		slowTimer = 0;
		reverseControls = false;

		// 스테이지 기본값 초기화
		stageIndex = 1;
        // 팩토리를 통해 스테이지 생성
        currentStage = StageFactory.createStage(this, stageIndex);

		// 플레이어 배 초기화 (스테이지보다 먼저 생성)
		// initPlayer() 로직을 직접 포함:
        ship = new ShipEntity(this, Constants.SHIP_SPRITE, Constants.PLAYER_START_X, Constants.PLAYER_START_Y);
		((ShipEntity) ship).setHealth(3);
		this.slowTimer = 0;
        entityManager.addEntity(ship);

		// 스테이지 적/보스 생성
		if (currentStage != null) {
			currentStage.init();
		}
		alienCount = 0; // 스테이지 내에서 카운트 갱신되므로 0으로 초기화

        inputManager.reset();
		waitingForKeyPress = false;
    }

	public void nextStage() {
		waitingForKeyPress = false;

		// [중요] 엔티티 리스트뿐만 아니라 '삭제 대기 목록'도 반드시 비워야 합니다.
        entityManager.clear();

		// [수정] 스테이지 전환/재시작 시 조작 반전 및 키 입력 상태 초기화
		reverseControls = false;
        inputManager.reset();

		// 스테이지 제한 확인 (예: 6단계)
		if (stageIndex > MAX_STAGE) {
			message = "ALL STAGES CLEAR! Returning to Menu...";
			waitingForKeyPress = true;
			changeState(GameState.PVP_MENU);
			return;
		}

        // 팩토리를 통해 스테이지 생성
        currentStage = StageFactory.createStage(this, stageIndex);

		// initPlayer() 로직을 직접 포함 (ship만 다시 추가)
		ship = new ShipEntity(this, Constants.SHIP_SPRITE, 370, 550);
		((ShipEntity) ship).setHealth(3); // 체력 복구
		this.slowTimer = 0;
        entityManager.addEntity(ship);

		if (currentStage != null) {
			currentStage.init();
		} else {
			changeState(GameState.PVP_MENU);
		}
	}



    private void startMatchmakingLoop() {
        matchmakingThread = new Thread(() -> {
            FirebaseClientService clientService = new FirebaseClientService();
            String myUid = CurrentUserManager.getInstance().getUid();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("상대방 찾는 중...");
                    String opponentUid = clientService.findOpponent(myUid);

                    boolean matched = false;
                    if (opponentUid != null) {
                        matched = tryProcessHostMatchmaking(clientService, myUid, opponentUid);
                    } else {
                        matched = tryProcessGuestMatchmaking(clientService, myUid);
                    }

                    if (matched) break; // 매칭 성공 시 루프 종료

                    Thread.sleep(2000);

                } catch (InterruptedException e) {
                    // ... 에러 처리
                }
            }
        });
        matchmakingThread.start();
    }

    private boolean tryProcessHostMatchmaking(FirebaseClientService client, String myUid, String opUid) {
        if (myUid.compareTo(opUid) < 0) { // 내가 방장 조건
            String matchId = client.createMatch(myUid, opUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                this.player1_uid = myUid;
                this.player2_uid = opUid;
                client.deleteFromQueue(myUid);
                client.deleteFromQueue(opUid);
                SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_PVP));
                return true;
            }
        }
        return false;
    }

    private boolean tryProcessGuestMatchmaking(FirebaseClientService client, String myUid) {
        if (!client.isUserInQueue(myUid)) { // 큐에서 사라졌다면 (누가 날 데려감)
            String matchId = client.findMyMatch(myUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                Map<String, Object> matchData = client.getMatchData(matchId);
                if (matchData != null) {
                    this.player1_uid = (String) matchData.get("player1");
                    this.player2_uid = (String) matchData.get("player2");
                    SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_PVP));
                    return true;
                }
            }
        }
        return false;
    }

	private void startPvpGame() {
        entityManager.clear();
		waitingForKeyPress = false;
        inputManager.reset();

        // 언제나 '나'는 아래쪽에 생성
        ship = new ShipEntity(this, Constants.SHIP_SPRITE, 370, 550);
        ((ShipEntity) ship).setHealth(3);
        entityManager.addEntity(ship);

        // 언제나 '상대방'은 위쪽에 생성
        opponentShip = new ShipEntity(this, "sprites/opponent_ship.gif", 370, 50);
        ((ShipEntity) opponentShip).setHealth(3);
        entityManager.addEntity(opponentShip);

        startNetworkLoop();
    }

    private void startNetworkLoop() {
        networkThread = new Thread(() -> {
            // 매번 객체를 생성하지 않도록 루프 밖으로 뺄 수도 있지만, 로직 유지를 위해 내부에 둠
            FirebaseClientService clientService = new FirebaseClientService();

            while (isPlayingState() && !Thread.currentThread().isInterrupted()) {
                try {
                    // 1. 내 상태 전송
                    sendMyStatus(clientService);

                    // 2. 상대 상태 수신 및 업데이트
                    updateOpponentStatus(clientService);

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        networkThread.start();
    }

    private void sendMyStatus(FirebaseClientService clientService) {
        String myUid = org.newdawn.spaceinvaders.CurrentUserManager.getInstance().getUid();
        Map<String, Object> myState = new HashMap<>();
        myState.put("x", ship.getX());
        myState.put("y", ship.getY());
        myState.put(KEY_HEALTH, ((ShipEntity) ship).getCurrentHealth());

        java.util.List<Map<String, Integer>> myShotsData = new ArrayList<>();
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof ShotEntity) {
                ShotEntity shot = (ShotEntity) entity;
                if (myUid.equals(shot.getOwnerUid())) {
                    Map<String, Integer> shotData = new HashMap<>();
                    shotData.put("x", shot.getX());
                    shotData.put("y", shot.getY());
                    myShotsData.add(shotData);
                }
            }
        }
        myState.put(KEY_SHOTS, myShotsData);

        String myPlayerNode = amIPlayer1() ? "player1_state" : "player2_state";
        clientService.updatePlayerState(currentMatchId, myPlayerNode, myState);
    }

    private void updateOpponentStatus(FirebaseClientService clientService) {
        String opponentNode = amIPlayer1() ? "player2_state" : "player1_state";
        Map<String, Object> opponentState = clientService.getOpponentState(currentMatchId, opponentNode);

        if (opponentState != null) {
            // 위치 동기화
            if (opponentState.get("x") instanceof Number && opponentState.get("y") instanceof Number) {
                double opX = ((Number) opponentState.get("x")).doubleValue();
                double opY = ((Number) opponentState.get("y")).doubleValue();
                opponentShip.setLocation((int) opX, (int) opY);
            }

            // 체력 동기화
            if (opponentState.get(KEY_HEALTH) instanceof Number) {
                int opHealth = ((Number) opponentState.get(KEY_HEALTH)).intValue();
                ((ShipEntity)opponentShip).setCurrentHealth(opHealth);
                if (opHealth <= 0) {
                    SwingUtilities.invokeLater(this::notifyWinPVP);
                    // 루프 종료를 위해 인터럽트 발생 (선택 사항)
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // 총알 동기화
            updateOpponentShots(opponentState);
        }
    }

    private void updateOpponentShots(Map<String, Object> opponentState) {
        String opponentUid = amIPlayer1() ? player2_uid : player1_uid;

        // 기존 총알 제거
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof ShotEntity && opponentUid.equals(((ShotEntity) entity).getOwnerUid())) {
                removeEntity(entity);
            }
        }

        // 새 총알 생성
        if (opponentState.get(KEY_SHOTS) instanceof java.util.List) {
            java.util.List<Map<String, Double>> shotList = (java.util.List<Map<String, Double>>) opponentState.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", sData.get("x").intValue(), sData.get("y").intValue(), 0, -300);
                shot.setOwnerUid(opponentUid);
                addEntity(shot);
            }
        }
    }

	private void startCoopGame() {
        entityManager.clear();
        inputManager.reset();

		// 내 우주선 (아래쪽)
		ship = new ShipEntity(this, Constants.SHIP_SPRITE, 300, 550); // X좌표를 약간 왼쪽으로
		((ShipEntity) ship).setHealth(3);
        entityManager.addEntity(ship);

		// 상대방 우주선 (같은 편! 아래쪽)
		opponentShip = new ShipEntity(this, Constants.SHIP_SPRITE, 500, 550); // X좌표를 약간 오른쪽으로, 이미지는 ship.gif 사용
		((ShipEntity) opponentShip).setHealth(3);
        entityManager.addEntity(opponentShip);

		// 외계인 생성 (싱글 플레이처럼 적들도 생성해야 함!)
		alienCount = 0;
		initStandardStage();
		startNetworkLoop();

		waitingForKeyPress = false;
	}

    private void startCoopMatchmakingLoop() {
        matchmakingThread = new Thread(() -> {
            FirebaseClientService clientService = new FirebaseClientService();
            String myUid = CurrentUserManager.getInstance().getUid();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("협동 상대 찾는 중...");
                    String opponentUid = clientService.findCoopOpponent(myUid);

                    boolean matched = false;

                    if (opponentUid != null) {
                        // 상대방을 찾았을 때 (방장 시도)
                        matched = tryProcessCoopHost(clientService, myUid, opponentUid);
                    } else {
                        // 대기열에 아무도 없을 때 (참가자 시도)
                        matched = tryProcessCoopGuest(clientService, myUid);
                    }

                    if (matched) break; // 매칭 성공 시 루프 종료

                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        matchmakingThread.start();
    }

    private boolean tryProcessCoopHost(FirebaseClientService client, String myUid, String opUid) {
        if (myUid.compareTo(opUid) < 0) {
            String matchId = client.createMatch(myUid, opUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                this.player1_uid = myUid;
                this.player2_uid = opUid;
                client.deleteFromCoopQueue(myUid);
                client.deleteFromCoopQueue(opUid);
                SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_COOP));
                return true;
            }
        }
        return false;
    }

    private boolean tryProcessCoopGuest(FirebaseClientService client, String myUid) {
        if (!client.isUserInCoopQueue(myUid)) { // 큐에서 사라졌다면 (누군가 나를 데려감)
            String matchId = client.findMyMatch(myUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                Map<String, Object> matchData = client.getMatchData(matchId);
                if (matchData != null) {
                    this.player1_uid = (String) matchData.get("player1");
                    this.player2_uid = (String) matchData.get("player2");
                    SwingUtilities.invokeLater(() -> changeState(GameState.PLAYING_COOP));
                    return true;
                }
            }
        }
        return false;
    }

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */

	private void initStandardStage() {
		double moveSpeed = 100;
		int alienRows = 3;
		double firingChance = 0;
		int startY = 50;

        switch (currentLevel) {
            case 1:
                moveSpeed = 100;
                alienRows = 3;
                firingChance = 0;
                break;
            case 2:
                moveSpeed = 130;
                alienRows = 4;
                firingChance = 0.0002;
                break;
            case 3:
                moveSpeed = 160;
                alienRows = 5;
                firingChance = 0.0005;
                break;
            case 4:
                moveSpeed = 200;
                alienRows = 5;
                firingChance = 0.0008;
                startY = 80;
                break;
            default: // [추가] 예외 상황 방지
                moveSpeed = 200;
                alienRows = 5;
                firingChance = 0.001;
                System.err.println("Undefined level: " + currentLevel + ". Using default settings.");
                break;
        }

		// 설정된 값으로 외계인을 생성하고 entities 리스트에 추가합니다.
		// 이 부분이 누락되면 외계인이 나타나지 않습니다.
		for (int row = 0; row < alienRows; row++) {
			for (int x = 0; x < 12; x++) {
				Entity alien = new AlienEntity(this, "sprites/alien.gif", 100 + (x * 50), startY + row * 30, moveSpeed, firingChance);
                entityManager.addEntity(alien);
				alienCount++;
			}
		}
	}

    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }
	
	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}
	
	/**
	 * Remove an entity from the game. The entity removed will
	 * no longer move or be drawn.
	 * 
	 * @param entity The entity that should be removed
	 */
    public void removeEntity(Entity entity) {
        entityManager.removeEntity(entity);
    }
	
	/**
	 * Notification that the player has died. 
	 */
	public void notifyDeath() {
		if (currentState == GameState.PLAYING_PVP) {
			if (waitingForKeyPress) return; // 중복 호출 방지
			message = "You Lose...";
			waitingForKeyPress = true;
			return;
		}

        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;

		// CurrentUserManager를 통해 로그인 상태 확인
		if (CurrentUserManager.getInstance().isLoggedIn()) {
			// 로그인 상태이면, 현재 닉네임을 가져와서 바로 랭킹에 추가
			String nickname = CurrentUserManager.getInstance().getNickname();
			rankingManager.addScore(playerStats.getScore(), nickname);
		} else {
			// 비로그인 상태일 때만 이름을 물어봄 (기존 방식)
			if (rankingManager.isHighScore(playerStats.getScore())) {
				String name = JOptionPane.showInputDialog(container, "New High Score! Enter your name:", "Ranking", JOptionPane.PLAIN_MESSAGE);
				if (name != null && !name.trim().isEmpty()) {
					rankingManager.addScore(playerStats.getScore(), name);
				}
			}
		}

        currentLevel = 1;// 죽으면 레벨 1로 리셋
        playerStats.resetScore();
    }

    /**
     * Notification that the player has won since all the aliens
     * are dead.
     */
    public void notifyWin() {
        playerStats.addScore(100);
        playerStats.addCoins(50);
        currentLevel++;
        if (currentLevel > BOSS_LEVEL) {
            message = "Congratulations! You have defeated the final boss!";
            waitingForKeyPress = true;
            currentLevel = 1;
        } else if (currentLevel == BOSS_LEVEL) {
            message = "Final Stage! The Boss is approaching!";
            waitingForKeyPress = true;
        } else {
            message = "Stage " + (currentLevel - 1) + " Cleared! Prepare for the next stage.";
            waitingForKeyPress = true;
        }
    }
    // ▼▼▼ PVP 승리/패배 처리 메소드 추가 ▼▼▼
    public void notifyWinPVP() {
        if (currentState != GameState.PLAYING_PVP) return; // 중복 호출 방지
        playerStats.addScore(30);
        message = "You win! 30 reward coins";
        waitingForKeyPress = true;
    }

    public void bossKilled() {
        message = "BOSS DEFEATED!";
        waitingForKeyPress = true;
        stageIndex++;
    }

    // 상점 옵션1: 공격 속도 증가
    private void purchaseAttackSpeed() {
        if (playerStats.getAttackLevel() >= MAX_UPGRADES) return;

        // spendCoins가 true를 반환하면(구매 성공하면) 레벨업 진행
        if (playerStats.spendCoins(UPGRADE_COST)) {
            playerStats.increaseAttackLevel();
            // 공격 속도 갱신 (기존 로직 유지)
            firingInterval = Math.max(100, (long)(firingInterval * 0.9));
        }
    }

    // 상점 옵션2: 이동 속도 증가
    private void purchaseMoveSpeed() {
        if (playerStats.getMoveLevel() >= MAX_UPGRADES) return;

        if (playerStats.spendCoins(UPGRADE_COST)) {
            playerStats.increaseMoveLevel();
            // 이동 속도 갱신 (기존 로직 유지)
            moveSpeed = moveSpeed * 1.1;
        }
    }

    // 상점 옵션3: 미사일 개수 증가
    private void purchaseMissileCount() {
        if (playerStats.getMissileLevel() >= MAX_UPGRADES) return;

        if (playerStats.spendCoins(UPGRADE_COST)) {
            playerStats.increaseMissileLevel();
            // 미사일 개수 증가 (PlayerStats의 변수 사용)
            playerStats.setMissileCount(Math.min(5, playerStats.getMissileCount() + 1));
        }
    }

    /**
     * Notification that an alien has been killed
     */
    public void notifyAlienKilled() {
        playerStats.addScore(100);
    }


    /**
     * Attempt to fire a shot from the player. Its called "try"
     * since we must first check that the player can fire at this
     * point, i.e. has he/she waited long enough between shots
     */

	public void applySlow(long duration) {
		this.slowTimer = duration;

	}

    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();

        while (gameRunning) {
            long now = SystemTimer.getTime();
            long delta = now - lastLoopTime;
            lastLoopTime = now;

            // 1. FPS 갱신
            updateFPS(delta);

            // 2. 타이머 갱신 (슬로우 효과 등)
            updateTimers(delta);

            // 3. 스테이지 전환 체크
            if (transitionRequested) {
                nextStage();
                transitionRequested = false;
                continue;
            }

            // 4. 게임 로직 수행 (플레이 중일 때만)
            if (isPlayingState()) {
                // (1) 입력 처리 및 이동
                processPlayerInput();

                // (2) 적 로직 (방향 전환 등)
                processEnemyLogic();

                // (3) 스테이지 업데이트 (기믹)
                updateStage(delta);

                // (4) 네트워크 (PVP)
                updateNetwork();

                // (5) 엔티티 이동
                moveEntities(delta);

                // (6) 충돌 체크
                checkCollisions();

                // (7) 게임 승리 조건 체크 (Coop)
                checkWinCondition();

                // (8) 죽은 엔티티 삭제
                removeDeadEntities();

                // (9) 화면 그리기 요청
                gamePlayPanel.repaint();
            } else {
                // 메뉴 화면 등에서도 repaint는 필요
                mainPanel.repaint();
            }

            SystemTimer.sleep(10);
        }
    }

    // --- 리팩토링으로 추출된 헬퍼 메서드들 ---

    private boolean isPlayingState() {
        return currentState == GameState.PLAYING_SINGLE ||
                currentState == GameState.PLAYING_PVP ||
                currentState == GameState.PLAYING_COOP;
    }

    private void updateFPS(long delta) {
        lastFpsTime += delta;
        fps++;
        if (lastFpsTime >= 1000) {
            container.setTitle(windowTitle + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }
    }

    private void updateTimers(long delta) {
        if (slowTimer > 0) {
            slowTimer -= delta;
        }
    }

    private void processPlayerInput() {
        if (!waitingForKeyPress && ship != null) {
            // 1. 기본 이동 처리
            handleMovement();

            // 2. 특수 효과 (바람 등) 처리
            handleEnvironmentalEffects();

            // 3. 발사 처리
            if (inputManager.isFirePressed() && ship instanceof ShipEntity) {
                ((ShipEntity) ship).tryToFire();
            }
        }
    }

    // [추가] 이동 로직 분리
    private void handleMovement() {
        ship.setHorizontalMovement(0);
        ship.setDY(0);

        double currentSpeed = moveSpeed;
        if (slowTimer > 0) currentSpeed *= 0.5;

        boolean left = reverseControls ? inputManager.isRightPressed() : inputManager.isLeftPressed();
        boolean right = reverseControls ? inputManager.isLeftPressed() : inputManager.isRightPressed();
        boolean up = reverseControls ? inputManager.isDownPressed() : inputManager.isUpPressed();
        boolean down = reverseControls ? inputManager.isUpPressed() : inputManager.isDownPressed();

        if (left && !right) ship.setHorizontalMovement(-currentSpeed);
        else if (right && !left) ship.setHorizontalMovement(currentSpeed);

        if (up && !down) ship.setDY(-currentSpeed);
        else if (down && !up) ship.setDY(currentSpeed);
    }

    // [추가] 환경 효과 분리
    private void handleEnvironmentalEffects() {
        if (currentState == GameState.PLAYING_SINGLE && currentStage instanceof NeptuneStage) {
            NeptuneStage ns = (NeptuneStage) currentStage;
            double wind = ns.getCurrentWindForce();
            if (wind != 0 && !((ShipEntity) ship).isBoosterActive()) {
                ship.setHorizontalMovement(ship.getDX() + wind);
            }
        }
    }

    private void processEnemyLogic() {
        if (logicRequiredThisLoop) {
            entityManager.doLogic(); // 위임
            logicRequiredThisLoop = false;
        }
    }

    private void updateStage(long delta) {
        if (currentState == GameState.PLAYING_SINGLE && !waitingForKeyPress && currentStage != null) {
            currentStage.update(delta);
            if (currentStage.isCompleted()) {
                waitingForKeyPress = true;
                message = "Stage " + stageIndex + " Clear!";
                playerStats.addCoins(50); // 스테이지 클리어 보상 50코인
                playerStats.addScore(100); // 점수 보상
                stageIndex++;
            }
        }
    }

    private void updateNetwork() {
        // PVP/COOP 네트워크 로직은 별도 스레드에서 돌지만,
        // 메인 루프에서 처리해야 할 동기화 로직이 있다면 이곳에 작성
    }

    private void moveEntities(long delta) {
        if (!waitingForKeyPress) {
            entityManager.moveEntities(delta); // 위임
        }
    }

    private void checkCollisions() {
        if (!waitingForKeyPress) {
            entityManager.checkCollisions(); // 위임
        }
    }





    private void checkWinCondition() {
        if (currentState == GameState.PLAYING_COOP) {
            if (currentLevel < BOSS_LEVEL) {
                int aliensRemaining = 0;
                for (Entity e : entityManager.getEntities()) if (e instanceof AlienEntity) aliensRemaining++;
                if (aliensRemaining == 0 && !waitingForKeyPress) {
                    notifyWin();
                }
            }
        }
    }

    private void removeDeadEntities() {
        entityManager.removeDeadEntities(); // 위임
    }

    public Rectangle getVisualBounds(Entity entity) {
        if (entity == null) return new Rectangle(0,0,0,0);
        int drawX = entity.getX();
        int drawY = entity.getY();
        String myUid = CurrentUserManager.getInstance().getUid();

        boolean isOpponentEntity = false;
        if (currentState == GameState.PLAYING_PVP) {
            if (entity == opponentShip ||
                    (entity instanceof ShotEntity && myUid != null && !((ShotEntity)entity).isOwnedBy(myUid))) {
                isOpponentEntity = true;
            }
        }

        if (isOpponentEntity) {
            drawY = container.getHeight() - entity.getY() - entity.getSpriteHeight();
        }

        return new Rectangle(drawX, drawY, entity.getSpriteWidth(), entity.getSpriteHeight());
    }

    public ArrayList<Entity> getEntities() {
        return entityManager.getEntities();
    }

    public boolean isWaitingForKeyPress() {
        return waitingForKeyPress;
    }

    public String getMessage() {
        return message;
    }

    public void requestTransition() {
        this.transitionRequested = true;
    }



    /**
     * A class to handle keyboard input from the user. The class
     * handles both dynamic input during game play, i.e. left/right
     * and shoot, and more static type input (i.e. press any key to
     * continue)
     *
     * This has been implemented as an inner class more through
     * habbit then anything else. Its perfectly normal to implement
     * this as seperate class if slight less convienient.
     *
     * @author Kevin Glass
     */
    // Game.java 파일의 KeyInputHandler 클래스 전체를 아래 코드로 교체


	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		SwingUtilities.invokeLater(() -> {
			new Game();
		});
	}
}
