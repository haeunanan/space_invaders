package org.newdawn.spaceinvaders;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;

public class GameMouseListener extends MouseAdapter {
    private final Game game;

    public GameMouseListener(Game game) {
        this.game = game;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // 게임이 싱글 플레이 모드가 아니면 처리하지 않음
        if (game.getCurrentState() != GameState.PLAYING_SINGLE) {
            return;
        }

        int mx = e.getX();
        int my = e.getY();

        // 1. "메뉴로 돌아가기" 버튼 처리 (대기 상태일 때)
        if (game.isWaitingForKeyPress()) {
            if (isMenuReturnClick(mx, my)) {
                showReturnToMenuDialog();
            }
            return;
        }

        // 2. 상점 상호작용 처리
        handleShopInteraction(mx, my);
    }

    private boolean isMenuReturnClick(int mx, int my) {
        // 하드코딩된 좌표 (Game.java에 있던 로직)
        return mx >= 325 && mx <= 475 && my >= 550 && my <= 590;
    }

    private void showReturnToMenuDialog() {
        int choice = JOptionPane.showConfirmDialog(
                game.getWindowManager().getContainer(),
                "메뉴로 돌아가시겠습니까?",
                "확인",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            game.getLevelManager().resetSinglePlayerState();
            game.changeState(GameState.PVP_MENU);
        }
    }

    private void handleShopInteraction(int mx, int my) {
        // 상점이 열려있으면 구매 시도
        if (game.getShopManager().isShopOpen()) {
            game.getShopManager().handlePurchase(mx, my);
            game.updateStatsBasedOnShop(); // 스탯 갱신
            game.getWindowManager().gamePanelRepaint();
        }

        // 상점 열기/닫기 버튼 (우측 상단)
        if (isShopToggleClick(mx, my)) {
            game.getShopManager().toggleShop();
            game.getWindowManager().gamePanelRepaint();
        }
    }

    private boolean isShopToggleClick(int mx, int my) {
        int btnWidth = 60;
        int btnHeight = 30;
        // Constants.WINDOW_WIDTH (800) - btnWidth(60) - 20 = 720
        int btnX = 720;
        int btnY = 10;
        return mx >= btnX && mx <= btnX + btnWidth && my >= btnY && my <= btnY + btnHeight;
    }
}
