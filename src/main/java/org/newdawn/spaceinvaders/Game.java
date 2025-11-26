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
import java.util.EnumMap;


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
	// shop 브랜치에서 복사해올 변수들
	public boolean shopOpen = false;
	public final int UPGRADE_COST = 10;
	public final int MAX_UPGRADES = 6;
	public RankingManager rankingManager;
	private Thread matchmakingThread;
	private String currentMatchId;
	private volatile String player1_uid;
	private volatile String player2_uid;
	private Thread networkThread;
	private volatile Map<String, Object> lastOpponentState;
    private NetworkManager networkManager;
    private ShopManager shopManager;
    private PlayerController playerController;
    private LevelManager levelManager;

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
	// ===== Stage System (Missing fields added) =====
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
    private final EnumMap<GameState, Runnable> stateActions = new EnumMap<>(GameState.class);
    private WindowManager windowManager;
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
    public javax.swing.JFrame getContainer() {
        return windowManager.getContainer(); // WindowManager에게 위임
    }
    public boolean amIPlayer1() {
        return networkManager.amIPlayer1();
    }

    public Game() {
        // 1. 데이터 매니저 초기화
        this.playerStats = new PlayerStats();
        this.rankingManager = new RankingManager();
        this.entityManager = new EntityManager(this);
        this.networkManager = new NetworkManager(this);
        this.shopManager = new ShopManager(playerStats);

        // 2. Firebase 초기화
        new FirebaseInitializer().initialize();

        // 3. [핵심] 화면 관리자 생성 (복잡한 GUI 생성 코드가 여기로 이동됨)
        this.windowManager = new WindowManager(this);

        // 4. 입력 리스너 설정 (WindowManager를 통해 등록)
        this.inputManager = new InputManager(this);
        windowManager.addGameKeyListener(inputManager);

        // 5. 로직 매니저 초기화 (순서 주의: EntityManager 생성 후)
        this.levelManager = new LevelManager(this, entityManager);
        this.playerController = new PlayerController(this, inputManager);

        // 6. 마우스 리스너 등록 (상점/메뉴 클릭용)
        windowManager.addGameMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // (1) 메뉴로 돌아가기 버튼 (싱글 플레이 & 대기 중일 때)
                if (isWaitingForKeyPress() && getCurrentState() == GameState.PLAYING_SINGLE) {
                    // 좌표 범위: x(325~475), y(550~590)
                    if (mx >= 325 && mx <= 475 && my >= 550 && my <= 590) {
                        showReturnToMenuDialog();
                        return;
                    }
                }

                // (2) 상점 로직 (싱글 플레이 중일 때)
                if (getCurrentState() == GameState.PLAYING_SINGLE) {
                    // 상점 열기/닫기 버튼
                    if (isShopButtonClick(mx, my)) {
                        shopManager.toggleShop();
                        windowManager.gamePanelRepaint(); // 화면 갱신
                        return;
                    }

                    // 아이템 구매 클릭
                    if (shopManager.isShopOpen()) {
                        shopManager.handlePurchase(mx, my);
                        updateStatsBasedOnShop(); // 스탯 적용 메서드
                        windowManager.gamePanelRepaint();
                    }
                }
            }
        });

        // 7. 게임 시작
        initStateActions(); // 상태별 동작 정의
        changeState(GameState.START_MENU); // 시작 화면으로 이동
        windowManager.showWindow(); // 창 띄우기

        // 게임 루프 스레드 시작
        new Thread(this::gameLoop).start();
    }

    private void showReturnToMenuDialog() {
        int choice = JOptionPane.showConfirmDialog(
                windowManager.getContainer(),
                "진행 상황이 저장되지 않습니다. 메뉴로 돌아가시겠습니까?\n(레벨 1부터 다시 시작합니다)",
                "메뉴로 돌아가기",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            levelManager.resetSinglePlayerState();
            changeState(GameState.PVP_MENU); // 메뉴로 이동
        }
        // '아니오'를 누르면 아무것도 하지 않음
    }

    private void initStateActions() {
        // UI 변경은 windowManager에게 위임
        stateActions.put(GameState.START_MENU, () -> windowManager.changeCard(WindowManager.CARD_START));
        stateActions.put(GameState.SIGN_IN, () -> windowManager.changeCard(WindowManager.CARD_SIGN_IN));
        stateActions.put(GameState.SIGN_UP, () -> windowManager.changeCard(WindowManager.CARD_SIGN_UP));
        stateActions.put(GameState.PVP_MENU, () -> windowManager.changeCard(WindowManager.CARD_PVP_MENU));

        stateActions.put(GameState.MY_PAGE, () -> {
            windowManager.changeCard(WindowManager.CARD_MY_PAGE);
            windowManager.updateMyPage();
        });

        stateActions.put(GameState.PVP_LOBBY, () -> {
            windowManager.changeCard(WindowManager.CARD_PVP_LOBBY);
            networkManager.startMatchmakingLoop();
        });

        stateActions.put(GameState.COOP_LOBBY, () -> {
            windowManager.changeCard(WindowManager.CARD_PVP_LOBBY); // 로비 UI 재사용
            networkManager.startCoopMatchmakingLoop();
        });

        stateActions.put(GameState.PLAYING_SINGLE, () -> {
            windowManager.changeCard(WindowManager.CARD_PLAYING_SINGLE);
            windowManager.gamePanelRequestFocus(); // 포커스 요청 위임
            levelManager.startNewGame();
        });

        stateActions.put(GameState.PLAYING_PVP, () -> {
            waitingForKeyPress = false;
            windowManager.changeCard(WindowManager.CARD_PLAYING_SINGLE);
            SwingUtilities.invokeLater(() -> windowManager.gamePanelRequestFocus());
            levelManager.startPvpGame();
            networkManager.startNetworkLoop();
        });

        stateActions.put(GameState.PLAYING_COOP, () -> {
            windowManager.changeCard(WindowManager.CARD_PLAYING_SINGLE);
            SwingUtilities.invokeLater(() -> windowManager.gamePanelRequestFocus());
            levelManager.startCoopGame();
            networkManager.startNetworkLoop();
        });
    }

    private boolean isShopButtonClick(int mx, int my) {
        // UIRenderer의 상점 버튼 위치와 일치시킴 (x: 720~780, y: 10~40)
        int btnX = Constants.WINDOW_WIDTH - 60 - 20; // 720
        int btnY = 10;
        int btnW = 60;
        int btnH = 30;

        return mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
    }

    private void updateStatsBasedOnShop() {
        // 1. 공격 속도 재계산 (레벨당 10% 감소)
        long baseFireInterval = 500;
        for (int i = 0; i < playerStats.getAttackLevel(); i++) {
            baseFireInterval *= 0.9;
        }
        this.firingInterval = Math.max(100, baseFireInterval);

        // 2. 이동 속도 재계산 (레벨당 10% 증가)
        double baseSpeed = 300;
        for (int i = 0; i < playerStats.getMoveLevel(); i++) {
            baseSpeed *= 1.1;
        }
        this.moveSpeed = baseSpeed;
    }

    public void changeState(GameState newState) {
        currentState = newState;
        networkManager.stopAllThreads(); // 상태 변경 시 스레드 정리는 공통

        Runnable action = stateActions.get(newState);
        if (action != null) {
            action.run();
        }
    }

	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entitiy will be added to the overall list of entities in the game.
	 */


    public void addEntity(Entity entity) {
        entityManager.addEntity(entity);
    }

    public double getMoveSpeed() {
        return (slowTimer > 0) ? moveSpeed * 0.5 : moveSpeed;
    }
    public boolean isReverseControls() {
        return reverseControls;
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


    // ▼▼▼ PVP 승리/패배 처리 메소드 추가 ▼▼▼
    public void notifyWinPVP() {
        if (currentState != GameState.PLAYING_PVP) return; // 중복 호출 방지
        playerStats.addScore(30);
        message = "You win! 30 reward coins";
        waitingForKeyPress = true;
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

            updateFPS(delta);
            updateTimers(delta);

            if (transitionRequested) {
                levelManager.nextStage();
                transitionRequested = false;
                continue;
            }

            if (isPlayingState()) {
                processGameLogic(delta); // 로직 처리 (이전 단계에서 분리함)
                windowManager.gamePanelRepaint(); // [수정] 게임 패널만 다시 그리기
            } else {
                windowManager.repaint(); // [수정] 전체 다시 그리기 (메뉴 등)
            }

            SystemTimer.sleep(10);
        }
    }

    private void processGameLogic(long delta) {
        playerController.setShip((ShipEntity) ship);
        playerController.update();

        entityManager.doLogic();
        levelManager.updateStage(delta);
        entityManager.moveEntities(delta);
        entityManager.checkCollisions();
        levelManager.checkWinCondition();
        entityManager.removeDeadEntities();
    }

    // --- 리팩토링으로 추출된 헬퍼 메서드들 ---
    public boolean isShopOpen() { return shopManager.isShopOpen(); }
    public NetworkManager getNetworkManager() { return networkManager; }

    private boolean isPlayingState() {
        return currentState == GameState.PLAYING_SINGLE ||
                currentState == GameState.PLAYING_PVP ||
                currentState == GameState.PLAYING_COOP;
    }

    private void updateFPS(long delta) {
        lastFpsTime += delta;
        fps++;
        if (lastFpsTime >= 1000) {
            // [수정] container.setTitle(...) -> windowManager.setWindowTitle(...)
            windowManager.setWindowTitle(windowTitle + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }
    }

    private void updateTimers(long delta) {
        if (slowTimer > 0) {
            slowTimer -= delta;
        }
    }



    private void processEnemyLogic() {
        if (logicRequiredThisLoop) {
            entityManager.doLogic(); // 위임
            logicRequiredThisLoop = false;
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
            drawY = getContainer().getHeight() - entity.getY() - entity.getSpriteHeight();
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
    public void setMessage(String message) { this.message = message; }
    public void setWaitingForKeyPress(boolean waiting) { this.waitingForKeyPress = waiting; }
    public void setSlowTimer(long timer) { this.slowTimer = timer; }
    public InputManager getInputManager() { return inputManager; }
    public EntityManager getEntityManager() { return entityManager; } // 중요!
    public RankingManager getRankingManager() { return rankingManager; }
    public PlayerStats getPlayerStats() { return playerStats; }
    public LevelManager getLevelManager() { return levelManager; }
    public void setShip(Entity ship) {
        this.ship = ship;
    }
    public void setOpponentShip(Entity opponentShip) {
        this.opponentShip = opponentShip;
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
