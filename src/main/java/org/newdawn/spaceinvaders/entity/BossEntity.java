package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;

public class BossEntity extends Entity {

    private int maxHp = 3000;
    private int hp = maxHp;
    private long lastShot = 0;
    private double moveSpeed = 100;
    private boolean movingLeft = true;
    private float gammaRayTimer = 0;
    private int gammaRayStep = 0; // 0:대기, 1:경고, 2:발사

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
        // ... (이동 및 패턴 로직은 변경 없음) ...
        if (isGammaRayActive) {
            gammaRayTimer += delta;
            if (gammaRayStep == 1 && gammaRayTimer > 2000) {
                gammaRayStep = 2;
                gammaRayTimer = 0;
                game.getEntityManager().addEntity(new GammaRayEntity(game, 275, 0, 3000, false));
            }
            else if (gammaRayStep == 2 && gammaRayTimer > 3000) {
                isGammaRayActive = false;
                gammaRayStep = 0;
                lastShot = System.currentTimeMillis();
            }
            return;
        }

        // 보스 좌우 이동 로직 (기존 코드 누락분 보완)
        if (movingLeft) {
            x -= (delta * moveSpeed) / 1000;
            if (x < 100) movingLeft = false;
        } else {
            x += (delta * moveSpeed) / 1000;
            if (x > 600) movingLeft = true;
        }

        updatePhase();
        executePattern(delta);
    }

    private void updatePhase() {
        double hpRatio = (double) hp / maxHp;

        if (hpRatio > 0.6) {
            currentPhase = 1;
            game.getPlayerController().setReverseControls(false);
        } else if (hpRatio > 0.3) {
            currentPhase = 2;
            game.getPlayerController().setReverseControls(true);
        } else {
            currentPhase = 3;
            game.getPlayerController().setReverseControls(false);

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
                game.getEntityManager().addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, -100, 200));
                game.getEntityManager().addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, 100, 200));
            }
        } else if (currentPhase == 2) {
            if (now - lastShot > 1200) {
                lastShot = now;
                game.getEntityManager().addEntity(new ShotEntity(game, Constants.BOSS_SHOT_SPRITE, (int)x+50, (int)y+50, 0, 150));
                game.getEntityManager().addEntity(new ShotEntity(game, Constants.BOSS_SHOT_SPRITE, (int)x+50, (int)y+50, -80, 100));
                game.getEntityManager().addEntity(new ShotEntity(game, Constants.BOSS_SHOT_SPRITE, (int)x+50, (int)y+50, 80, 100));
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
        gammaRayStep = 1;
        gammaRayTimer = 0;
        x = 275;
        game.getEntityManager().addEntity(new GammaRayEntity(game, 275, 0, 2000, true));
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
            hp -= 50;
            game.getEntityManager().removeEntity(other);
            if (hp <= 0) {
                game.getEntityManager().removeEntity(this);
                game.getLevelManager().bossKilled();
                game.getPlayerController().setReverseControls(false);
            }
        }
    }

    public int getHP() { return hp; }
    public int getMaxHP() { return maxHp; }
}