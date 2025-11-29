package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.stage.Stage;

public class StageTransitionHelper {
    private final Game game;
    private final GameSetupManager setupManager;

    public StageTransitionHelper(Game game, GameSetupManager setupManager) {
        this.game = game;
        this.setupManager = setupManager;
    }

    public Stage prepareNextStage(int stageIndex) {
        // 1. 상태 리셋
        game.getEntityManager().clear();
        game.getInputManager().reset();
        game.getPlayerController().applySlow(0);

        // 2. 게임 클리어 체크
        if (stageIndex > 6) {
            return null; // 모든 스테이지 클리어 신호
        }

        // 3. 스테이지 생성
        Stage newStage = StageFactory.createStage(game, stageIndex);

        // 4. 기체 재배치
        setupManager.respawnShipsForNextStage(stageIndex);

        // 5. 초기화
        if (newStage != null) {
            newStage.init();
        }
        return newStage;
    }
}