package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class IceShardEntity extends Entity {
    private double moveSpeed = 200; // 낙하 속도

    public IceShardEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.dy = moveSpeed; // 아래로 떨어짐
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 밖으로 나가면 제거
        if (y > 600) {
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;
            game.removeEntity(this); // 파편 제거

            // [수정] 방어막 상태에 따른 분기 처리
            if (ship.isShieldActive()) {
                // 방어막이 있으면: 방어막만 깨지고 슬로우 X
                ship.takeDamage();
                System.out.println("Ice Shard blocked by Shield!");
            } else {
                // 방어막이 없으면: 데미지 + 슬로우
                ship.takeDamage();
                game.applySlow(2000);
            }
        }
    }
}