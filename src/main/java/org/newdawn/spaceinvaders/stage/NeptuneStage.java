// src/main/java/org/newdawn/spaceinvaders/stage/NeptuneStage.java

package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;

public class NeptuneStage extends Stage {

    // 바람 관련 변수
    private double currentWindForce = 0; // 현재 바람 세기 (음수: 왼쪽, 양수: 오른쪽)
    private boolean isWindy = false;
    private long windTimer = 0;
    private long windCycle = 3000; // 3초마다 바람 상태 변경

    public NeptuneStage(Game game) {
        super(game, 5);
    }

    @Override
    public void init() {
        background = SpriteStore.get().getSprite(getBackgroundSpriteRef()).getImage();

        double moveSpeed = 70; // 적 기본 속도 (돌진 전에는 평범하게)
        int alienRows = 5;
        double firingChance = 0.0002;
        int startY = 80;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_neptune.gif", // 해왕성 적 이미지 필요
                        100 + (x * 45),
                        startY + row * 30,
                        moveSpeed,
                        firingChance
                );
                game.addEntity(alien);
            }
        }
        // 초기화
        currentWindForce = 0;
        isWindy = false;
        windTimer = 0;
    }

    @Override
    public void update(long delta) {
        // 1. 바람 주기 관리
        windTimer += delta;
        if (windTimer > windCycle) {
            windTimer = 0;
            isWindy = !isWindy; // 바람 켜기/끄기 전환

            if (isWindy) {
                // 랜덤하게 왼쪽(-1) 또는 오른쪽(+1) 바람
                // 바람 세기: 150 정도 (플레이어 이동속도의 절반 정도)
                double direction = Math.random() < 0.5 ? -1 : 1;
                currentWindForce = direction * 150;
                System.out.println("Strong Wind Blowing! Direction: " + direction);
            } else {
                currentWindForce = 0;
                System.out.println("Wind Stopped.");
            }
        }
    }

    // 다른 클래스(Game, ShotEntity)에서 바람 세기를 가져갈 수 있게 Getter 제공
    public double getCurrentWindForce() {
        return currentWindForce;
    }

    // [아이템] 추력 부스터 (바람 무시)
    @Override
    public void activateItem() {
        if (game.getShip() instanceof ShipEntity) {
            ((ShipEntity) game.getShip()).activateBooster();
        }
    }

    public boolean isWindy() {
        return isWindy;
    }

    @Override
    public String getItemSpriteRef() {
        return "sprites/item_booster.png"; // 아이템 이미지
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
        return isWindy ? "Neptune – WARNING: STORM" : "Neptune – Calm";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_neptune.png";
    }
}