package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class AlienIceShotEntity extends Entity {
    private double moveSpeed = 250; // 일반탄보다 약간 느림

    public AlienIceShotEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.dy = moveSpeed;
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        if (y > 600) {
            game.getEntityManager().removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            game.getEntityManager().removeEntity(this); // 총알 제거

            // [수정] 방어막 상태에 따른 분기 처리
            if (ship.isShieldActive()) {
                // 방어막이 있으면: 데미지 처리(방어막 파괴)만 하고 슬로우는 X
                ship.takeDamage();
                System.out.println("Ice Shot blocked by Shield!");
            } else {
                // 방어막이 없으면: 데미지 + 슬로우 효과 적용
                ship.takeDamage();
                game.getPlayerController().applySlow(2000);
            }
        }
    }
}