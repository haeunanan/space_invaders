package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import java.awt.Color;
import java.awt.Graphics;

public class GammaRayEntity extends Entity {
    private Game game;
    private long duration; // 빔 유지 시간
    private boolean isWarning; // 경고 상태인지 여부 (데미지 없음)

    // 빔 크기 설정
    private int beamWidth = 250;
    private int beamHeight = 600;

    public GammaRayEntity(Game game, int x, int y, long duration, boolean isWarning) {
        super("sprites/null.png", x, y); // 이미지는 draw에서 직접 그림
        this.game = game;
        this.duration = duration;
        this.isWarning = isWarning;
    }

    @Override
    public void move(long delta) {
        duration -= delta;
        if (duration <= 0) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 경고 상태일 때는 데미지 없음
        if (isWarning) return;

        // 빔 발사 중일 때 플레이어와 닿으면 즉사급 데미지
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            // 무적(부스터/실드) 상태가 아니면
            // 혹은 기믹상 절대 못 막는 공격으로 설정하려면:
            ship.takeDamage();
            ship.takeDamage();
            ship.takeDamage(); // 3번 연속 호출로 즉사 유도
        }
    }

    // [중요] 충돌 박스 재정의 (스프라이트 이미지가 없으므로)
    @Override
    public boolean collidesWith(Entity other) {
        if (isWarning) return false; // 경고 중엔 충돌 없음

        java.awt.Rectangle me = new java.awt.Rectangle((int)x, (int)y, beamWidth, beamHeight);
        java.awt.Rectangle him = new java.awt.Rectangle(other.getX(), other.getY(), other.getSpriteWidth(), other.getSpriteHeight());
        return me.intersects(him);
    }

    @Override
    public void draw(Graphics g) {
        // 이미지가 없으므로 직접 그리기
        if (isWarning) {
            // 경고: 붉은색 점선이나 반투명 영역
            g.setColor(new Color(255, 0, 0, 100)); // 반투명 빨강
            g.fillRect((int)x, 0, beamWidth, beamHeight);
            g.setColor(Color.RED);
            g.drawRect((int)x, 0, beamWidth, beamHeight);
            g.drawString("WARNING: GAMMA RAY DETECTED", (int)x + 20, 300);
        } else {
            // 발사: 눈부신 흰색/푸른색 빔
            g.setColor(new Color(200, 200, 255)); // 연한 파랑
            g.fillRect((int)x, 0, beamWidth, beamHeight);

            // 빔 중심부 (더 밝게)
            g.setColor(Color.WHITE);
            g.fillRect((int)x + 50, 0, beamWidth - 100, beamHeight);
        }
    }
}