package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;

import java.awt.*;

public class HUDRenderer {
    private final Game game;
    private final Sprite heartFullSprite;
    private final Sprite heartEmptySprite;

    public HUDRenderer(Game game) {
        this.game = game;
        heartFullSprite = SpriteStore.get().getSprite("sprites/heart_full.png");
        heartEmptySprite = SpriteStore.get().getSprite("sprites/heart_empty.png");
    }

    public void drawHealth(Graphics2D g2d, int width) {
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_PVP) {
            drawPvpHealth(g2d, width);
        } else if (game.getShip() instanceof ShipEntity) {
            // 싱글/협동
            int hp = ((ShipEntity) game.getShip()).getCurrentHealth();
            drawHeartRow(g2d, 20, 60, hp);
        }
    }

    private void drawPvpHealth(Graphics2D g2d, int width) {
        ShipEntity player = (ShipEntity) game.getShip();
        ShipEntity opponent = (ShipEntity) game.getOpponentShip();
        if (player == null || opponent == null) return;

        // 내 체력 (우측)
        int startX = width - 10 - 32;
        for (int i = 0; i < 3; i++) {
            Sprite heart = (i < player.getCurrentHealth()) ? heartFullSprite : heartEmptySprite;
            if (heart != null) heart.draw(g2d, startX - (i * (32 + 5)), 10);
        }
        // 상대 체력 (좌측)
        drawHeartRow(g2d, 10, 10, opponent.getCurrentHealth());
    }

    private void drawHeartRow(Graphics2D g2d, int x, int y, int currentHp) {
        for (int i = 0; i < 3; i++) {
            Sprite heart = (i < currentHp) ? heartFullSprite : heartEmptySprite;
            if (heart != null) heart.draw(g2d, x + (i * 35), y);
        }
    }

    public void drawBossHealth(Graphics2D g2d, int width) {
        BossEntity boss = findBossEntity();
        if (boss == null) return;

        int barWidth = 300;
        int barHeight = 15;
        int barX = (width - barWidth) / 2;
        int barY = 50;

        double hpRatio = (double) boss.getHP() / boss.getMaxHP();
        hpRatio = Math.max(0, Math.min(1, hpRatio));

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        if (hpRatio > 0.5) g2d.setColor(new Color(138, 43, 226));
        else if (hpRatio > 0.2) g2d.setColor(new Color(255, 140, 0));
        else g2d.setColor(Color.RED);

        g2d.fillRect(barX, barY, (int) (barWidth * hpRatio), barHeight);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(barX, barY, barWidth, barHeight);

        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 16));
        String bossName = "THE VOID CORE";
        int textWidth = g2d.getFontMetrics().stringWidth(bossName);
        g2d.drawString(bossName, barX + (barWidth - textWidth) / 2, barY - 10);
    }

    private BossEntity findBossEntity() {
        for (Entity e : game.getEntityManager().getEntities()) {
            if (e instanceof BossEntity) return (BossEntity) e;
        }
        return null;
    }
    public void drawHUD(Graphics2D g2d, int width) {
        drawHealth(g2d, width);
        drawBossHealth(g2d, width);

        if (isSingleOrCoop()) {
            drawScoreAndCoins(g2d);
        }
    }
    private boolean isSingleOrCoop() {
        return game.getGameStateManager().getCurrentState() == GameState.PLAYING_SINGLE ||
                game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP;
    }

    public void drawScoreAndCoins(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 16));
        g2d.drawString("Score: " + game.getPlayerStats().getScore(), 160, 45);

        int badgeX = 20, badgeY = 20;
        g2d.setColor(new Color(255, 165, 0));
        g2d.fillRoundRect(badgeX, badgeY, 120, 40, 20, 20);
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(badgeX + 6, badgeY + 6, 28, 28);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("$", badgeX + 13, badgeY + 28);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 12));
        g2d.drawString(String.valueOf(game.getPlayerStats().getCoins()), badgeX + 46, badgeY + 26);
    }
}
