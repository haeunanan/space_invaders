package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;

import java.awt.*;
import java.util.List;

/**
 * 게임의 UI(HUD, 오버레이 등) 렌더링을 전담하는 클래스
 * (Refactoring: GamePlayPanel에서 분리됨)
 */
public class UIRenderer {
    private Game game;
    private Sprite heartFullSprite;
    private Sprite heartEmptySprite;
    private Sprite windSprite;

    public UIRenderer(Game game) {
        this.game = game;
        // 스프라이트 미리 로드
        heartFullSprite = SpriteStore.get().getSprite("sprites/heart_full.png");
        heartEmptySprite = SpriteStore.get().getSprite("sprites/heart_empty.png");
        windSprite = SpriteStore.get().getSprite("sprites/wind_effect.png");
    }

    // 메인 그리기 메서드
    public void drawUI(Graphics2D g2d, int width, int height) {
        // 1. 플레이어 체력 (싱글/협동/PVP 분기 처리)
        drawHealth(g2d, width);

        // 2. 보스 체력바
        drawBossHealth(g2d, width);

        // 3. 점수 및 코인 (싱글/협동)
        if (isSingleOrCoop()) {
            drawScoreAndCoins(g2d);
        }

        // 4. 상점 버튼 (싱글)
        if (game.getCurrentState() == Gamestate.PLAYING_SINGLE) {
            drawShopButton(g2d);
        }

        // 5. 상점 오버레이 또는 메시지
        if (game.shopOpen) {
            drawShopOverlay(g2d);
        } else if (game.isWaitingForKeyPress()) {
            drawMessageOverlay(g2d, width, height);
        }
    }

    public void drawWindEffect(Graphics2D g2d, int width, int height, double windForce) {
        if (windSprite == null) return;

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

        long time = System.currentTimeMillis();
        int offsetX = (int) ((time / 5) * (windForce / 100.0)) % width;

        int y = 0;
        while (y < height) {
            int x = offsetX - windSprite.getWidth();
            while (x < width) {
                windSprite.draw(g2d, x, y);
                x += windSprite.getWidth() + 50;
            }
            y += windSprite.getHeight() + 30;
        }
        g2d.setComposite(originalComposite);
    }

    // --- 내부 헬퍼 메서드들 ---

    private void drawHealth(Graphics2D g2d, int width) {
        if (isSingleOrCoop() && game.getShip() instanceof ShipEntity) {
            int hp = ((ShipEntity) game.getShip()).getCurrentHealth();
            drawHeartRow(g2d, 20, 60, hp);
        } else if (game.getCurrentState() == Gamestate.PLAYING_PVP) {
            ShipEntity player = (ShipEntity) game.getShip();
            ShipEntity opponent = (ShipEntity) game.getOpponentShip();
            if (player == null || opponent == null) return;

            // 내 체력 (우측)
            int startX = width - 10 - 32;
            for (int i = 0; i < 3; i++) {
                Sprite heart = (i < player.getCurrentHealth()) ? heartFullSprite : heartEmptySprite;
                if(heart != null) heart.draw(g2d, startX - (i * (32 + 5)), 10);
            }
            // 상대 체력 (좌측)
            drawHeartRow(g2d, 10, 10, opponent.getCurrentHealth());
        }
    }

    private void drawHeartRow(Graphics2D g2d, int x, int y, int currentHp) {
        for (int i = 0; i < 3; i++) {
            Sprite heart = (i < currentHp) ? heartFullSprite : heartEmptySprite;
            if (heart != null) {
                heart.draw(g2d, x + (i * 35), y);
            }
        }
    }

    private void drawBossHealth(Graphics2D g2d, int width) {
        BossEntity boss = null;
        for (Entity e : game.getEntities()) {
            if (e instanceof BossEntity) {
                boss = (BossEntity) e;
                break;
            }
        }
        if (boss == null) return;

        int barWidth = 300;
        int barHeight = 15;
        int barX = (width - barWidth) / 2;
        int barY = 50;

        double hpRatio = (double) boss.getHP() / boss.getMaxHP();
        if (hpRatio < 0) hpRatio = 0;
        if (hpRatio > 1) hpRatio = 1;

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        if (hpRatio > 0.5) g2d.setColor(new Color(138, 43, 226));
        else if (hpRatio > 0.2) g2d.setColor(new Color(255, 140, 0));
        else g2d.setColor(Color.RED);

        g2d.fillRect(barX, barY, (int)(barWidth * hpRatio), barHeight);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(barX, barY, barWidth, barHeight);

        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 16));
        String bossName = "THE VOID CORE";
        int textWidth = g2d.getFontMetrics().stringWidth(bossName);
        g2d.drawString(bossName, barX + (barWidth - textWidth) / 2, barY - 10);
    }

    private void drawScoreAndCoins(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 16));
        g2d.drawString("Score: " + game.playerStats.getScore(), 160, 45);

        // Coin Badge
        int badgeX = 20, badgeY = 20;
        g2d.setColor(new Color(255,165,0));
        g2d.fillRoundRect(badgeX, badgeY, 120, 40, 20, 20);
        g2d.setColor(new Color(255,215,0));
        g2d.fillOval(badgeX+6, badgeY+6, 28, 28);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("$", badgeX+13, badgeY+28);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 12));
        g2d.drawString(String.valueOf(game.playerStats.getCoins()), badgeX+46, badgeY+26);
    }

    private void drawShopButton(Graphics2D g2d) {
        // [수정] 매직 넘버 제거: 화면 너비 기준으로 버튼 위치 계산 (우측 상단)
        int btnWidth = 60;
        int btnHeight = 30;
        int x = Constants.WINDOW_WIDTH - btnWidth - 20; // 오른쪽에서 20px 띔
        int y = 10;

        g2d.setColor(Color.gray);
        g2d.fillRect(x, y, btnWidth, btnHeight);
        g2d.setColor(Color.white);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.PLAIN, 12));
        g2d.drawString("Shop", x + 12, y + 20);
    }

    // UIRenderer.java 내부의 drawShopOverlay 메서드를 이 코드로 통째로 교체하세요.
    private void drawShopOverlay(Graphics2D g2d) {
        int overlayW = 720;
        int overlayH = 520;
        // 화면 중앙 정렬
        int overlayX = (Constants.WINDOW_WIDTH - overlayW) / 2;
        int overlayY = (Constants.WINDOW_HEIGHT - overlayH) / 2;

        // 1. 상점 배경 그리기 (반투명 검정)
        g2d.setColor(new Color(40, 42, 45, 230));
        g2d.fillRoundRect(overlayX, overlayY, overlayW, overlayH, 10, 10);

        // 테두리
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(overlayX, overlayY, overlayW, overlayH, 10, 10);

        // 2. 상점 제목
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 30));
        int titleWidth = g2d.getFontMetrics().stringWidth("UPGRADE SHOP");
        g2d.drawString("UPGRADE SHOP", overlayX + (overlayW - titleWidth) / 2, overlayY + 50);

        // 3. 보유 코인 표시 (좌측 상단)
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 20));
        g2d.drawString("Your Coins: " + game.playerStats.getCoins(), overlayX + 30, overlayY + 45);

        // 4. 아이템 패널 3개 그리기
        int pad = 20;
        int panelW = (overlayW - pad * 4) / 3; // 3등분
        int panelH = overlayH - 120;
        int panelY = overlayY + 80;

        for (int i = 0; i < 3; i++) {
            int px = overlayX + pad + i * (panelW + pad);
            int py = panelY;

            // 각 아이템 패널 배경
            g2d.setColor(new Color(60, 63, 65));
            g2d.fillRoundRect(px, py, panelW, panelH, 10, 10);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRoundRect(px, py, panelW, panelH, 10, 10);

            // 아이템 정보 설정
            String title = "";
            int level = 0;
            boolean maxed = false;

            if (i == 0) {
                title = "Attack Speed";
                level = game.playerStats.getAttackLevel();
                maxed = level >= Constants.MAX_UPGRADES;
            } else if (i == 1) {
                title = "Move Speed";
                level = game.playerStats.getMoveLevel();
                maxed = level >= Constants.MAX_UPGRADES;
            } else if (i == 2) {
                title = "Missile Count";
                level = game.playerStats.getMissileLevel();
                maxed = level >= Constants.MAX_UPGRADES;
            }

            // (1) 아이템 이름
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 18));
            int itemTitleW = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, px + (panelW - itemTitleW) / 2, py + 40);

            // (2) 레벨 표시
            g2d.setFont(new Font(Constants.FONT_ARIAL, Font.PLAIN, 16));
            String levelText = "Level: " + level + " / " + Constants.MAX_UPGRADES;
            int levelW = g2d.getFontMetrics().stringWidth(levelText);
            g2d.drawString(levelText, px + (panelW - levelW) / 2, py + 100);

            // (3) 가격 또는 MAX 표시
            String priceText = maxed ? "MAXED" : ("Cost: " + Constants.UPGRADE_COST);

            // 색상 처리: 만렙이면 초록, 돈 부족하면 빨강, 살 수 있으면 노랑
            if (maxed) {
                g2d.setColor(Color.GREEN);
            } else if (game.playerStats.getCoins() < Constants.UPGRADE_COST) {
                g2d.setColor(new Color(255, 80, 80)); // 연한 빨강
            } else {
                g2d.setColor(Color.YELLOW);
            }

            g2d.setFont(new Font(Constants.FONT_ARIAL, Font.BOLD, 20));
            int priceW = g2d.getFontMetrics().stringWidth(priceText);
            g2d.drawString(priceText, px + (panelW - priceW) / 2, py + panelH - 40);
        }

        // 색상 초기화
        g2d.setColor(Color.WHITE);
    }

    private void drawMessageOverlay(Graphics2D g2d, int width, int height) {
        if (game.getCurrentState() == Gamestate.PLAYING_SINGLE) {
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String message = game.getMessage();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(message, (width - fm.stringWidth(message)) / 2, 120);

            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("--- RANKING ---", (width - fm.stringWidth("--- RANKING ---")) / 2, 190);

            List<ScoreEntry> topScores = game.rankingManager.getScores();
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            for (int i = 0; i < topScores.size(); i++) {
                ScoreEntry entry = topScores.get(i);
                String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
                g2d.drawString(rankString, 280, 230 + i * 25);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawRect(325, 550, 150, 40);
            g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 16));
            String backToMenu = "메뉴로 돌아가기";
            g2d.drawString(backToMenu, (width - g2d.getFontMetrics().stringWidth(backToMenu)) / 2, 575);
        } else {
            g2d.setColor(Color.white);
            g2d.setFont(new Font(Constants.FONT_MALGUN, Font.BOLD, 40));
            String message = game.getMessage();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(message, (width - fm.stringWidth(message)) / 2, height / 2 - 50);
            g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
            String sub = "아무 키나 눌러 메뉴 화면으로 돌아가기";
            g2d.drawString(sub, (width - g2d.getFontMetrics().stringWidth(sub)) / 2, height / 2 + 50);
        }
    }

    private void drawWrappedString(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        if (text == null || text.length() == 0) return;
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
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

    private boolean isSingleOrCoop() {
        return game.getCurrentState() == Gamestate.PLAYING_SINGLE ||
                game.getCurrentState() == Gamestate.PLAYING_COOP;
    }
}