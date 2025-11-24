package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;


/**
 * 4단계 – 천왕성
 * - 얼음 테마
 * - 플레이어 이동이 "살짝 미끄러지는" 느낌을 주고 싶지만,
 *   현재 ShipEntity / Game 구조를 크게 건드리지 않기 위해
 *   여기서는 적/탄 속도만 약간 느리게 해서 "얼어붙은 느낌" 위주로 표현.
 */
public class UranusStage extends Stage {
    private double slideFactor = 0.92;
    public UranusStage(Game game) {
        super(game, 4);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        double moveSpeed = 120;
        int alienRows = 5;
        double firingChance = 0.0005;
        int startY = 70;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_uranus.gif",
                        90 + (x * 50),
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
        ShipEntity ship = (ShipEntity) game.getShip();
        if (ship != null) {
            double dx = ship.getDX();
            dx *= slideFactor;
            ship.setHorizontalMovement(dx);
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
        return "Uranus – Frozen Orbit";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_uranus.png";
    }
}

