package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
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

    public void startCoopGame() {
        entityManager.clear();
        game.getInputManager().reset();

        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 300, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        ShipEntity opponentShip = new ShipEntity(game, Constants.SHIP_SPRITE, 500, 550);
        opponentShip.setHealth(3);
        entityManager.addEntity(opponentShip);
        game.setOpponentShip(opponentShip);

        setupCoopStage();
    }

    private void setupCoopStage() {
        double moveSpeed = 100;
        int alienRows = 3;
        int startY = 50;
        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(game, "sprites/alien.gif",
                        100 + (x * 50), startY + row * 30, moveSpeed, 0);
                entityManager.addEntity(alien);
            }
        }
    }
}