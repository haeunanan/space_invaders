package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class NeptuneAlienEntity extends AlienEntity {
    private boolean isDashing = false;
    private long dashTimer = 0;

    public NeptuneAlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(game, ref, x, y, moveSpeed, firingChance);
    }

    @Override
    public void move(long delta) {
        // 해왕성 특수 패턴: 대시 로직
        if (alive) {
            if (isDashing) {
                // 대시 중: 속도를 3배 빠르게
                y += (moveSpeed * 3) * delta / 1000.0;
                dashTimer -= delta;
                if (dashTimer <= 0) {
                    isDashing = false;
                }
            } else if (Math.random() < 0.0002) {
                // 평상시: 아주 낮은 확률로 대시 시작
                isDashing = true;
                dashTimer = 500;
            }
        }

        // 부모의 이동 로직(좌우 이동, 발사 등) 수행
        super.move(delta);
    }
}
