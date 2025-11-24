package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

import java.util.List;

public class MarsStage extends Stage {

    private long elapsedTime;
    private boolean stabilizerActive = false; // 아이템 효과 활성화 여부
    private long itemTimer = 0;

    public MarsStage(Game game) {
        super(game, 1);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        // 저중력이므로 적의 기본 이동 속도도 약간 느리게 설정
        double moveSpeed = 70;
        int alienRows = 3;
        double firingChance = 0.0; // 1스테이지라 공격 안 함
        int startY = 60;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_mars.gif",
                        120 + (x * 50),
                        startY + row * 35,
                        moveSpeed,
                        firingChance
                );
                game.addEntity(alien);
            }
        }
        elapsedTime = 0;
        stabilizerActive = false;
    }

    @Override
    public void update(long delta) {
        elapsedTime += delta;

        if (stabilizerActive) {
            itemTimer -= delta;
            if (itemTimer <= 0) {
                stabilizerActive = false;
                System.out.println("Stabilizer Deactivated.");
            }
        }

        // --- 기믹: 저중력 부유 효과 (Floating) ---
        // 위치를 직접 수정하는 것이 아니라 속도(DY)를 조절하여 물리 엔진이 처리하도록 함
        double frequency = 800.0;
        double amplitude = 50.0;

        List<Entity> list = getEntities();
        for (Entity e : list) {
            if (e instanceof AlienEntity) {
                double phase = (elapsedTime / frequency) + (e.getX() * 0.005);
                e.setDY(amplitude * Math.sin(phase));
            }
        }
    }

    // --- 기믹: 탄환 속도 조절 ---
    @Override
    public double getPlayerShotVelocity() {
        // 안정제가 활성화되면 정상 속도(-300), 아니면 저중력 저항으로 느림(-150)
        if (stabilizerActive) {
            return -300;
        } else {
            return -150;
        }
    }

    @Override
    public void activateItem() {
        this.stabilizerActive = true;
        this.itemTimer = 5000; // 아이템 지속 5초
        System.out.println("Gravity Stabilizer Activated!");
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
        return stabilizerActive ? "Mars – Stabilized Gravity" : "Mars – Low Gravity Warning!";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_mars.png";
    }
}