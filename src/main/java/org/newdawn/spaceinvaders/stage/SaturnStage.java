package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SaturnStage extends Stage {

    // 운석 고리(링) 설정
    private final int ringY = 250;
    private final int ringThickness = 40;

    // 아이템 효과 상태 (반사 제어기)
    private boolean reflectionControlActive = false;

    // 한 번 튕긴 탄환 관리 (중복 튕김 방지)
    private final Set<ShotEntity> bouncedShots = new HashSet<>();

    private long itemTimer = 0;

    public SaturnStage(Game game) {
        super(game, 3);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        double moveSpeed = 130;
        int alienRows = 4;
        double firingChance = 0.0004; // 적당한 공격 빈도
        int startY = 80;

        // 토성 적 이미지 설정 (파일이 없다면 alien.gif 사용)
        String spriteRef = "sprites/alien_saturn.gif";

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        spriteRef,
                        110 + (x * 45),
                        startY + row * 30,
                        moveSpeed,
                        firingChance
                );
                game.addEntity(alien);
            }
        }

        reflectionControlActive = false;
        bouncedShots.clear();
    }

    @Override
    public void update(long delta) {
        if (reflectionControlActive) {
            itemTimer -= delta;
            if (itemTimer <= 0) {
                reflectionControlActive = false;
                System.out.println("Reflection Control Deactivated.");
            }
        }
        // [기믹 1] 운석 고리 반사 시스템
        // 아이템(반사 제어기)을 먹으면 반사 효과가 사라짐!
        if (!reflectionControlActive) {
            handleMeteorRingReflection();
        }

        // 삭제된 탄환은 관리 목록에서도 제거 (메모리 관리)
        List<Entity> currentEntities = getEntities();
        bouncedShots.removeIf(shot -> !currentEntities.contains(shot));
    }

    // handleMeteorRingReflection 메서드 수정
    // handleMeteorRingReflection 메소드 전체 교체
    private void handleMeteorRingReflection() {
        List<Entity> list = getEntities();

        for (Entity e : list) {
            if (e instanceof ShotEntity) {
                ShotEntity shot = (ShotEntity) e;

                // 이미 튕긴 탄환은 패스
                if (bouncedShots.contains(shot)) continue;

                int shotY = shot.getY();
                int shotHeight = shot.getSpriteHeight(); // 탄환의 높이 고려
                double dy = shot.getDY();

                // [수정] 정밀한 충돌 체크 (AABB 방식)
                // 탄환의 '아랫부분'이 고리 '윗부분'보다 아래에 있고,
                // 탄환의 '윗부분'이 고리 '아랫부분'보다 위에 있으면 충돌로 간주
                boolean hitRing = (shotY + shotHeight >= ringY) && (shotY <= ringY + ringThickness);

                if (hitRing) {
                    // 1. 플레이어 탄환 (위로 이동 중) -> 아래로 튕김
                    if (dy < 0) {
                        shot.setDY(-dy);
                        // 끼임 방지를 위해 고리 밖으로 강제 이동
                        shot.setLocation(shot.getX(), ringY + ringThickness + 1);
                        bouncedShots.add(shot);
                    }
                    // 2. 적 탄환 (아래로 이동 중) -> 위로 튕김
                    else if (dy > 0) {
                        shot.setDY(-dy);
                        // 끼임 방지를 위해 고리 밖으로 강제 이동
                        shot.setLocation(shot.getX(), ringY - shotHeight - 1);
                        bouncedShots.add(shot);
                    }
                }
            }
        }
    }

    // [기믹 2] 아이템 획득 시 호출됨: 반사 효과 제거
    @Override
    public void activateItem() {
        this.reflectionControlActive = true;
        this.itemTimer = 2000; // 2초 설정
        System.out.println("Reflection Controller Activated! The ring is now safe.");
    }
    // [추가] 토성 스테이지 전용 아이템 이미지
    @Override
    public String getItemSpriteRef() {
        return "sprites/item_reflection.png";
        // *주의: 해당 이미지 파일을 resources/sprites 폴더에 꼭 넣어주세요!
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
        return reflectionControlActive ? "Saturn – Ring Stabilized" : "Saturn – Meteor Ring Hazard";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_saturn.png";
    }

    // GamePlayPanel에서 고리를 그리기 위해 필요
    public int getRingY() { return ringY; }
    public int getRingThickness() { return ringThickness; }
    public boolean isReflectionActive() { return !reflectionControlActive; }
}