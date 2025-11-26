package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.stage.JupiterStage;
import org.newdawn.spaceinvaders.stage.NeptuneStage;
import org.newdawn.spaceinvaders.stage.SaturnStage;
import org.newdawn.spaceinvaders.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GamePlayPanel extends JPanel {
    // [수정 1] 직렬화 제외를 위해 transient 키워드 추가
    private transient Game game;
    private transient UIRenderer uiRenderer;

    // 토성 운석 효과를 위한 랜덤 객체
    private Random rand = new Random();

    public GamePlayPanel(Game game) {
        this.game = game;
        this.uiRenderer = new UIRenderer(game);

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

        drawBackground(g2d);
        drawEntities(g2d);
        drawStageEffects(g2d);
        uiRenderer.drawUI(g2d, getWidth(), getHeight());
    }

    private void drawBackground(Graphics2D g2d) {
        if (game.getCurrentState() == Gamestate.PLAYING_SINGLE &&
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
                int drawX = entity.getX();
                int drawY = entity.getY();
                boolean isOpponentEntity = false;

                if (game.getCurrentState() == Gamestate.PLAYING_PVP) {
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

    // [수정 2] 리팩토링된 drawStageEffects: 복잡한 로직을 개별 메서드로 분리
    private void drawStageEffects(Graphics2D g2d) {
        Stage stage = game.getCurrentStage();
        if (stage == null) return;

        if (stage instanceof JupiterStage) {
            drawJupiterEffects(g2d, (JupiterStage) stage);
        } else if (stage instanceof SaturnStage) {
            drawSaturnEffects(g2d, (SaturnStage) stage);
        } else if (stage instanceof NeptuneStage) {
            drawNeptuneEffects(g2d, (NeptuneStage) stage);
        }
    }

    // [추가] 목성 효과 그리기
    private void drawJupiterEffects(Graphics2D g2d, JupiterStage js) {
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

    // [추가] 토성 효과 그리기
    private void drawSaturnEffects(Graphics2D g2d, SaturnStage ss) {
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

    // [추가] 해왕성 효과 그리기
    private void drawNeptuneEffects(Graphics2D g2d, NeptuneStage ns) {
        if (ns.isWindy()) {
            uiRenderer.drawWindEffect(g2d, getWidth(), getHeight(), ns.getCurrentWindForce());
        }
    }
}