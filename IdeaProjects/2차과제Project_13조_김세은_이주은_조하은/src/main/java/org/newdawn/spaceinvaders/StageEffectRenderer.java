package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.stage.*;
import java.awt.*;
import java.util.Random;

public class StageEffectRenderer {
    private final Random rand = new Random();
    private final Sprite windSprite;

    public StageEffectRenderer() {
        windSprite = SpriteStore.get().getSprite("sprites/wind_effect.png");
    }

    public void drawStageEffects(Graphics2D g2d, Stage stage, int width, int height) {
        // if-else if 대신 다형성이나 간단한 타입 체크 사용 권장되나,
        // 여기서는 메서드를 바로 리턴하는 방식으로 복잡도를 줄임
        if (stage instanceof JupiterStage) {
            drawJupiterEffects(g2d, (JupiterStage) stage, width, height);
            return;
        }
        if (stage instanceof SaturnStage) {
            drawSaturnEffects(g2d, (SaturnStage) stage, width);
            return;
        }
        if (stage instanceof NeptuneStage) {
            drawNeptuneEffects(g2d, (NeptuneStage) stage, width, height);
        }
    }

    private void drawJupiterEffects(Graphics2D g2d, JupiterStage js, int w, int h) {
        if (!js.isLightningActive()) return; // Guard Clause

        long time = System.currentTimeMillis();
        if ((time / 50) % 2 != 0) return; // 번개 깜빡임 타이밍 아님

        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(255, 255, 255, 220));

        if (js.getLightningType() == 1) {
            g2d.fillRect(0, 0, w, h / 2);
        } else if (js.getLightningType() == 2) {
            g2d.fillRect(0, h / 2, w, h / 2);
        }
    }

    private void drawSaturnEffects(Graphics2D g2d, SaturnStage ss, int width) {
        if (!ss.isReflectionActive()) return; // Guard Clause

        int ry = ss.getRingY();
        int rh = ss.getRingThickness();

        drawRingBackground(g2d, ry, rh, width);
        drawRingParticles(g2d, ry, rh, width); // 반복문 분리
        drawRingWarning(g2d, ry, width, rh);
    }

    private void drawRingBackground(Graphics2D g2d, int ry, int rh, int width) {
        g2d.setColor(new Color(100, 60, 20, 80));
        g2d.fillRect(0, ry, width, rh);
    }

    private void drawRingParticles(Graphics2D g2d, int ry, int rh, int width) {
        g2d.setColor(new Color(160, 120, 80, 180));
        for (int i = 0; i < 150; i++) {
            int rx = rand.nextInt(width);
            int r_offset = rand.nextInt(rh);
            int size = rand.nextInt(3) + 2;
            g2d.fillOval(rx, ry + r_offset, size, size);
        }
    }

    private void drawRingWarning(Graphics2D g2d, int ry, int width, int rh) {
        g2d.setColor(new Color(200, 100, 50, 200));
        g2d.drawRect(0, ry, width, rh);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("WARNING: METEOR RING FIELD", 20, ry - 5);
    }

    private void drawNeptuneEffects(Graphics2D g2d, NeptuneStage ns, int w, int h) {
        if (!ns.isWindy() || windSprite == null) return;

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

        drawWindParticles(g2d, ns.getCurrentWindForce(), w, h);

        g2d.setComposite(originalComposite);
    }

    private void drawWindParticles(Graphics2D g2d, double windForce, int w, int h) {
        long time = System.currentTimeMillis();
        int offsetX = (int) ((time / 5) * (windForce / 100.0)) % w;

        for (int y = 0; y < h; y += windSprite.getHeight() + 30) {
            for (int x = offsetX - windSprite.getWidth(); x < w; x += windSprite.getWidth() + 50) {
                windSprite.draw(g2d, x, y);
            }
        }
    }
}