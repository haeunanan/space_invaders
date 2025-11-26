package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.stage.JupiterStage;
import org.newdawn.spaceinvaders.stage.NeptuneStage;
import org.newdawn.spaceinvaders.stage.SaturnStage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GamePlayPanel extends JPanel {
    private Game game;
    private UIRenderer uiRenderer; // [New] UI 렌더러

    // 토성 운석 효과를 위한 랜덤 객체
    private Random rand = new Random();

    public GamePlayPanel(Game game) {
        this.game = game;
        this.uiRenderer = new UIRenderer(game); // [New] 초기화

        setFocusable(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(this::requestFocusInWindow);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 스테이지 배경 그리기
        drawBackground(g2d);

        // 2. 엔티티(우주선, 적, 총알) 그리기
        drawEntities(g2d);

        // 3. 스테이지별 특수 효과 (목성 번개, 토성 고리, 해왕성 바람)
        drawStageEffects(g2d);

        // 4. UI 및 오버레이 그리기 (UIRenderer에 위임!) -> [핵심 리팩토링]
        uiRenderer.drawUI(g2d, getWidth(), getHeight());
    }

    // --- 리팩토링된 내부 헬퍼 메서드들 ---

    private void drawBackground(Graphics2D g2d) {
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
    }

    private void drawEntities(Graphics2D g2d) {
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
    }

    private void drawStageEffects(Graphics2D g2d) {
        // [목성] 번개 효과
        if (game.getCurrentStage() instanceof JupiterStage) {
            JupiterStage js = (JupiterStage) game.getCurrentStage();
            if (js.isLightningActive()) {
                long time = System.currentTimeMillis();
                if ((time / 50) % 2 == 0) {
                    g2d.setColor(new Color(255, 255, 255, 60));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(new Color(255, 255, 255, 220));
                    int type = js.getLightningType();
                    if (type == 1) g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
                    else if (type == 2) g2d.fillRect(0, getHeight() / 2, getWidth(), getHeight() / 2);
                }
            }
        }

        // [토성] 운석 고리 효과
        if (game.getCurrentStage() instanceof SaturnStage) {
            SaturnStage ss = (SaturnStage) game.getCurrentStage();
            if (ss.isReflectionActive()) {
                int ry = ss.getRingY();
                int rh = ss.getRingThickness();
                int width = getWidth();

                g2d.setColor(new Color(100, 60, 20, 80));
                g2d.fillRect(0, ry, width, rh);

                g2d.setColor(new Color(160, 120, 80, 180));
                for (int i = 0; i < 150; i++) {
                    int rx = rand.nextInt(width);
                    int r_offset = rand.nextInt(rh);
                    int size = rand.nextInt(3) + 2;
                    g2d.fillOval(rx, ry + r_offset, size, size);
                }

                g2d.setColor(new Color(200, 100, 50, 200));
                g2d.drawRect(0, ry, width, rh);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("WARNING: METEOR RING FIELD", 20, ry - 5);
            }
        }

        // [해왕성] 바람 효과 (UIRenderer 사용)
        if (game.getCurrentStage() instanceof NeptuneStage) {
            NeptuneStage ns = (NeptuneStage) game.getCurrentStage();
            if (ns.isWindy()) {
                // UI 렌더러에게 바람 효과 그리기 요청
                uiRenderer.drawWindEffect(g2d, getWidth(), getHeight(), ns.getCurrentWindForce());
            }
        }
    }
}