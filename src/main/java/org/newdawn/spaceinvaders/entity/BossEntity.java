package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

public class BossEntity extends Entity {

    private int hp = 2000;  // 보스 체력
    private long lastShot = 0;  // 공격 딜레이 관리
    private long shotInterval = 1000; // 1초 간격 기본 공격
    private double moveSpeed = 150;
    private boolean movingLeft = true;

    public BossEntity(Game game, int x, int y) {
        super("sprites/boss_blackhole.png", x, y);
        this.game = game;
    }

    @Override
    public void move(long delta) {
        // 좌우 왕복 이동
        if (movingLeft) {
            dx = -moveSpeed;
            if (x <= 50) movingLeft = false;
        } else {
            dx = moveSpeed;
            if (x >= 700) movingLeft = true;
        }

        super.move(delta);

        // 공격 패턴 호출
        firePattern(delta);
    }

    private void firePattern(long delta) {
        long now = System.currentTimeMillis();

        if (now - lastShot < shotInterval) {
            return;
        }
        lastShot = now;

        // 기본 3-way 공격
        game.addEntity(new ShotEntity(
                game,
                "sprites/boss_shot.png",
                (int)(x + 30),
                (int)(y + 40),
                0, 300
        ));
        game.addEntity(new ShotEntity(
                game,
                "sprites/boss_shot.png",
                (int)(x + 30),
                (int)(y + 40),
                -150, 300
        ));
        game.addEntity(new ShotEntity(
                game,
                "sprites/boss_shot.png",
                (int)(x + 30),
                (int)(y + 40),
                150, 300
        ));
    }

    /**
     * 보스에게 데미지를 입히는 메소드입니다.
     * 체력이 0 이하가 되면 보스를 제거하고 게임 승리 처리를 합니다.
     * @param damage 입힐 데미지 양
     */
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            game.removeEntity(this);
            game.bossKilled();
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity && other.getY() < this.y) {
            takeDamage(50);

            // 탄은 제거
            game.removeEntity(other);

            if (hp <= 0) {
                game.removeEntity(this);
                game.bossKilled();
            }
        }
    }

    public int getHP() {
        return hp;
    }
}
