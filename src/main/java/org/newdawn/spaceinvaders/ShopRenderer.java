package org.newdawn.spaceinvaders;

import java.awt.*;

public class ShopRenderer {
    private final Game game;

    public ShopRenderer(Game game) {
        this.game = game;
    }

    public void drawShopButton(Graphics2D g2d) {
        int btnWidth = 60;
        int btnHeight = 30;
        int x = Constants.WINDOW_WIDTH - btnWidth - 20;
        int y = 10;

        g2d.setColor(Color.gray);
        g2d.fillRect(x, y, btnWidth, btnHeight);
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.PLAIN, 12));
        g2d.drawString("Shop", x + 12, y + 20);
    }

    public void drawShopOverlay(Graphics2D g2d) {
        int overlayW = 720;
        int overlayH = 520;
        int overlayX = (Constants.WINDOW_WIDTH - overlayW) / 2;
        int overlayY = (Constants.WINDOW_HEIGHT - overlayH) / 2;

        // 1. 배경
        g2d.setColor(new Color(40, 42, 45, 230));
        g2d.fillRoundRect(overlayX, overlayY, overlayW, overlayH, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(overlayX, overlayY, overlayW, overlayH, 10, 10);

        // 2. 제목 및 코인
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 30));
        int titleWidth = g2d.getFontMetrics().stringWidth("UPGRADE SHOP");
        g2d.drawString("UPGRADE SHOP", overlayX + (overlayW - titleWidth) / 2, overlayY + 50);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 20));
        g2d.drawString("Your Coins: " + game.getPlayerStats().getCoins(), overlayX + 30, overlayY + 45);

        // 3. 아이템 패널
        drawItemPanels(g2d, overlayX, overlayY, overlayW, overlayH);

        g2d.setColor(Color.WHITE); // 색상 초기화
    }

    private void drawItemPanels(Graphics2D g2d, int ox, int oy, int ow, int oh) {
        int pad = 20;
        int panelW = (ow - pad * 4) / 3;
        int panelH = oh - 120;
        int panelY = oy + 80;

        for (int i = 0; i < 3; i++) {
            int px = ox + pad + i * (panelW + pad);
            drawSingleItem(g2d, i, px, panelY, panelW, panelH);
        }
    }

    private void drawSingleItem(Graphics2D g2d, int index, int x, int y, int w, int h) {
        g2d.setColor(new Color(60, 63, 65));
        g2d.fillRoundRect(x, y, w, h, 10, 10);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRoundRect(x, y, w, h, 10, 10);

        String title = "";
        int level = 0;
        boolean maxed = false;
        PlayerStats stats = game.getPlayerStats();

        if (index == 0) {
            title = "Attack Speed";
            level = stats.getAttackLevel();
            maxed = level >= Constants.MAX_UPGRADES;
        } else if (index == 1) {
            title = "Move Speed";
            level = stats.getMoveLevel();
            maxed = level >= Constants.MAX_UPGRADES;
        } else if (index == 2) {
            title = "Missile Count";
            level = stats.getMissileLevel();
            maxed = level >= Constants.MAX_UPGRADES;
        }

        // 텍스트 그리기
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 18));
        centerString(g2d, title, x, w, y + 40);

        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.PLAIN, 16));
        centerString(g2d, "Level: " + level + " / " + Constants.MAX_UPGRADES, x, w, y + 100);

        // 가격 및 상태
        String priceText = maxed ? "MAXED" : ("Cost: " + Constants.UPGRADE_COST);
        if (maxed) g2d.setColor(Color.GREEN);
        else if (stats.getCoins() < Constants.UPGRADE_COST) g2d.setColor(new Color(255, 80, 80));
        else g2d.setColor(Color.YELLOW);

        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 20));
        centerString(g2d, priceText, x, w, y + h - 40);
    }

    private void centerString(Graphics2D g2d, String text, int x, int w, int y) {
        int textW = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + (w - textW) / 2, y);
    }
}