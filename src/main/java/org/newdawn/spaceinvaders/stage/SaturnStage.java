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

/**
 * 3단계 – 토성
 * - 운석 고리(링)에 탄환이 닿으면 튕겨 나가는 느낌을 간단히 구현
 *   (실제 운석 Entity 없이, 특정 y 구간을 "링"으로 가정)
 * - 적들은 링 근처에서 일렬 대형 유지
 */
public class SaturnStage extends Stage {

    // "운석 고리"가 존재하는 y 구간
    private final int ringY = 250;
    private final int ringThickness = 40;
    private double slideFactor = 0.92;

    // 한 번 튕긴 탄환은 다시 튕기지 않도록 관리
    private final Set<ShotEntity> bouncedShots = new HashSet<>();

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
        double firingChance = 0.0004;
        int startY = 80;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_saturn.gif",
                        110 + (x * 45),
                        startY + row * 30,
                        moveSpeed,
                        firingChance
                );
                game.addEntity(alien);
            }
        }
    }

    @Override
    public void update(long delta) {

        // 1) 플레이어 미끄러짐(이미 구현한 부분 유지)
        ShipEntity ship = (ShipEntity) game.getShip();
        if (ship != null) {
            double dx = ship.getDX();
            dx *= slideFactor;
            ship.setDX(dx);
        }

        // 2) 운석 고리 반사 (탄환 튕김)
        List<Entity> list = getEntities();

        for (Entity e : list) {
            if (e instanceof ShotEntity) {
                ShotEntity shot = (ShotEntity) e;

                // 아래→위로 올라가는 탄환만 처리 (dy < 0)
                if (shot.getDY() < 0) {

                    // 중복 반사 방지
                    if (bouncedShots.contains(shot)) continue;

                    int y = shot.getY();

                    // 운석 고리 영역 진입 체크
                    if (y <= ringY + ringThickness && y >= ringY) {

                        // 실제 반사 처리: 수직속도 dy 반전 (탄환 하강)
                        shot.setDY(-shot.getDY());

                        // 다시 튕기지 않게 리스트에 기록
                        bouncedShots.add(shot);
                    }
                }
            }
        }

        // 3) 화면 아래로 떨어져서 삭제된 탄환은 리스트에서 제거
        bouncedShots.removeIf(shot -> !list.contains(shot));
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
        return "Saturn – Meteor Ring";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_saturn.png";
    }

    public int getRingY() {
        return ringY;
    }

    public int getRingThickness() {
        return ringThickness;
    }
}

