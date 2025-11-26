// src/main/java/org/newdawn/spaceinvaders/stage/NeptuneStage.java

package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.NeptuneAlienEntity;
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
        // 배경 이미지 로드
        background = org.newdawn.spaceinvaders.SpriteStore.get().getSprite(getBackgroundSpriteRef()).getImage();

        // 초기화
        currentWindForce = 0;
        isWindy = false;
        windTimer = 0;

        // [수정] 복잡한 2중 for문 삭제 -> 부모 메서드 호출로 대체
        // 파라미터: (이미지경로, 행, 열, 시작Y, 가로간격, 세로간격, 속도, 공격확률)
        // 해왕성은 적 간격(45, 30)이 조금 좁고, 줄 수(5줄)가 많습니다.
        setupAliens("sprites/alien_neptune.gif", 5, 10, 80, 45, 30, 70, 0.0002);
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
    protected AlienEntity createAlien(String ref, int x, int y, double speed, double chance) {
        // 해왕성에서는 일반 AlienEntity 대신 NeptuneAlienEntity 생성
        return new NeptuneAlienEntity(game, ref, x, y, speed, chance);
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