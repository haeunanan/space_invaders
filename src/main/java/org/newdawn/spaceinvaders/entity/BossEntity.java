// src/main/java/org/newdawn/spaceinvaders/entity/BossEntity.java

package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

public class BossEntity extends Entity {

    private int maxHp = 3000;
    private int hp = maxHp;
    private long lastShot = 0;
    private double moveSpeed = 100;
    private boolean movingLeft = true;

    // 페이즈 상태
    private int currentPhase = 1;
    private boolean isGammaRayActive = false;
    private boolean spriteChanged = false;

    public BossEntity(Game game, int x, int y) {
        super("sprites/boss.gif", x, y);
        this.game = game;
        this.hp = maxHp;
    }

    @Override
    public void move(long delta) {
        if (!isGammaRayActive) {
            if (movingLeft) {
                dx = -moveSpeed;
                if (x <= 50) movingLeft = false;
            } else {
                dx = moveSpeed;
                if (x >= 650) movingLeft = true;
            }
            super.move(delta);
        }

        updatePhase();
        executePattern(delta);
    }

    private void updatePhase() {
        double hpRatio = (double) hp / maxHp;

        if (hpRatio > 0.6) {
            currentPhase = 1;
            game.reverseControls = false;
        } else if (hpRatio > 0.3) {
            currentPhase = 2;
            game.reverseControls = true; // 조작 반전 기믹
        } else {
            currentPhase = 3;
            game.reverseControls = false;

            // 3페이즈 진입 시 이미지 교체
            if (!spriteChanged) {
                changeSprite("sprites/boss_phase3.gif");
                spriteChanged = true;
            }
        }
    }

    private void executePattern(long delta) {
        long now = System.currentTimeMillis();

        if (currentPhase == 1) {
            if (now - lastShot > 800) {
                lastShot = now;
                // 파편 뱉기 (기본 구현으로 대체 가능)
                game.addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, -100, 200));
                game.addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, 100, 200));
            }
        } else if (currentPhase == 2) {
            if (now - lastShot > 1200) {
                lastShot = now;
                // 확산탄
                game.addEntity(new ShotEntity(game, "sprites/boss_shot.png", (int)x+50, (int)y+50, 0, 150));
                game.addEntity(new ShotEntity(game, "sprites/boss_shot.png", (int)x+50, (int)y+50, -80, 100));
                game.addEntity(new ShotEntity(game, "sprites/boss_shot.png", (int)x+50, (int)y+50, 80, 100));
            }
        } else if (currentPhase == 3) {
            if (!isGammaRayActive && now - lastShot > 3000) {
                lastShot = now;
                startGammaRaySequence();
            }
        }
    }

    private void startGammaRaySequence() {
        isGammaRayActive = true;

        // 별도 스레드에서 시퀀스 실행
        new Thread(() -> {
            try {
                x = 275; // 중앙 이동

                // 경고 (2초)
                game.addEntity(new GammaRayEntity(game, 275, 0, 2000, true));
                Thread.sleep(2000);

                // 발사 (3초)
                game.addEntity(new GammaRayEntity(game, 275, 0, 3000, false));
                Thread.sleep(3000);

                isGammaRayActive = false;
                lastShot = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void changeSprite(String ref) {
        try {
            this.sprite = SpriteStore.get().getSprite(ref);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShotEntity && ((ShotEntity)other).getDY() < 0) {
            hp -= 50; // 데미지
            game.removeEntity(other);
            if (hp <= 0) {
                game.removeEntity(this);
                game.bossKilled();
                game.reverseControls = false;
            }
        }
    }

    public int getHP() { return hp; }
    public int getMaxHP() { return maxHp; }
}