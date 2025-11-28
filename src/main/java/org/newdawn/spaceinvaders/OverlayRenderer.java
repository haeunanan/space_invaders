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
        String msg = game.getLevelManager().getMessage();
        centerString(g2d, msg, 0, width, 120);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        centerString(g2d, "--- RANKING ---", 0, width, 190);

        List<ScoreEntry> topScores = game.getRankingManager().getScores();
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        for (int i = 0; i < topScores.size(); i++) {
            ScoreEntry entry = topScores.get(i);
            String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
            g2d.drawString(rankString, 280, 230 + i * 25);
        }

        // [수정] 안내 문구 동적 변경
        String actionText;
        if (msg.contains("die") || msg.contains("got you") || msg.contains("Lose")) {
            actionText = "재시도하려면 아무 키나 누르세요";
        } else if (msg.contains("Clear") || msg.contains("Win")) {
            actionText = "다음 스테이지로 (아무 키나 누르세요)";
        } else if (msg.contains("Waiting")) {
            actionText = "호스트를 기다리는 중...";
        } else {
            actionText = "아무 키나 눌러 계속하기";
        }

        g2d.setColor(Color.WHITE);
        g2d.drawRect(250, 550, 300, 40); // 박스 크기 조정
        g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 16));
        centerString(g2d, actionText, 0, width, 575);
    }

    private void drawMultiMessage(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 40));
        String msg = game.getLevelManager().getMessage();
        centerString(g2d, msg, 0, width, height / 2 - 50);

        g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));

        // [수정] 안내 문구 동적 변경
        String actionText;
        if (msg.contains("Waiting")) {
            actionText = "호스트가 다음 스테이지를 시작할 때까지 대기 중...";
        } else if (msg.contains("die") || msg.contains("Lose")) {
            actionText = "아무 키나 눌러 재시도";
        } else if (msg.contains("Win") || msg.contains("Clear")) {
            // 호스트인지 확인
            if (game.getNetworkManager().amIPlayer1()) {
                actionText = "아무 키나 눌러 다음 스테이지로 이동";
            } else {
                actionText = "호스트의 입력을 대기 중...";
            }
        } else {
            actionText = "아무 키나 눌러 계속하기";
        }

        centerString(g2d, actionText, 0, width, height / 2 + 50);
    }

    private void centerString(Graphics2D g2d, String text, int x, int w, int y) {
        int textW = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + (w - textW) / 2, y);
    }
}