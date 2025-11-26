package org.newdawn.spaceinvaders;

import java.awt.Graphics2D;

public class UIRenderer {
    private final Game game;
    private final ShopRenderer shopRenderer;
    private final OverlayRenderer overlayRenderer;
    private final HUDRenderer hudRenderer;

    public UIRenderer(Game game) {
        this.game = game;
        this.shopRenderer = new ShopRenderer(game);
        this.overlayRenderer = new OverlayRenderer(game);
        this.hudRenderer = new HUDRenderer(game);
    }

    public void drawUI(Graphics2D g2d, int width, int height) {
        // 1. 기본 HUD (체력, 보스, 점수)
        hudRenderer.drawHUD(g2d, width);

        // 2. 상점 및 오버레이
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_SINGLE) {
            shopRenderer.drawShopButton(g2d);
        }

        if (game.getShopManager().isShopOpen()) {
            shopRenderer.drawShopOverlay(g2d);
        } else if (game.getLevelManager().isWaitingForKeyPress()) {
            overlayRenderer.drawMessageOverlay(g2d, width, height);
        }
    }

    // [삭제] drawWindEffect, isSingleOrCoop 등은 모두 각 렌더러 내부로 이동시킵니다.
}