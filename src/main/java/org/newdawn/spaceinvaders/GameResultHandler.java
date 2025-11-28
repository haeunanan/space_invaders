package org.newdawn.spaceinvaders;

import javax.swing.JOptionPane;

public class GameResultHandler {
    private final Game game;
    private final LevelManager levelManager;

    private static final int BOSS_LEVEL = 6;

    public GameResultHandler(Game game, LevelManager levelManager) {
        this.game = game;
        this.levelManager = levelManager;
    }

    public void notifyAlienKilled() {
        game.getPlayerStats().addScore(100);
    }

    public void notifyWinPVP() {
        // PVP 승리 처리
        if (game.getGameStateManager().getCurrentState() != GameState.PLAYING_PVP) return;

        game.getPlayerStats().addScore(30);
        levelManager.setMessage("You win! 30 reward coins");
        levelManager.setWaitingForKeyPress(true);
    }

    public void notifyWin() {
        // 싱글/협동 승리(스테이지 클리어) 처리
        game.getPlayerStats().addScore(100);
        game.getPlayerStats().addCoins(50);

        levelManager.increaseLevel(); // 레벨 증가

        String msg;
        int currentLevel = levelManager.getCurrentLevel();

        if (currentLevel > BOSS_LEVEL) {
            msg = "Congratulations! You have defeated the final boss!";
            levelManager.resetLevel(); // 레벨 초기화
        } else if (currentLevel == BOSS_LEVEL) {
            msg = "Final Stage! The Boss is approaching!";
        } else {
            msg = "Stage " + (currentLevel - 1) + " Cleared! Prepare for the next stage.";
        }

        levelManager.setMessage(msg);
        levelManager.setWaitingForKeyPress(true);
    }

    public void notifyDeath() {
        // 1. PVP 패배 처리
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_PVP) {
            if (levelManager.isWaitingForKeyPress()) return;
            levelManager.setMessage("You Lose...");
            levelManager.setWaitingForKeyPress(true);
            return;
        }

        // 2. 싱글 플레이 사망 처리
        levelManager.setMessage("Oh no! They got you, try again?");
        levelManager.setWaitingForKeyPress(true);

        // 3. 랭킹 처리
        processRanking();

        // 4. 상태 리셋
        levelManager.resetSinglePlayerState();
    }

    public void bossKilled() {
        levelManager.setMessage("BOSS DEFEATED!");
        levelManager.setWaitingForKeyPress(true);
        levelManager.increaseStageIndex(); // 스테이지 인덱스 증가
    }

    private void processRanking() {
        int currentScore = game.getPlayerStats().getScore();
        RankingManager rankingManager = game.getRankingManager();

        if (CurrentUserManager.getInstance().isLoggedIn()) {
            // 로그인 상태면 자동 등록
            String nickname = CurrentUserManager.getInstance().getNickname();
            rankingManager.addScore(currentScore, nickname);
        } else {
            // 비로그인 상태면 신기록일 때만 입력창 띄움
            if (rankingManager.isHighScore(currentScore)) {
                String name = JOptionPane.showInputDialog(
                        game.getWindowManager().getContainer(),
                        "New High Score! Enter your name:",
                        "Ranking",
                        JOptionPane.PLAIN_MESSAGE
                );
                if (name != null && !name.trim().isEmpty()) {
                    rankingManager.addScore(currentScore, name);
                }
            }
        }
    }
}
