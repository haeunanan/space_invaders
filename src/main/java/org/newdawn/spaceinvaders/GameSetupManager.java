package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.stage.Stage;

public class GameSetupManager {
    private final Game game;
    private final EntityManager entityManager;

    public GameSetupManager(Game game, EntityManager entityManager) {
        this.game = game;
        this.entityManager = entityManager;
    }

    public void startNewGame(LevelManager levelManager) {
        entityManager.clear();
        game.getPlayerController().applySlow(0);

        // LevelManager의 상태 리셋
        levelManager.resetForNewGame();

        Stage currentStage = StageFactory.createStage(game, 1);
        levelManager.setCurrentStage(currentStage);

        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, Constants.PLAYER_START_X, Constants.PLAYER_START_Y);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        if (currentStage != null) currentStage.init();

        game.getLevelManager().setWaitingForKeyPress(false);
        game.getInputManager().reset();
    }

    public void startPvpGame() {
        entityManager.clear();
        game.getInputManager().reset();

        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 370, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        ShipEntity opponentShip = new ShipEntity(game, "sprites/opponent_ship.gif", 370, 50);
        opponentShip.setHealth(3);
        entityManager.addEntity(opponentShip);
        game.setOpponentShip(opponentShip);
    }

    public void startCoopGame(LevelManager levelManager) {
        entityManager.clear();
        game.getPlayerController().applySlow(0); // 디버프 초기화
        game.getInputManager().reset();

        // 1. 레벨/스테이지 상태 리셋 (싱글 플레이와 동일하게 1단계부터 시작)
        levelManager.resetForNewGame();

        // 2. 1스테이지(화성) 생성
        Stage currentStage = StageFactory.createStage(game, 1);
        levelManager.setCurrentStage(currentStage);

        // 3. 플레이어(나) 생성 - 왼쪽 위치
        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 300, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        // 4. 협동 플레이어(상대방) 생성 - 오른쪽 위치
        // (Co-op 모드에서는 상대방도 ShipEntity이며 아군 판정)
        ShipEntity opponentShip = new ShipEntity(game, Constants.SHIP_SPRITE, 500, 550);
        opponentShip.setHealth(3);
        entityManager.addEntity(opponentShip);
        game.setOpponentShip(opponentShip);

        // 5. 스테이지 초기화 (적 생성 등)
        if (currentStage != null) currentStage.init();

        // 키 입력 대기 해제
        game.getLevelManager().setWaitingForKeyPress(false);
    }
    public void respawnShipsForNextStage(int stageIndex) {
        // LevelManager의 nextStage에 있던 기체 생성 분기문(if-else)을 이곳으로 이동
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP) {
            // 협동 모드 기체 2개 생성 로직
        } else {
            // 싱글 모드 기체 1개 생성 로직
        }
    }

}