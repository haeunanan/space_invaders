package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;

public class GamePlayPanel extends JPanel {
    private Game game; // 게임 데이터에 접근하기 위한 Game 객체

    private Sprite heartFullSprite;
    private Sprite heartEmptySprite;

    public GamePlayPanel(Game game) {
        this.game = game;

        setFocusable(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(this::requestFocusInWindow);
            }
        });

        heartFullSprite = SpriteStore.get().getSprite("sprites/heart_full.png");
        heartEmptySprite = SpriteStore.get().getSprite("sprites/heart_empty.png");
    }

    // 이 패널이 다시 그려져야 할 때마다 호출됩니다. (repaint()가 불릴 때)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 그리기 기능을 먼저 호출
        Graphics2D g2d = (Graphics2D) g;

        // --- 1. 배경 그리기 (가장 먼저) ---
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // --- 2. 게임 엔티티(우주선, 총알 등) 그리기 ---
        // 'Press any key' 상태가 아닐 때만 게임 속 객체들을 그립니다.
        if (!game.isWaitingForKeyPress()) {
            ArrayList<Entity> entities = game.getEntities();
            for (Entity entity : entities) {
                // PVP 모드일 때 상대방 엔티티는 Y좌표를 뒤집어서 그립니다.
                int drawX = entity.getX();
                int drawY = entity.getY();

                boolean isOpponentEntity = false;
                if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
                    String myUid = CurrentUserManager.getInstance().getUid();
                    if (entity == game.getOpponentShip() ||
                            (entity instanceof ShotEntity && myUid != null && !((ShotEntity)entity).getOwnerUid().equals(myUid))) {
                        isOpponentEntity = true;
                    }
                }

                if (isOpponentEntity) {
                    drawY = getHeight() - entity.getY() - entity.getSpriteHeight();
                }
                entity.draw(g2d, drawX, drawY);
            }
        }

        // --- 3. 게임 상태별 HUD(Head-Up Display) 그리기 ---
        Game.GameState currentState = game.getCurrentState();

        // '혼자하기' 모드일 때만 점수, 코인, 상점 UI를 그립니다.
        if (currentState == Game.GameState.PLAYING_SINGLE){
            // 점수 UI
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Score: " + game.score, 160, 45);

            // 코인뱃지 UI
            int badgeX = 20;
            int badgeY = 20;
            g2d.setColor(new Color(255,165,0)); // orange
            g2d.fillRoundRect(badgeX, badgeY, 120, 40, 20, 20);
            g2d.setColor(new Color(255,215,0)); // gold
            g2d.fillOval(badgeX+6, badgeY+6, 28, 28);
            g2d.setColor(Color.black);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2d.drawString("$", badgeX+13, badgeY+28);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(String.valueOf(game.coins), badgeX+46, badgeY+26);

            // 상점 버튼 UI
            g2d.setColor(Color.gray);
            g2d.fillRect(720,10,60,30);
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Shop",732,30);
        }
        // PVP 모드일 때만 하트 체력 UI를 그립니다.
        else if (game.getCurrentState() == Game.GameState.PLAYING_PVP){
            drawHealthHearts(g2d);
        }

        // --- 4. 오버레이 UI(상점 또는 랭킹) 그리기 (가장 마지막) ---
        if (game.shopOpen) {
            int overlayX = 40;
            int overlayY = 40;
            int overlayW = 720;
            int overlayH = 520;
            // dark rounded background
            g2d.setColor(new Color(40,42,45, 230));
            g2d.fillRoundRect(overlayX,overlayY,overlayW,overlayH,10,10);

            // 3개의 light panel
            int pad = 20;
            int panelW = (overlayW - pad*4)/3;
            int panelH = overlayH - 120;
            int panelY = overlayY + 60;

            String[] titles = {"공격 속도 증가","이동 속도 증가","미사일 개수 증가"};
            String[] desc = {
                    "공격 속도가 증가합니다",
                    "플레이어의 이동속도가 증가합니다",
                    "한 번에 발사할 수 있는 미사일의 개수를 하나 추가합니다"
            };

            for (int i=0;i<3;i++) {
                int px = overlayX + pad + i*(panelW + pad);
                int py = panelY;

                g2d.setColor(new Color(220,220,220));
                g2d.fillRect(px,panelY,panelW,panelH);

                g2d.setColor(Color.black);
                int textX = px + 12;
                int textY = py + 20;
                int innerWidth = panelW - 24;

                g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
                drawWrappedString(g2d, titles[i], textX, textY, innerWidth, 20);

                g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
                drawWrappedString(g2d, desc[i], textX, textY + 36, innerWidth, 16);

                // price and level display
                boolean maxed = false;
                int level = 0;
                if (i==0) { level = game.attackLevel; maxed = game.attackLevel>=game.MAX_UPGRADES; }
                if (i==1) { level = game.moveLevel; maxed = game.moveLevel>=game.MAX_UPGRADES; }
                if (i==2) { level = game.missileLevel; maxed = game.missileLevel>=game.MAX_UPGRADES; }

                String levelText = "Lv "+level;
                String priceText = maxed ? "MAX" : ("Price: "+game.UPGRADE_COST);

                if (!maxed && game.coins < game.UPGRADE_COST) g2d.setColor(Color.red);
                else g2d.setColor(Color.darkGray);

                g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
                drawWrappedString(g2d, priceText, px+12, py+panelH-40, innerWidth, 14);
                g2d.drawString(levelText, px+panelW-60, py+panelH-20);
            }
            // 왼쪽 상단에 코인 개수 표시
            g2d.setColor(Color.white);
            g2d.drawString("Coins: "+game.coins, overlayX+20, overlayY+30);
        }

        // 게임이 '대기 상태'일 때 랭킹/메시지 화면을 그립니다.
        else if (game.isWaitingForKeyPress()) {
            if (currentState == Game.GameState.PLAYING_SINGLE) {
                g2d.setColor(Color.white);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String message = game.getMessage();
                // FontMetrics를 g2d에서 다시 가져와야 정확한 계산이 가능합니다.
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(message, (800 - fm.stringWidth(message)) / 2, 120);

                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("--- RANKING ---", (800 - fm.stringWidth("--- RANKING ---")) / 2, 190);

                // Game 객체에서 RankingManager를 통해 점수를 가져온다.
                java.util.List<ScoreEntry> topScores = game.rankingManager.getScores();
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
                for (int i = 0; i < topScores.size(); i++) {
                    ScoreEntry entry = topScores.get(i);
                    String rankString = String.format("%2d. %-10s %7d", (i + 1), entry.getPlayerName(), entry.getScore());
                    g2d.drawString(rankString, 280, 230 + i * 25);
                }

                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                int pressKeyX = (800 - g2d.getFontMetrics().stringWidth("Press any key to continue")) / 2;
                g2d.drawString("Press any key to continue", pressKeyX - 5, 520);

                // ▼▼▼ '메뉴로 돌아가기' 버튼 그리기 추가 ▼▼▼
                g2d.setColor(Color.WHITE);
                g2d.drawRect(325, 550, 150, 40); // 버튼 테두리
                g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
                String backToMenu = "메뉴로 돌아가기";
                int strWidth = g2d.getFontMetrics().stringWidth(backToMenu);
                g2d.drawString(backToMenu, (800 - strWidth) / 2, 575);
            }
            else if (currentState == Game.GameState.PLAYING_PVP) {
                g2d.setColor(Color.white);
                g2d.setFont(new Font("Malgun Gothic", Font.BOLD, 40));
                String message = game.getMessage();
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2 - 50);

                g2d.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
                String subMessage = "아무 키나 눌러 메뉴 화면으로 돌아가기";
                fm = g2d.getFontMetrics();
                g2d.drawString(subMessage, (getWidth() - fm.stringWidth(subMessage)) / 2, getHeight() / 2 + 50);
            }
        }
    }

    private void drawHealthHearts(Graphics2D g2d) {
        ShipEntity playerShip = (ShipEntity) game.getShip();
        ShipEntity opponentShip = (ShipEntity) game.getOpponentShip(); // 나중에 추가될 상대방 우주선
        if (opponentShip == null) return;

        if (playerShip == null) return;

        int playerHealth = playerShip.getCurrentHealth();
        int maxHealth = playerShip.getMaxHealth();
        int opponentHealth = opponentShip.getCurrentHealth();
        int heartSize = 32; // 하트 이미지 크기
        int padding = 10;   // 화면 가장자리와의 간격

        // 내 체력 (오른쪽 상단)
        for (int i = 0; i < maxHealth; i++) {
            Sprite heart = (i < playerHealth) ? heartFullSprite : heartEmptySprite;
            int x = getWidth() - padding - heartSize - (i * (heartSize + 5));
            heart.draw(g2d, x, padding);
        }
        // 상대 체력 (왼쪽 상단)
        for (int i = 0; i < maxHealth; i++) {
            Sprite heart = (i < opponentHealth) ? heartFullSprite : heartEmptySprite;
            int x = padding + (i * (heartSize + 5));
            heart.draw(g2d, x, padding);
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