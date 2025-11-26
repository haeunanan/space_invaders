package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.stage.Stage;

public class LevelManager {
    private final Game game;
    private final EntityManager entityManager;

    // [분리된 매니저들]
    private final GameSetupManager setupManager;
    private final GameResultHandler resultHandler;

    private int currentLevel = 1;
    private int stageIndex = 1;
    private Stage currentStage;

    private static final int MAX_STAGE = 6;
    private static final int BOSS_LEVEL = 5;

    // 메시지 상태
    private String message = "";
    private boolean waitingForKeyPress = true;

    public LevelManager(Game game, EntityManager entityManager) {
        this.game = game;
        this.entityManager = entityManager;

        // [초기화] 하위 매니저 생성
        this.setupManager = new GameSetupManager(game, entityManager);
        this.resultHandler = new GameResultHandler(game, this);
    }

    // --- [위임] 게임 시작/설정 (SetupManager) ---
    public void startNewGame() {
        setupManager.startNewGame(this);
    }

    public void startPvpGame() {
        setupManager.startPvpGame();
    }

    public void startCoopGame() {
        setupManager.startCoopGame();
    }

    // --- [위임] 게임 결과 처리 (ResultHandler) ---
    public void notifyAlienKilled() {
        resultHandler.notifyAlienKilled();
    }

    public void notifyWinPVP() {
        resultHandler.notifyWinPVP();
    }

    public void notifyWin() {
        resultHandler.notifyWin();
    }

    public void notifyDeath() {
        resultHandler.notifyDeath();
    }

    public void bossKilled() {
        resultHandler.bossKilled();
    }

    // --- LevelManager 고유 로직 (스테이지 진행) ---
    public void checkWinCondition() {
        // 협동 모드 승리 체크 (보스전 이전)
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP && currentLevel < BOSS_LEVEL) {
            if (entityManager.getAlienCount() == 0 && !waitingForKeyPress) {
                notifyWin();
            }
        }
    }

    public void nextStage() {
        this.setWaitingForKeyPress(false); // GameStateManger 혹은 직접 제어 확인 필요
        // 여기서는 game.setWaiting...이 사라졌으므로 this.setWaiting... 사용
        this.setWaitingForKeyPress(false);

        entityManager.clear();
        game.getInputManager().reset();

        if (stageIndex > MAX_STAGE) {
            setMessage("ALL STAGES CLEAR! Returning to Menu...");
            setWaitingForKeyPress(true);
            game.getGameStateManager().changeState(GameState.PVP_MENU);
            return;
        }

        currentStage = StageFactory.createStage(game, stageIndex);

        // (참고) 플레이어 재생성은 SetupManager 로직과 중복될 수 있으나,
        // 다음 스테이지 진행 시 위치 초기화를 위해 여기에 둡니다.
        // 필요하다면 setupManager.respawnShip() 같은 메서드로 뺄 수 있습니다.
        // 여기서는 간단하게 직접 생성합니다.
        org.newdawn.spaceinvaders.entity.ShipEntity ship =
                new org.newdawn.spaceinvaders.entity.ShipEntity(game, Constants.SHIP_SPRITE, 370, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        if (currentStage != null) {
            currentStage.init();
        } else {
            game.getGameStateManager().changeState(GameState.PVP_MENU);
        }
    }

    public void updateStage(long delta) {
        if (currentStage != null) {
            currentStage.update(delta);
            if (!waitingForKeyPress && currentStage.isCompleted()) {
                setWaitingForKeyPress(true);
                setMessage("Stage " + stageIndex + " Clear!");
                game.getPlayerStats().addCoins(50);
                game.getPlayerStats().addScore(100);
                stageIndex++;
            }
        }
    }

    // --- 헬퍼 메서드 (GameResultHandler 등이 사용) ---
    public void resetSinglePlayerState() {
        this.currentLevel = 1;
        game.getPlayerStats().resetScore();
    }

    public void resetForNewGame() {
        this.stageIndex = 1;
        this.currentLevel = 1;
        game.getPlayerStats().resetScore();
    }

    public void increaseLevel() {
        this.currentLevel++;
    }

    public void resetLevel() {
        this.currentLevel = 1;
    }

    public void increaseStageIndex() {
        this.stageIndex++;
    }

    // --- Getters / Setters ---
    public Stage getCurrentStage() { return currentStage; }
    public void setCurrentStage(Stage stage) { this.currentStage = stage; }

    public int getCurrentLevel() { return currentLevel; }

    public String getMessage() { return message; }
    public void setMessage(String msg) { this.message = msg; }

    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public void setWaitingForKeyPress(boolean val) { this.waitingForKeyPress = val; }
}