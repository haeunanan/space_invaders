package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class BossShotEntity extends Entity {
    private double moveSpeed = 250;

    public BossShotEntity(Game game, String ref, int x, int y, double dx) {
        super(ref, x, y);
        this.game = game;
        this.dx = dx; // 수평 이동 속도 (대각선 발사를 위해)
        this.dy = moveSpeed; // 수직 이동 속도 (아래로)
    }

    @Override
    public void move(long delta) {
        super.move(delta);

        // 화면 밖으로 나가면 자동 제거
        if (y > 600) {
            game.getEntityManager().removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어와 충돌 시
        if (other instanceof ShipEntity) {
            game.getEntityManager().removeEntity(this); // 총알 제거
            ((ShipEntity) other).takeDamage();
        }
    }
}