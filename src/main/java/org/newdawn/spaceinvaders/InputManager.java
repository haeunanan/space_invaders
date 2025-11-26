package org.newdawn.spaceinvaders;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 게임의 키보드 입력을 전담하여 관리하는 클래스
 * (Refactoring: Game.java에서 추출함)
 */
public class InputManager extends KeyAdapter {

    private Game game; // 게임 상태 확인을 위한 참조

    // 키 상태 저장 변수들 (Game.java에서 이동해옴)
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean firePressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    public InputManager(Game game) {
        this.game = game;
    }

    // 각 키의 상태를 외부에서 확인할 수 있는 Getter 메서드
    public boolean isLeftPressed() { return leftPressed; }
    public boolean isRightPressed() { return rightPressed; }
    public boolean isFirePressed() { return firePressed; }
    public boolean isUpPressed() { return upPressed; }
    public boolean isDownPressed() { return downPressed; }

    // 상태 초기화 메서드 (스테이지 변경 시 등)
    public void reset() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        firePressed = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }

        if (game.isWaitingForKeyPress()) {
            if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
                game.requestTransition(); // Game 클래스에 이 메서드 추가 필요
            }
            else if (game.getCurrentState() == Game.GameState.PLAYING_PVP ||
                    game.getCurrentState() == Game.GameState.PLAYING_COOP) {
                game.changeState(Game.GameState.PVP_MENU);
            }
            return;
        }

        // 게임 플레이 중 키 입력 처리
        if (isPlayingState()) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT) rightPressed = true;
            if (key == KeyEvent.VK_UP) upPressed = true;
            if (key == KeyEvent.VK_DOWN) downPressed = true;
            if (key == KeyEvent.VK_SPACE) firePressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (game.isWaitingForKeyPress()) return;

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
        Game.GameState state = game.getCurrentState();
        return state == Game.GameState.PLAYING_SINGLE ||
                state == Game.GameState.PLAYING_PVP ||
                state == Game.GameState.PLAYING_COOP;
    }
}