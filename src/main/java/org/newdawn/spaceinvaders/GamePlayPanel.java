package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.stage.Stage;
import org.newdawn.spaceinvaders.stage.JupiterStage;
import org.newdawn.spaceinvaders.stage.SaturnStage;
import org.newdawn.spaceinvaders.stage.NeptuneStage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GamePlayPanel extends JPanel {
    private Game game;
    private Sprite heartFullSprite;
    private Sprite heartEmptySprite;

    // 토성 운석 효과를 위한 랜덤 객체
    private Random rand = new Random();

    public GamePlayPanel(Game game) {
        this.game = game;
        setFocusable(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(this::requestFocusInWindow);
            }
        });

        // 하트 스프라이트 미리 로드 (paintComponent 성능 최적화)
        heartFullSprite = SpriteStore.get().getSprite("sprites/heart_full.png");
        heartEmptySprite = SpriteStore.get().getSprite("sprites/heart_empty.png");
    }

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
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        /* ===============================
         * 2) 엔티티 그리기 (천왕성 방어막 포함)
         * =============================== */
        // ShipEntity의 draw() 메서드 안에 방어막 그리는 로직이 포함되어 있습니다.
        if (!game.isWaitingForKeyPress()) {
            ArrayList<Entity> entities = game.getEntities();
            for (Entity entity : entities) {
                // PVP 모드일 때 상대방 엔티티 그리기 처리 (기존 로직 유지)
                int drawX = entity.getX();
                int drawY = entity.getY();
                boolean isOpponentEntity = false;

                if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
                    String myUid = CurrentUserManager.getInstance().getUid();
                    if (entity == game.getOpponentShip() ||
                            (entity instanceof ShotEntity && myUid != null && !((ShotEntity)entity).isOwnedBy(myUid))) {
                        isOpponentEntity = true;
                    }
                }

                if (isOpponentEntity) {
                    drawY = getHeight() - entity.getY() - entity.getSpriteHeight();
                }
                entity.draw(g2d, drawX, drawY);
            }
        }

        /* ===============================
         * 3) 스테이지별 특수 효과 (목성, 토성, 해왕성)
         * =============================== */

        // [목성] 번개 효과 (시야 방해)
        if (game.getCurrentStage() instanceof JupiterStage) {
            JupiterStage js = (JupiterStage) game.getCurrentStage();
            if (js.isLightningActive()) {
                long time = System.currentTimeMillis();
                // 0.05초마다 깜빡임 (Strobe Effect)
                if ((time / 50) % 2 == 0) {
                    // 전체 화면 섬광
                    g2d.setColor(new Color(255, 255, 255, 60));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // 번개 영역 (진하게)
                    g2d.setColor(new Color(255, 255, 255, 220));
                    int type = js.getLightningType();
                    if (type == 1) { // 상단
                        g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
                    } else if (type == 2) { // 하단
                        g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
                    }
                }
            }
        }

        // [토성] 운석 고리 효과
        if (game.getCurrentStage() instanceof SaturnStage) {
            SaturnStage ss = (SaturnStage) game.getCurrentStage();
            if (ss.isReflectionActive()) { // 반사 패턴이 활성화된 경우(위험)
                int ry = ss.getRingY();
                int rh = ss.getRingThickness();
                int width = getWidth();

                // 1. 배경 띠 (반투명 갈색)
                g2d.setColor(new Color(100, 60, 20, 80));
                g2d.fillRect(0, ry, width, rh);

                // 2. 운석 입자 (지지직거리는 노이즈 효과)
                g2d.setColor(new Color(160, 120, 80, 180));
                for (int i = 0; i < 150; i++) {
                    int rx = rand.nextInt(width);
                    int r_offset = rand.nextInt(rh);
                    int size = rand.nextInt(3) + 2;
                    g2d.fillOval(rx, ry + r_offset, size, size);
                }

                // 3. 경고 문구 및 테두리
                g2d.setColor(new Color(200, 100, 50, 200));
                g2d.drawRect(0, ry, width, rh);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("WARNING: METEOR RING FIELD", 20, ry - 5);
            }
        }

        // [해왕성] 바람 효과
        if (game.getCurrentStage() instanceof NeptuneStage) {
            NeptuneStage ns = (NeptuneStage) game.getCurrentStage();
            if (ns.isWindy()) {
                drawWindEffect(g2d, ns.getCurrentWindForce());
            }
        }

        /* ===============================
         * 4) UI 오버레이 (체력, 점수, 보스바 등)
         * =============================== */

        // [플레이어 체력] 하트 그리기 (싱글/협동 모드)
        if ((game.getCurrentState() == Game.GameState.PLAYING_SINGLE || game.getCurrentState() == Game.GameState.PLAYING_COOP)
                && game.getShip() instanceof ShipEntity) {
            int hp = ((ShipEntity) game.getShip()).getCurrentHealth();
            for (int i = 0; i < 3; i++) {
                Sprite heart = (i < hp) ? heartFullSprite : heartEmptySprite;
                if (heart != null) {
                    heart.draw(g2d, 20 + (i * 35), 60);
                }
            }
        } else if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
            drawHealthHeartsPVP(g2d); // PVP 전용 하트 메서드 사용 (기존 유지)
        }

        // [보스 체력바] (블랙홀 스테이지 등)
        BossEntity boss = null;
        for (Entity e : game.getEntities()) {
            if (e instanceof BossEntity) {
                boss = (BossEntity) e;
                break;
            }
        }
        if (boss != null) {
            drawBossHealthBar(g2d, boss);
        }

        // [점수 및 코인]
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE || game.getCurrentState() == Game.GameState.PLAYING_COOP) {
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Score: " + game.score, 160, 45);

            drawCoinBadge(g2d);
        }

        // [상점]
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
            g2d.setColor(Color.gray);
            g2d.fillRect(720, 10, 60, 30);
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Shop", 732, 30);
        }

        if (game.shopOpen) {
            drawShopOverlay(g2d);
            return; // 상점 열려있으면 아래 메시지 생략
        }

        // [메시지] 스테이지 클리어 / 게임 오버 등
        if (game.isWaitingForKeyPress()) {
            drawMessageOverlay(g2d);
        }
    }

    // --- Helper Methods ---

    private void drawWindEffect(Graphics2D g2d, double windForce) {
        Sprite windSprite = SpriteStore.get().getSprite("sprites/wind_effect.png");
        if (windSprite == null) return;

        // 투명도 설정 (바람 느낌)
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

        long time = System.currentTimeMillis();
        // 바람 세기에 따라 이미지가 흘러가는 오프셋 계산
        int offsetX = (int) ((time / 5) * (windForce / 100.0)) % getWidth();

        // 화면 전체에 반복해서 그리기 (Tiling)
        int y = 0;
        while (y < getHeight()) {
            int x = offsetX - windSprite.getWidth(); // 화면 왼쪽 밖에서부터 시작
            while (x < getWidth()) {
                windSprite.draw(g2d, x, y);
                x += windSprite.getWidth() + 50; // 간격 띄우기
            }
            y += windSprite.getHeight() + 30; // 수직 간격
        }

        g2d.setComposite(originalComposite); // 투명도 복구
    }

    private void drawBossHealthBar(Graphics2D g2d, BossEntity boss) {
        int barWidth = 300;
        int barHeight = 15;
        int barX = (getWidth() - barWidth) / 2;
        int barY = 50;

        double hpRatio = (double) boss.getHP() / 2000.0; // BossEntity에 getMaxHP가 없다면 하드코딩 혹은 메서드 추가 필요
        if (hpRatio < 0) hpRatio = 0;
        if (hpRatio > 1) hpRatio = 1;

        // 배경
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // 체력바 색상
        if (hpRatio > 0.5) g2d.setColor(new Color(138, 43, 226)); // 보라
        else if (hpRatio > 0.2) g2d.setColor(new Color(255, 140, 0)); // 주황
        else g2d.setColor(Color.RED); // 빨강

        g2d.fillRect(barX, barY, (int)(barWidth * hpRatio), barHeight);

        // 테두리
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(barX, barY, barWidth, barHeight);

        // 텍스트
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String bossName = "THE VOID CORE";
        int textWidth = g2d.getFontMetrics().stringWidth(bossName);
        g2d.drawString(bossName, barX + (barWidth - textWidth) / 2, barY - 10);
    }

    private void drawCoinBadge(Graphics2D g2d) {
        int badgeX = 20, badgeY = 20;
        g2d.setColor(new Color(255,165,0));
        g2d.fillRoundRect(badgeX, badgeY, 120, 40, 20, 20);
        g2d.setColor(new Color(255,215,0));
        g2d.fillOval(badgeX+6, badgeY+6, 28, 28);
        g2d.setColor(Color.black);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("$", badgeX+13, badgeY+28);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(game.coins), badgeX+46, badgeY+26);
    }

    // PVP용 하트 그리기 (기존 코드 유지용)
    private void drawHealthHeartsPVP(Graphics2D g2d) {
        ShipEntity playerShip = (ShipEntity) game.getShip();
        ShipEntity opponentShip = (ShipEntity) game.getOpponentShip();
        if (playerShip == null || opponentShip == null) return;

        int heartSize = 32;
        int padding = 10;

        // 내 체력 (우측 상단)
        for (int i = 0; i < 3; i++) { // Max HP 3 가정
            Sprite heart = (i < playerShip.getCurrentHealth()) ? heartFullSprite : heartEmptySprite;
            if(heart != null) heart.draw(g2d, getWidth() - padding - heartSize - (i * (heartSize + 5)), padding);
        }
        // 상대 체력 (좌측 상단)
        for (int i = 0; i < 3; i++) {
            Sprite heart = (i < opponentShip.getCurrentHealth()) ? heartFullSprite : heartEmptySprite;
            if(heart != null) heart.draw(g2d, padding + (i * (heartSize + 5)), padding);
        }
    }

    private void drawShopOverlay(Graphics2D g2d) {
        int overlayX = 40;
        int overlayY = 40;
        int overlayW = 720;
        int overlayH = 520;

        g2d.setColor(new Color(40,42,45, 230));
        g2d.fillRoundRect(overlayX,overlayY,overlayW,overlayH,10,10);

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
            drawWrappedString(g2d, priceText, px+12, py+panelH-40, innerWidth, 14);
            g2d.drawString(levelText, px+panelW-60, py+panelH-20);
        }
        g2d.setColor(Color.white);
        g2d.drawString("Coins: "+game.coins, overlayX+20, overlayY+30);
    }

    private void drawMessageOverlay(Graphics2D g2d) {
        // 기존 랭킹/메시지 그리기 로직 (스니펫에서 생략되었으나 원래 파일에 있던 내용 유지)
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String message = game.getMessage();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, 120);

            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("--- RANKING ---", (getWidth() - fm.stringWidth("--- RANKING ---")) / 2, 190);

            java.util.List<ScoreEntry> topScores = game.rankingManager.getScores();
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            for (int i = 0; i < topScores.size(); i++) {
                ScoreEntry entry = topScores.get(i);
                String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
                g2d.drawString(rankString, 280, 230 + i * 25);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawRect(325, 550, 150, 40);
            g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
            String backToMenu = "메뉴로 돌아가기";
            g2d.drawString(backToMenu, (getWidth() - g2d.getFontMetrics().stringWidth(backToMenu)) / 2, 575);
        } else {
            // PVP 등 다른 모드 메시지
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 40));
            String message = game.getMessage();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2 - 50);
            g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
            String sub = "아무 키나 눌러 메뉴 화면으로 돌아가기";
            g2d.drawString(sub, (getWidth() - g2d.getFontMetrics().stringWidth(sub)) / 2, getHeight() / 2 + 50);
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
}