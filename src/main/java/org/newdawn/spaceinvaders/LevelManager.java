package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.StageFactory;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.stage.Stage;


import javax.swing.*;

public class LevelManager {
    private final Game game;
    private final EntityManager entityManager;

    private int currentLevel = 1;
    private int stageIndex = 1;
    private Stage currentStage;

    private static final int MAX_STAGE = 6;
    private static final int BOSS_LEVEL = 5;

    public LevelManager(Game game, EntityManager entityManager) {
        this.game = game;
        this.entityManager = entityManager;
    }

    public void startNewGame() {
        entityManager.clear();
        game.setSlowTimer(0);

        stageIndex = 1;
        currentStage = StageFactory.createStage(game, stageIndex);

        // 플레이어 생성
        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, Constants.PLAYER_START_X, Constants.PLAYER_START_Y);
        ship.setHealth(3);
        entityManager.addEntity(ship);

        // Game에 ship 참조 설정 (PlayerController 등을 위해)
        game.setShip(ship);

        if (currentStage != null) {
            currentStage.init();
        }

        game.setWaitingForKeyPress(false);
        game.getInputManager().reset();
    }

    public void setupCoopStage() {
        // 레벨 1 난이도로 설정
        double moveSpeed = 100;
        int alienRows = 3;
        double firingChance = 0;
        int startY = 50;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 12; x++) {
                Entity alien = new AlienEntity(game, "sprites/alien.gif",
                        100 + (x * 50), startY + row * 30, moveSpeed, firingChance);
                entityManager.addEntity(alien);
            }
        }
    }

    public void startPvpGame() {
        entityManager.clear();
        game.getInputManager().reset();

        // 내 우주선 (아래쪽)
        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 370, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        // 상대방 우주선 (위쪽)
        ShipEntity opponentShip = new ShipEntity(game, "sprites/opponent_ship.gif", 370, 50);
        opponentShip.setHealth(3);
        entityManager.addEntity(opponentShip);
        game.setOpponentShip(opponentShip); // Game에 Setter 추가 필요
    }

    public void startCoopGame() {
        entityManager.clear();
        game.getInputManager().reset();

        // 내 우주선
        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 300, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        // 상대방 우주선 (협동 파트너)
        ShipEntity opponentShip = new ShipEntity(game, Constants.SHIP_SPRITE, 500, 550);
        opponentShip.setHealth(3);
        entityManager.addEntity(opponentShip);
        game.setOpponentShip(opponentShip);

        setupCoopStage(); // 외계인 생성
    }

    public void nextStage() {
        game.setWaitingForKeyPress(false);
        entityManager.clear();
        game.getInputManager().reset();

        // 다음 스테이지 진입 시 조작 반전 등 초기화
        // (필요 시 Game에 setReverseControls 메서드 추가)
        // game.setReverseControls(false);

        if (stageIndex > MAX_STAGE) {
            game.setMessage("ALL STAGES CLEAR! Returning to Menu...");
            game.setWaitingForKeyPress(true);
            game.changeState(Game.GameState.PVP_MENU);
            return;
        }

        currentStage = StageFactory.createStage(game, stageIndex);

        // 플레이어 재생성
        ShipEntity ship = new ShipEntity(game, Constants.SHIP_SPRITE, 370, 550);
        ship.setHealth(3);
        entityManager.addEntity(ship);
        game.setShip(ship);

        if (currentStage != null) {
            currentStage.init();
        } else {
            game.changeState(Game.GameState.PVP_MENU);
        }
    }

    public void updateStage(long delta) {
        if (currentStage != null) {
            currentStage.update(delta);
            if (currentStage.isCompleted()) {
                game.setWaitingForKeyPress(true);
                game.setMessage("Stage " + stageIndex + " Clear!");
                game.getPlayerStats().addCoins(50);
                game.getPlayerStats().addScore(100);
                stageIndex++;
            }
        }
    }

    public void checkWinCondition() {
        // 협동 모드 승리 체크 (보스전 이전)
        if (game.getCurrentState() == Game.GameState.PLAYING_COOP && currentLevel < BOSS_LEVEL) {
            if (entityManager.getAlienCount() == 0 && !game.isWaitingForKeyPress()) {
                notifyWin();
            }
        }
    }

    public void notifyWin() {
        game.getPlayerStats().addScore(100);
        game.getPlayerStats().addCoins(50);
        currentLevel++;

        String message;
        if (currentLevel > BOSS_LEVEL) {
            message = "Congratulations! You have defeated the final boss!";
            currentLevel = 1;
        } else if (currentLevel == BOSS_LEVEL) {
            message = "Final Stage! The Boss is approaching!";
        } else {
            message = "Stage " + (currentLevel - 1) + " Cleared! Prepare for the next stage.";
        }

        game.setMessage(message);
        game.setWaitingForKeyPress(true);
    }

    public void notifyDeath() {
        if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
            if (game.isWaitingForKeyPress()) return;
            game.setMessage("You Lose...");
            game.setWaitingForKeyPress(true);
            return;
        }

        game.setMessage("Oh no! They got you, try again?");
        game.setWaitingForKeyPress(true);

        // 랭킹 처리 로직
        if (CurrentUserManager.getInstance().isLoggedIn()) {
            String nickname = CurrentUserManager.getInstance().getNickname();
            game.getRankingManager().addScore(game.getPlayerStats().getScore(), nickname);
        } else {
            if (game.getRankingManager().isHighScore(game.getPlayerStats().getScore())) {
                String name = JOptionPane.showInputDialog(game.getContainer(), "New High Score! Enter your name:", "Ranking", JOptionPane.PLAIN_MESSAGE);
                if (name != null && !name.trim().isEmpty()) {
                    game.getRankingManager().addScore(game.getPlayerStats().getScore(), name);
                }
            }
        }

        resetSinglePlayerState();
    }

    public void bossKilled() {
        game.setMessage("BOSS DEFEATED!");
        game.setWaitingForKeyPress(true);
        stageIndex++;
    }

    public void resetSinglePlayerState() {
        this.currentLevel = 1;
        game.getPlayerStats().resetScore();
    }

    public Stage getCurrentStage() { return currentStage; }
    public int getCurrentLevel() { return currentLevel; }
}