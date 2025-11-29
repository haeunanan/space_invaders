package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.stage.Stage;

public class LevelManager {
    private final Game game;
    private final EntityManager entityManager;

    // [변경] SetupManager와 ResultHandler는 Game 클래스에서 직접 관리하거나
    // 필요하다면 여기서 getter로 노출만 합니다.
    private final StageTransitionHelper transitionHelper;

    private int currentLevel = 1;
    private int stageIndex = 1;
    private Stage currentStage;

    private static final int MAX_STAGE = 6;
    private static final int BOSS_LEVEL = 5;

    private String message = "";
    private boolean waitingForKeyPress = true;

    public LevelManager(Game game, EntityManager entityManager, GameSetupManager setupManager) {
        this.game = game;
        this.entityManager = entityManager;
        this.transitionHelper = new StageTransitionHelper(game, setupManager);
    }

    // [핵심] 복잡했던 nextStage 로직은 이미 Helper로 위임됨
    public void nextStage() {
        this.setWaitingForKeyPress(false);
        Stage nextStage = transitionHelper.prepareNextStage(stageIndex);

        if (nextStage == null) {
            handleAllStagesClear();
        } else {
            this.currentStage = nextStage;
        }
    }

    private void handleAllStagesClear() {
        setMessage("ALL STAGES CLEAR! Returning to Menu...");
        setWaitingForKeyPress(true);
        game.getGameStateManager().changeState(GameState.PVP_MENU);
    }

    public void updateStage(long delta) {
        if (currentStage == null) return;
        currentStage.update(delta);

        if (isGuest()) return;

        if (!waitingForKeyPress && currentStage.isCompleted()) {
            setWaitingForKeyPress(true);
            setMessage("Stage " + stageIndex + " Clear!");
            game.getPlayerStats().addCoins(50);
            game.getPlayerStats().addScore(100);
            stageIndex++;
        }
    }

    // [변경] 승리 조건 체크 로직도 ResultHandler로 이동하거나 단순화
    public void checkWinCondition() {
        if (isGuest()) return;
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP
                && currentLevel < BOSS_LEVEL) {
            if (entityManager.getAlienCount() == 0 && !waitingForKeyPress) {
                game.getResultHandler().notifyWin(); // [변경] 직접 호출
            }
        }
    }

    private boolean isGuest() {
        return game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP
                && !game.getNetworkManager().amIPlayer1();
    }

    public void resetSinglePlayerState() {
        this.currentLevel = 1;
        game.getPlayerStats().resetScore();
    }

    public void resetForNewGame() {
        this.stageIndex = 1;
        this.currentLevel = 1;
        game.getPlayerStats().resetScore();
        game.getPlayerStats().setCoins(0);
    }

    public void increaseLevel() { this.currentLevel++; }
    public void resetLevel() { this.currentLevel = 1; }
    public void increaseStageIndex() { this.stageIndex++; }

    // Getters & Setters
    public int getStageIndex() { return stageIndex; }
    public void setStageIndex(int index) { this.stageIndex = index; }
    public Stage getCurrentStage() { return currentStage; }
    public void setCurrentStage(Stage stage) { this.currentStage = stage; }
    public int getCurrentLevel() { return currentLevel; }
    public String getMessage() { return message; }
    public void setMessage(String msg) { this.message = msg; }
    public boolean isWaitingForKeyPress() { return waitingForKeyPress; }
    public void setWaitingForKeyPress(boolean val) { this.waitingForKeyPress = val; }

    // [삭제된 메소드들]
    // startNewGame(), startPvpGame(), startCoopGame() -> GameSetupManager 직접 호출
    // notifyAlienKilled(), notifyWin(), notifyDeath() -> GameResultHandler 직접 호출
}