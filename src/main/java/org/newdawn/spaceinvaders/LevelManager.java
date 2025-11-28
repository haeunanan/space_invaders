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

    public int getStageIndex() { return stageIndex; }
    public void setStageIndex(int index) { this.stageIndex = index; }

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
        setupManager.startCoopGame(this);
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

    public void checkWinCondition() {
        // [수정] 게스트는 승리 체크를 하지 않음
        if (isGuest()) return;

        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP && currentLevel < BOSS_LEVEL) {
            if (entityManager.getAlienCount() == 0 && !waitingForKeyPress) {
                notifyWin();
            }
        }
    }
    private boolean isGuest() {
        return game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP
                && !game.getNetworkManager().amIPlayer1();
    }

    public void nextStage() {
        this.setWaitingForKeyPress(false);

        entityManager.clear();
        game.getInputManager().reset();
        game.getPlayerController().applySlow(0); // 스테이지 넘어가면 슬로우 해제

        if (stageIndex > MAX_STAGE) {
            setMessage("ALL STAGES CLEAR! Returning to Menu...");
            setWaitingForKeyPress(true);
            game.getGameStateManager().changeState(GameState.PVP_MENU);
            return;
        }

        currentStage = StageFactory.createStage(game, stageIndex);

        setupManager.respawnShipsForNextStage(stageIndex);

        if (currentStage != null) {
            currentStage.init();
        } else {
            game.getGameStateManager().changeState(GameState.PVP_MENU);
        }
    }

    public void updateStage(long delta) {
        if (currentStage != null) {
            currentStage.update(delta);

            // [수정] 협동 모드 게스트라면, 스스로 스테이지 클리어를 판단하지 않음 (호스트 명령 대기)
            if (isGuest()) return;

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