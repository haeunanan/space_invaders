package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.stage.JupiterStage; // 목성 스테이지 참조를 위해 필요

import javax.swing.*;
import java.awt.*;

public class GamePlayPanel extends JPanel {
    private Game game; // 게임 데이터에 접근하기 위한 Game 객체

    public GamePlayPanel(Game game) {
        this.game = game;
    }

    // 이 패널이 다시 그려져야 할 때마다 호출됩니다. (repaint()가 불릴 때)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        /* ===============================
         * 1) 스테이지 배경 그리기
         * =============================== */
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE &&
                game.getCurrentStage() != null &&
                game.getCurrentStage().getBackground() != null) {

            g2d.drawImage(
                    game.getCurrentStage().getBackground(),
                    0, 0, getWidth(), getHeight(),
                    null
            );
        } else {
            // 기본 검정 배경
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        /* ===============================
         * 2) 점수 표시
         * =============================== */
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + game.score, 160, 45);

        /* ===============================
         * 3) 엔티티 그리기
         * =============================== */
        if (!game.isWaitingForKeyPress()) {
            for (Entity entity : game.getEntities()) {
                entity.draw(g2d);
            }
        }

        /* ===============================
         * [추가] 목성 스테이지 번개 효과 그리기 (시야 방해)
         * =============================== */
        // 현재 스테이지가 목성(JupiterStage)인지 확인
        if (game.getCurrentStage() instanceof JupiterStage) {
            JupiterStage js = (JupiterStage) game.getCurrentStage();

            // 번개가 활성화된 상태일 때만 그리기
            if (js.isLightningActive()) {

                // 리얼함을 위해 50ms(0.05초)마다 깜빡거리게 만듭니다 (Strobe Effect)
                long time = System.currentTimeMillis();

                // 짝수 타이밍에만 그려서 깜빡임 구현
                if ((time / 50) % 2 == 0) {

                    // 1. 전체 화면에 옅은 섬광을 주어 번쩍이는 느낌 강조 (Alpha 60)
                    g2d.setColor(new Color(255, 255, 255, 60));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // 2. 시야 방해 영역 (더 진하게 설정: Alpha 220)
                    g2d.setColor(new Color(255, 255, 255, 220));

                    int type = js.getLightningType();
                    if (type == 1) {
                        // 상단 절반 가리기
                        g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
                    } else if (type == 2) {
                        // 하단 절반 가리기
                        g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
                    }
                }
            }
        }
        /* ===============================
         * [추가] 토성 스테이지 운석 고리 그리기
         * =============================== */
        /* ===============================
         * [수정] 토성 스테이지 운석 고리 그리기 (시각 효과 강화)
         * =============================== */
        if (game.getCurrentStage() instanceof org.newdawn.spaceinvaders.stage.SaturnStage) {
            org.newdawn.spaceinvaders.stage.SaturnStage ss =
                    (org.newdawn.spaceinvaders.stage.SaturnStage) game.getCurrentStage();

            // 반사 효과가 활성화되어 있을 때만 그리기
            if (ss.isReflectionActive()) {
                int ry = ss.getRingY();
                int rh = ss.getRingThickness();
                int width = getWidth();

                // 1. 배경 띠 (반투명 갈색)
                g2d.setColor(new Color(100, 60, 20, 80));
                g2d.fillRect(0, ry, width, rh);

                // 2. 운석 입자 효과 (랜덤한 점들 그리기)
                // 매 프레임마다 랜덤하게 그리면 '지지직'거리는 노이즈 효과처럼 보여 역동적입니다.
                g2d.setColor(new Color(160, 120, 80, 180)); // 운석 색상

                // 고리 내부에 약 100개의 작은 점(운석)을 무작위로 찍습니다.
                java.util.Random rand = new java.util.Random();
                // (주의: 성능을 위해 Random 객체는 멤버 변수로 빼는 게 정석이지만, 여기선 간단히 처리)

                for (int i = 0; i < 150; i++) {
                    int rx = rand.nextInt(width);
                    int r_offset = rand.nextInt(rh);
                    int size = rand.nextInt(3) + 2; // 2~4 크기의 점

                    g2d.fillOval(rx, ry + r_offset, size, size);
                }

                // 3. 경계선 및 경고 문구
                g2d.setColor(new Color(200, 100, 50, 200)); // 밝은 주황색
                g2d.drawRect(0, ry, width, rh);

                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("WARNING: METEOR RING FIELD", 20, ry - 5);
            }
        }

        /* ===============================
         * 4) SHOP/코인 UI
         * =============================== */
        // --- 코인 뱃지 ---
        int badgeX = 20, badgeY = 20;
        int badgeW = 120, badgeH = 40;
        g2d.setColor(new Color(255,165,0));
        g2d.fillRoundRect(badgeX,badgeY,badgeW,badgeH,20,20);

        g2d.setColor(new Color(255,215,0));
        g2d.fillOval(badgeX+6,badgeY+6,28,28);

        g2d.setColor(Color.black);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("$", badgeX+13, badgeY+28);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(game.coins), badgeX+46, badgeY+26);

        // --- SHOP 버튼 ---
        g2d.setColor(Color.gray);
        g2d.fillRect(720,10,60,30);
        g2d.setColor(Color.white);
        g2d.drawString("Shop",732,30);

        /* ===============================
         * 5) SHOP UI (활성화 시)
         * =============================== */
        if (game.shopOpen) {
            drawShopOverlay(g2d);
            return;
        }

        /* ===============================
         * 6) "Press any key" 메시지
         * =============================== */
        if (game.isWaitingForKeyPress()) {
            drawStageClearMessage(g2d);
        }
    }

    // --- Helper Methods (기존 유지) ---

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

    private void drawShopOverlay(Graphics2D g2d) {
        int overlayX = 40;
        int overlayY = 40;
        int overlayW = 720;
        int overlayH = 520;

        g2d.setColor(new Color(40,42,45, 230));
        g2d.fillRoundRect(overlayX, overlayY, overlayW, overlayH, 10, 10);

        int pad = 20;
        int panelW = (overlayW - pad*4)/3;
        int panelH = overlayH - 120;
        int panelY = overlayY + 60;

        String[] titles = {"공격 속도 증가", "이동 속도 증가", "미사일 개수 증가"};
        String[] desc = {
                "공격 속도가 증가합니다",
                "플레이어의 이동속도가 증가합니다",
                "한 번에 발사할 수 있는 미사일의 개수를 하나 추가합니다"
        };

        for (int i = 0; i < 3; i++) {
            int px = overlayX + pad + i*(panelW + pad);
            int py = panelY;

            g2d.setColor(new Color(220,220,220));
            g2d.fillRect(px, py, panelW, panelH);

            g2d.setColor(Color.black);
            int textX = px + 12;
            int textY = py + 20;
            int innerWidth = panelW - 24;

            g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
            drawWrappedString(g2d, titles[i], textX, textY, innerWidth, 20);

            g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            drawWrappedString(g2d, desc[i], textX, textY + 36, innerWidth, 16);

            boolean maxed = false;
            int level = 0;
            if (i == 0) { level = game.attackLevel; maxed = game.attackLevel >= game.MAX_UPGRADES; }
            if (i == 1) { level = game.moveLevel; maxed = game.moveLevel >= game.MAX_UPGRADES; }
            if (i == 2) { level = game.missileLevel; maxed = game.missileLevel >= game.MAX_UPGRADES; }

            String levelText = "Lv " + level;
            String priceText = maxed ? "MAX" : ("Price: " + game.UPGRADE_COST);

            if (!maxed && game.coins < game.UPGRADE_COST) g2d.setColor(Color.red);
            else g2d.setColor(Color.darkGray);

            g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            drawWrappedString(g2d, priceText, px + 12, py + panelH - 40, innerWidth, 14);
            g2d.drawString(levelText, px + panelW - 60, py + panelH - 20);
        }

        g2d.setColor(Color.white);
        g2d.drawString("Coins: " + game.coins, overlayX + 20, overlayY + 30);
    }

    private void drawStageClearMessage(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        String text = game.getMessage();
        FontMetrics fm = g2d.getFontMetrics();

        g2d.drawString(
                text,
                (800 - fm.stringWidth(text)) / 2,
                200
        );

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String pressKey = "Press any key to continue";
        g2d.drawString(
                pressKey,
                (800 - g2d.getFontMetrics().stringWidth(pressKey)) / 2,
                260
        );
    }
}