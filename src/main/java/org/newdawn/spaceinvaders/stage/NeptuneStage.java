package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

/**
 * 5단계 – 해왕성
 * - 바람(흐름) 효과: 화면 전체가 좌우로 살짝 끌리는 느낌
 * - 현재 구조에서는 dx를 직접 조작하기 어렵기 때문에,
 *   나중에 Entity에 getter를 추가하거나 GameLoop에서 별도 처리를 할 예정.
 *   지금은 배치/테마 위주로 구성.
 */
public class NeptuneStage extends Stage {
    private double wind = 0.15;
    public NeptuneStage(Game game) {
        super(game, 5);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        double moveSpeed = 140;
        int alienRows = 5;
        double firingChance = 0.0008;
        int startY = 80;

        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_neptune.gif",
                        100 + (x * 45),
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
        for (Entity e : getEntities()) {
            e.setLocation(e.getX() + (int)wind, e.getY());
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
        return "Neptune – Deep Current";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_neptune.png";
    }
}

