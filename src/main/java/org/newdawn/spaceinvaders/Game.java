package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import javax.swing.*;

public class Game {
    // --- 필드 ---
    private boolean gameRunning = true;
    public long lastFire = 0;
    public long firingInterval = 500;

    // 상태 변수
    private GameState currentState;
    private String message = "";
    private boolean waitingForKeyPress = true;
    public boolean reverseControls = false;
    private boolean transitionRequested = false;

    // 매니저들
    private WindowManager windowManager;
    private InputManager inputManager;
    private EntityManager entityManager;
    private LevelManager levelManager;
    private NetworkManager networkManager;
    private ShopManager shopManager;
    private PlayerController playerController;
    public PlayerStats playerStats; // public 접근 허용 (편의상)
    public RankingManager rankingManager;

    // 엔티티 참조
    private Entity ship;
    private Entity opponentShip;

    // FPS 계산용
    private long lastFpsTime;
    private int fps;
    private String windowTitle = "Space Invaders 102";

    public Game() {
        // 1. 초기화
        initializeCoreSystems();
        initializeWindowAndInput();

        // 2. 시작
        initStateActions();
        changeState(GameState.START_MENU);
        windowManager.showWindow();

        new Thread(this::gameLoop).start();
    }

    private void initializeCoreSystems() {
        this.playerStats = new PlayerStats();
        this.rankingManager = new RankingManager();
        this.entityManager = new EntityManager(this);
        this.networkManager = new NetworkManager(this);
        this.shopManager = new ShopManager(playerStats);
        this.levelManager = new LevelManager(this, entityManager);
        new FirebaseInitializer().initialize();
    }

    private void initializeWindowAndInput() {
        this.windowManager = new WindowManager(this);
        this.inputManager = new InputManager(this);
        windowManager.addGameKeyListener(inputManager);
        this.playerController = new PlayerController(this, inputManager);
        setupMouseListeners();
    }

    private void setupMouseListeners() {
        GameMouseListener mouseListener = new GameMouseListener(this);
        windowManager.addGameMouseListener(mouseListener);
    }

    private void showReturnToMenuDialog() {
        int choice = JOptionPane.showConfirmDialog(windowManager.getContainer(),
                "메뉴로 돌아가시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            levelManager.resetSinglePlayerState();
            changeState(GameState.PVP_MENU);
        }
    }

    // --- 게임 루프 ---
    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();
        while (gameRunning) {
            long now = SystemTimer.getTime();
            long delta = now - lastLoopTime;
            lastLoopTime = now;

            updateFPS(delta);
            // [변경] PlayerController에게 타이머 업데이트 위임
            playerController.updateTimer(delta);

            if (transitionRequested) {
                levelManager.nextStage();
                transitionRequested = false;
                continue;
            }

            if (isPlayingState()) {
                processGameLogic(delta);
                windowManager.gamePanelRepaint();
            } else {
                windowManager.repaint();
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

    private boolean isPlayingState() {
        return currentState == GameState.PLAYING_SINGLE ||
                currentState == GameState.PLAYING_PVP ||
                currentState == GameState.PLAYING_COOP;
    }

    private void updateFPS(long delta) {
        lastFpsTime += delta;
        fps++;
        if (lastFpsTime >= 1000) {
            windowManager.setWindowTitle(windowTitle + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }
    }

    // [중요] 상점 업그레이드 반영 (Game에 남겨두거나 PlayerController로 이동 가능)
    public void updateStatsBasedOnShop() {
        // 공격 속도 갱신
        long baseFire = 500;
        for(int i=0; i<playerStats.getAttackLevel(); i++) baseFire *= 0.9;
        this.firingInterval = Math.max(100, baseFire);

        // 이동 속도 갱신은 PlayerController에게 위임
        playerController.upgradeMoveSpeed();
    }

    // --- 상태 변경 ---
    public void changeState(GameState newState) {
        currentState = newState;
        networkManager.stopAllThreads();
        Runnable action = stateActions.get(newState);
        if (action != null) action.run();
    }

    // [삭제 대상이었던 메서드들 제거됨]:
    // notifyDeath, notifyWinPVP, notifyAlienKilled -> LevelManager로 이동
    // getVisualBounds -> CollisionManager로 이동
    // purchaseAttackSpeed 등 -> ShopManager가 처리
    // applySlow -> PlayerController로 이동
    // addEntity, removeEntity -> EntityManager 직접 접근 권장 (단, 호환성을 위해 남길 수도 있음)

    // --- Getters / Setters ---
    public GameState getCurrentState() { return currentState; }
    public EntityManager getEntityManager() { return entityManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public NetworkManager getNetworkManager() { return networkManager; }
    public PlayerController getPlayerController() { return playerController; }
    public RankingManager getRankingManager() { return rankingManager; }
    public PlayerStats getPlayerStats() { return playerStats; }
    public boolean isShopOpen() { return shopManager.isShopOpen(); }
    public Entity getShip() { return ship; }
    public void setShip(Entity ship) { this.ship = ship; }
    public Entity getOpponentShip() { return opponentShip; }
    public void setOpponentShip(Entity ship) { this.opponentShip = ship; }
    public String getMessage() { return message; }
    public void setMessage(String msg) { this.message = msg; }
    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public void setWaitingForKeyPress(boolean val) { this.waitingForKeyPress = val; }
    public void requestTransition() { this.transitionRequested = true; }
    public InputManager getInputManager() { return inputManager; }
    public org.newdawn.spaceinvaders.stage.Stage getCurrentStage() { return levelManager.getCurrentStage(); }
    public boolean isReverseControls() { return reverseControls; }
    public WindowManager getWindowManager() { return windowManager; }
    public ShopManager getShopManager() { return shopManager; }

    // --- State Map ---
    private final java.util.EnumMap<GameState, Runnable> stateActions = new java.util.EnumMap<>(GameState.class);
    private void initStateActions() {
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
            windowManager.changeCard(WindowManager.CARD_PVP_LOBBY);
            networkManager.startCoopMatchmakingLoop();
        });
        stateActions.put(GameState.PLAYING_SINGLE, () -> {
            windowManager.changeCard(WindowManager.CARD_PLAYING_SINGLE);
            windowManager.gamePanelRequestFocus();
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

    public static void main(String argv[]) {
        SwingUtilities.invokeLater(Game::new);
    }
}