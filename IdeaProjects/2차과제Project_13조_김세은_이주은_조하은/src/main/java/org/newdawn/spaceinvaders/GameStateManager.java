package org.newdawn.spaceinvaders;

import javax.swing.SwingUtilities;

public class GameStateManager {
    private final Game game;
    private GameState currentState;

    public GameStateManager(Game game) {
        this.game = game;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public boolean isPlayingState() {
        return currentState == GameState.PLAYING_SINGLE ||
                currentState == GameState.PLAYING_PVP ||
                currentState == GameState.PLAYING_COOP;
    }

    public void changeState(GameState newState) {
        this.currentState = newState;
        // 기존 네트워크 스레드 정리
        if (game.getNetworkManager() != null) {
            game.getNetworkManager().stopAllThreads();
        }
        // [추가] 매치메이킹 스레드도 정리 (상태 변경 시 멈춤)
        if (game.getMatchmakingManager() != null) {
            game.getMatchmakingManager().stopMatchmaking();
        }
        switch (newState) {
            case START_MENU:
            case SIGN_IN:
            case SIGN_UP:
            case PVP_MENU:
            case MY_PAGE:
                handleMenuState(newState); // [리팩토링] 단순 메뉴 전환 통합
                break;

            case PVP_LOBBY:
            case COOP_LOBBY:
                handleLobbyState(newState); // [리팩토링] 로비 진입 통합
                break;

            case PLAYING_SINGLE:
            case PLAYING_PVP:
            case PLAYING_COOP:
                handlePlayingState(newState); // [리팩토링] 게임 플레이 진입 통합
                break;
        }
    }
    private void handleMenuState(GameState state) {
        WindowManager wm = game.getWindowManager();
        if (state == GameState.START_MENU) wm.changeCard(WindowManager.CARD_START);
        else if (state == GameState.SIGN_IN) wm.changeCard(WindowManager.CARD_SIGN_IN);
        else if (state == GameState.SIGN_UP) wm.changeCard(WindowManager.CARD_SIGN_UP);
        else if (state == GameState.PVP_MENU) wm.changeCard(WindowManager.CARD_PVP_MENU);
        else if (state == GameState.MY_PAGE) {
            wm.changeCard(WindowManager.CARD_MY_PAGE);
            wm.updateMyPage();
        }
    }
    private void handleLobbyState(GameState state) {
        game.getWindowManager().changeCard(WindowManager.CARD_PVP_LOBBY);
        if (state == GameState.PVP_LOBBY) {
            game.getMatchmakingManager().startMatchmakingLoop();
        } else {
            game.getMatchmakingManager().startCoopMatchmakingLoop();
        }
    }
    private void handlePlayingState(GameState state) {
        game.getWindowManager().changeCard(WindowManager.CARD_PLAYING_SINGLE);

        if (state == GameState.PLAYING_SINGLE) {
            game.getWindowManager().gamePanelRequestFocus();
            // LevelManager 인스턴스를 인자로 전달
            game.getSetupManager().startNewGame(game.getLevelManager());
        } else {
            SwingUtilities.invokeLater(() -> game.getWindowManager().gamePanelRequestFocus());

            if (state == GameState.PLAYING_PVP) {
                game.getLevelManager().setWaitingForKeyPress(false);
                game.getSetupManager().startPvpGame(); // [수정]
            } else {
                game.getSetupManager().startCoopGame(game.getLevelManager()); // [수정]
            }
            game.getNetworkManager().startNetworkLoop();
        }
    }
}