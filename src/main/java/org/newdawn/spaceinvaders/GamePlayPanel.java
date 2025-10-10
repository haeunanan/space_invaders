package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePlayPanel extends JPanel {
    private Game game; // 게임 데이터에 접근하기 위한 Game 객체

    public GamePlayPanel(Game game) {
        this.game = game;
    }

    // 이 패널이 다시 그려져야 할 때마다 호출됩니다. (repaint()가 불릴 때)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 그리기 기능을 먼저 호출

        Graphics2D g2d = (Graphics2D) g;

        // 배경을 검게 칠합니다.
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Game 객체로부터 엔티티 리스트를 가져와 그립니다.
        ArrayList<Entity> entities = game.getEntities();
        for (Entity entity : entities) {
            entity.draw(g2d);
        }

        // "Press any key" 같은 메시지를 그립니다.
        if (game.isWaitingForKeyPress()) {
            g2d.setColor(Color.white);
            String message = game.getMessage();
            g2d.drawString(message, (800 - g2d.getFontMetrics().stringWidth(message)) / 2, 250);
            g2d.drawString("Press any key", (800 - g2d.getFontMetrics().stringWidth("Press any key")) / 2, 300);
        }

        // 코인뱃지 왼쪽상단
        int badgeX = 20;
        int badgeY = 20;
        int badgeW = 120;
        int badgeH = 40;
        g2d.setColor(new Color(255,165,0)); // orange
        g2d.fillRoundRect(badgeX,badgeY,badgeW,badgeH,20,20);
        // 코인 아이콘
        g2d.setColor(new Color(255,215,0)); // gold
        g2d.fillOval(badgeX+6,badgeY+6,28,28);
        g2d.setColor(Color.black);
        g2d.drawString("★", badgeX+12, badgeY+28);
        g2d.setColor(Color.black);
        g2d.drawString(String.valueOf(game.coins), badgeX+46, badgeY+26);

        // 오른쪽 상단 샵버튼
        g2d.setColor(Color.gray);
        g2d.fillRect(720,10,60,30);
        g2d.setColor(Color.white);
        g2d.drawString("Shop",732,30);

        // if 샵 오픈 >> overlay with three panels 표시
        if (game.shopOpen) {
            int overlayX = 40;
            int overlayY = 40;
            int overlayW = 720;
            int overlayH = 520;
            // dark rounded background
            g2d.setColor(new Color(40,42,45));
            g2d.fillRoundRect(overlayX,overlayY,overlayW,overlayH,10,10);

            // top coin badge inside overlay (mirror) optional - skip

            // 3개의 light panel
            int pad = 20;
            int panelW = (overlayW - pad*4)/3;
            int panelH = overlayH - 120;
            int panelY = overlayY + 60;
            g2d.setColor(new Color(220,220,220));
            g2d.setFont(g2d.getFont().deriveFont(14f));
            String[] titles = {"공격 속도 증가","이동 속도 증가","미사일 개수 증가"};
            String[] desc = {
                    "공격 속도가 증가합니다",
                    "플레이어의 이동속도가 증가합니다",
                    "한 번에 발사할 수 있는 미사일의 개수를 하나 추가합니다"
            };

            for (int i=0;i<3;i++) {
                int px = overlayX + pad + i*(panelW + pad);
                int py = panelY;
                // determine if this upgrade is maxed or affordable
                boolean maxed = false;
                int level = 0;
                if (i==0) { level = game.attackLevel; maxed = game.attackLevel>=game.MAX_UPGRADES; }
                if (i==1) { level = game.moveLevel; maxed = game.moveLevel>=game.MAX_UPGRADES; }
                if (i==2) { level = game.missileLevel; maxed = game.missileLevel>=game.MAX_UPGRADES; }


                g2d.fillRect(px,py,panelW,panelH);
                // draw wrapped title and description
                g2d.setColor(Color.black);
                int textX = px + 12;
                int textY = py + 20;
                int innerWidth = panelW - 24;
                g2d.setFont(g2d.getFont().deriveFont(16f));
                drawWrappedString(g2d, titles[i], textX, textY, innerWidth, 20);
                g2d.setFont(g2d.getFont().deriveFont(12f));
                drawWrappedString(g2d, desc[i], textX, textY + 36, innerWidth, 16);
                // price and level display
                String levelText = "Lv "+level;
                String priceText = maxed ? "MAX" : ("Price: "+game.UPGRADE_COST);
                // color price red if not enough coins
                if (!maxed && game.coins < game.UPGRADE_COST) g2d.setColor(Color.red);
                else g2d.setColor(Color.darkGray);
                g2d.setFont(g2d.getFont().deriveFont(12f));
                drawWrappedString(g2d, priceText, px+12, py+panelH-40, innerWidth, 14);
                g2d.drawString(levelText, px+panelW-60, py+panelH-20);
                g2d.setColor(new Color(220,220,220));
            }
            // 왼쪽 상단에 코인 개수 표시
            g2d.setColor(Color.white);
            g2d.drawString("Coins: "+game.coins, overlayX+20, overlayY+30);
        }
    }
    private void drawWrappedString(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.length() == 0) return;
        java.awt.FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (int i=0;i<words.length;i++) {
            String word = words[i];
            String test = line.length() == 0 ? word : line + " " + word;
            int width = fm.stringWidth(test);
            if (width > maxWidth) {
                // 현재 라인에서부터 새로 그리기
                g.drawString(line.toString(), x, curY);
                curY += lineHeight;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), x, curY);
        }
    }
}