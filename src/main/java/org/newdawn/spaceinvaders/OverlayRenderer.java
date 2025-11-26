package org.newdawn.spaceinvaders;

import java.awt.*;
import java.util.List;

public class OverlayRenderer {
    private final Game game;

    public OverlayRenderer(Game game) {
        this.game = game;
    }

    public void drawMessageOverlay(Graphics2D g2d, int width, int height) {
        if (game.getCurrentState() == GameState.PLAYING_SINGLE) {
            drawSingleMessage(g2d, width);
        } else {
            drawMultiMessage(g2d, width, height);
        }
    }

    private void drawSingleMessage(Graphics2D g2d, int width) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        centerString(g2d, game.getLevelManager().getMessage(), 0, width, 120);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        centerString(g2d, "--- RANKING ---", 0, width, 190);

        List<ScoreEntry> topScores = game.getRankingManager().getScores();
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        for (int i = 0; i < topScores.size(); i++) {
            ScoreEntry entry = topScores.get(i);
            String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
            g2d.drawString(rankString, 280, 230 + i * 25);
        }

        g2d.setColor(Color.WHITE);
        g2d.drawRect(325, 550, 150, 40);
        g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 16));
        centerString(g2d, "메뉴로 돌아가기", 0, width, 575);
    }

    private void drawMultiMessage(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 40));
        centerString(g2d, game.getLevelManager().getMessage(), 0, width, height / 2 - 50);

        g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
        centerString(g2d, "아무 키나 눌러 메뉴 화면으로 돌아가기", 0, width, height / 2 + 50);
    }

    private void centerString(Graphics2D g2d, String text, int x, int w, int y) {
        int textW = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + (w - textW) / 2, y);
    }
}
