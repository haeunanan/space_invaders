package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import javax.swing.*;

public class Game {
    // --- 필드 ---
    private boolean gameRunning = true;
    public long lastFire = 0;

    // 상태 변수
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
    private GameStateManager gameStateManager;
    private MatchmakingManager matchmakingManager;

    // [GameSetupManager는 내부에서만 사용되므로 Getter 제거]
    private GameSetupManager setupManager;
    private GameResultHandler resultHandler;

    // 엔티티 참조
    private Entity ship;
    private Entity opponentShip;

    public Game() {
        // [리팩토링] 초기화 메소드들을 생성자로 인라인(Inline)하여 WMC 감소

        // 1. Core Systems 초기화
        this.gameStateManager = new GameStateManager(this);
        this.playerStats = new PlayerStats();
        this.rankingManager = new RankingManager();
        this.entityManager = new EntityManager(this);
        this.networkManager = new NetworkManager(this);
        this.shopManager = new ShopManager(playerStats);

        // SetupManager & ResultHandler 생성 (순서 중요)
        this.setupManager = new GameSetupManager(this, entityManager);
        this.levelManager = new LevelManager(this, entityManager, setupManager);
        this.resultHandler = new GameResultHandler(this, levelManager);

        new FirebaseInitializer().initialize();
        this.matchmakingManager = new MatchmakingManager(this);

        // 2. Window & Input 초기화
        this.windowManager = new WindowManager(this);
        this.inputManager = new InputManager(this);
        windowManager.addGameKeyListener(inputManager);
        this.playerController = new PlayerController(this, inputManager);

        // 3. MouseListener 설정
        GameMouseListener mouseListener = new GameMouseListener(this);
        windowManager.addGameMouseListener(mouseListener);

        // 4. 게임 시작
        changeState(GameState.START_MENU);
        windowManager.showWindow();

        new Thread(this::gameLoop).start();
    }

    // --- 게임 루프 ---
    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();
        while (gameRunning) {
            long now = SystemTimer.getTime();
            long delta = now - lastLoopTime;
            lastLoopTime = now;

            windowManager.updateFPS(delta);
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
        levelManager.updateStage(delta);
        entityManager.moveEntities(delta);
        entityManager.checkCollisions();
        levelManager.checkWinCondition();
        entityManager.removeDeadEntities();
    }

    public boolean isPlayingState() {
        return gameStateManager.isPlayingState();
    }

    // --- 상태 변경 ---
    public void changeState(GameState newState) {
        gameStateManager.changeState(newState);
    }

    public void stopGame() {
        this.gameRunning = false;
    }

    public void requestTransition() {
        this.transitionRequested = true;
    }

    // --- Getters / Setters ---
    public GameState getCurrentState() {
        return gameStateManager.getCurrentState();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public RankingManager getRankingManager() {
        return rankingManager;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public Entity getShip() {
        return ship;
    }

    public void setShip(Entity ship) {
        this.ship = ship;
    }

    public MatchmakingManager getMatchmakingManager() {
        return matchmakingManager;
    }

    public Entity getOpponentShip() {
        return opponentShip;
    }

    public void setOpponentShip(Entity ship) {
        this.opponentShip = ship;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public org.newdawn.spaceinvaders.stage.Stage getCurrentStage() {
        return levelManager.getCurrentStage();
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public GameResultHandler getResultHandler() {
        return resultHandler;
    }
    public GameSetupManager getSetupManager() {
        return setupManager;
    }
}