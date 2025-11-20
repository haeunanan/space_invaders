package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2단계 – 목성
 * - 번개가 랜덤하게 친다는 연출 (UI에서 추가)
 * - 번개가 칠 때 잠깐 적 이동이 멈추는 느낌
 * - 난이도용 연출 스테이지
 */
public class JupiterStage extends Stage {

    // 번개 연출 관련 상태
    private boolean thunderActive = false;
    private long thunderTimer = 0;
    ;

    // 번개 동안 적 움직임을 멈추기 위해, 원래 속도를 보관
    private final Map<AlienEntity, Double> originalDx = new HashMap<>();

    public JupiterStage(Game game) {
        super(game, 2);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        double moveSpeed = 110;
        int alienRows = 4;
        double firingChance = 0.0002;
        int startY = 60;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_jupiter.gif", // 번개 테마 적
                        100 + (x * 50),
                        startY + row * 35,
                        moveSpeed,
                        firingChance
                );
                game.addEntity(alien);
            }
        }
    }

    @Override
    public void update(long delta) {
        // 번개 확률
        if (!thunderActive && Math.random() < 0.003) {
            thunderActive = true;
            thunderTimer = 400; // 0.4초 멈춤
        }

        if (thunderActive) {
            thunderTimer -= delta;

            // 적 이동 멈춤 → dx=0 처리
            for (Entity e : getEntities()) {
                if (e instanceof AlienEntity) {
                    ((AlienEntity) e).setHorizontalMovement(0);
                }
            }

            if (thunderTimer <= 0) {
                thunderActive = false;
            }
        }
    }


    @Override
    public boolean isCompleted() {
        for (Entity e : getEntities()) {
            if (e instanceof AlienEntity) return false;
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Jupiter – Lightning Storm";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_jupiter.png";
    }

    /** UI에서 번개 이펙트 그릴 때 참조할 수 있는 플래그 */
    public boolean isLightningActive() {
        return thunderActive;
    }
}

