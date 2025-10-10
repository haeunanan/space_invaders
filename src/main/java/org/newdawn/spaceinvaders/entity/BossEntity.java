package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class BossEntity extends Entity {
    private int health = 50; // 높은 체력 설정
    private Game game;
    private long lastFire = 0;
    private long firingInterval = 1000; // 1초마다 발사

    public BossEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        dx = -150; // 초기 이동 속도 (좌측으로)
    }

    @Override
    public void move(long delta) {
        // 좌우 벽에 닿으면 방향 전환
        if ((dx < 0) && (x < 10)) {
            dx = -dx; // 방향 반전
        }
        if ((dx > 0) && (x > 750)) {
            dx = -dx; // 방향 반전
        }
        super.move(delta);

        // 공격 로직 (주기적으로 발사)
        if (System.currentTimeMillis() - lastFire < firingInterval) {
            return;
        }
        lastFire = System.currentTimeMillis();

        // 3-way spread shot
        BossShotEntity shot1 = new BossShotEntity(game, "sprites/boss_shot.gif", getX() + 25, getY() + 50, -100);
        BossShotEntity shot2 = new BossShotEntity(game, "sprites/boss_shot.gif", getX() + 25, getY() + 50, 0);
        BossShotEntity shot3 = new BossShotEntity(game, "sprites/boss_shot.gif", getX() + 25, getY() + 50, 100);

        game.addEntity(shot1);
        game.addEntity(shot2);
        game.addEntity(shot3);
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어의 총알과 충돌했을 때
        if (other instanceof ShotEntity) {
            health--; // 체력 감소
            game.removeEntity(other); // 플레이어 총알 제거

            if (health <= 0) {
                // 보스가 죽었을 때
                game.removeEntity(this);
                game.notifyBossKilled(); // 게임에 보스가 죽었음을 알림
            }
        }
    }
}