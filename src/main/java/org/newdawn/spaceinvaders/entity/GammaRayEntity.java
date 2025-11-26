// src/main/java/org/newdawn/spaceinvaders/entity/GammaRayEntity.java

package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import java.awt.Color;
import java.awt.Graphics;

public class GammaRayEntity extends Entity {
    private long duration;
    private boolean isWarning;

    private int beamWidth = 250;
    private int beamHeight = 600;

    public GammaRayEntity(Game game, int x, int y, long duration, boolean isWarning) {
        super("sprites/null.png", x, y);
        this.game = game;
        this.duration = duration;
        this.isWarning = isWarning;
    }

    // ▼▼▼ [추가] 충돌 감지를 위해 빔의 실제 크기를 반환하도록 오버라이드 ▼▼▼
    @Override
    public int getSpriteWidth() {
        return beamWidth;
    }

    @Override
    public int getSpriteHeight() {
        return beamHeight;
    }
    // ▲▲▲ [여기까지 추가] ▲▲▲

    @Override
    public void move(long delta) {
        duration -= delta;
        if (duration <= 0) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (isWarning) return;

        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            // 무조건 죽이기 위해 여러 번 호출
            ship.takeDamage();
            ship.takeDamage();
            ship.takeDamage();
        }
    }

    @Override
    public boolean collidesWith(Entity other) {
        if (isWarning) return false;
        java.awt.Rectangle me = new java.awt.Rectangle((int)x, (int)y, beamWidth, beamHeight);
        java.awt.Rectangle him = new java.awt.Rectangle(other.getX(), other.getY(), other.getSpriteWidth(), other.getSpriteHeight());
        return me.intersects(him);
    }

    @Override
    public void draw(Graphics g, int x, int y) {
        if (isWarning) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(x, 0, beamWidth, beamHeight);
            g.setColor(Color.RED);
            g.drawRect(x, 0, beamWidth, beamHeight);
            g.drawString("WARNING: GAMMA RAY DETECTED", x + 20, 300);
        } else {
            g.setColor(new Color(200, 200, 255));
            g.fillRect(x, 0, beamWidth, beamHeight);
            g.setColor(Color.WHITE);
            g.fillRect(x + 50, 0, beamWidth - 100, beamHeight);
        }
    }
}