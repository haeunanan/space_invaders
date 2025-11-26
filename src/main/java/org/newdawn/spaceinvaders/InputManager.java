package org.newdawn.spaceinvaders;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputManager extends KeyAdapter {

    private Game game;

    // 키 상태 변수들
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    public InputManager(Game game) {
        this.game = game;
    }

    // ... (Getter 및 reset 메소드는 기존 유지) ...
    public boolean isLeftPressed() { return leftPressed; }
    public boolean isRightPressed() { return rightPressed; }
    public boolean isFirePressed() { return firePressed; }
    public boolean isUpPressed() { return upPressed; }
    public boolean isDownPressed() { return downPressed; }

    public void reset() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        firePressed = false;
    }

    /**
     * 리팩토링된 keyPressed 메소드
     * 복잡한 로직을 별도 메소드로 위임하여 인지 복잡도를 대폭 낮췄습니다.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 1. 강제 종료
        if (key == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        // 2. 대기 상태일 때의 키 처리 (메소드 추출)
        if (game.getLevelManager().isWaitingForKeyPress()) {
            handleWaitingKeyPress();
            return;
        }

        // 3. 게임 플레이 중일 때의 키 처리 (메소드 추출)
        if (isPlayingState()) {
            handleInGameKeyPress(key);
        }
    }

    // [추가] 대기 상태(게임 오버/클리어 등)에서의 키 입력 처리
    private void handleWaitingKeyPress() {
        if (game.getCurrentState() == GameState.PLAYING_SINGLE) {
            game.requestTransition();
        }
        else if (game.getCurrentState() == GameState.PLAYING_PVP ||
                game.getCurrentState() == GameState.PLAYING_COOP) {
            game.changeState(GameState.PVP_MENU);
        }
    }

    // [추가] 게임 플레이 중 방향키/발사키 입력 처리
    private void handleInGameKeyPress(int key) {
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_DOWN) downPressed = true;
        if (key == KeyEvent.VK_SPACE) firePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (game.getLevelManager().isWaitingForKeyPress()) return;

        if (isPlayingState()) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) leftPressed = false;
            if (key == KeyEvent.VK_RIGHT) rightPressed = false;
            if (key == KeyEvent.VK_UP) upPressed = false;
            if (key == KeyEvent.VK_DOWN) downPressed = false;
            if (key == KeyEvent.VK_SPACE) firePressed = false;
        }
    }

    private boolean isPlayingState() {
        GameState state = game.getCurrentState();
        return state == GameState.PLAYING_SINGLE ||
                state == GameState.PLAYING_PVP ||
                state == GameState.PLAYING_COOP;
    }
}