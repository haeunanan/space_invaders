package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class AlienShotEntity extends Entity {
    private double moveSpeed = 300;
    private Game game;
    private boolean used = false;

    public AlienShotEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        dy = moveSpeed; // 아래로만 이동
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y > 600) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어와 충돌하면 플레이어 사망 처리
        if (other instanceof ShipEntity) {
            game.removeEntity(this);
            game.notifyDeath();
        }
    }
}