package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

import java.util.List;

public class MarsStage extends Stage {

    private long elapsedTime;

    public MarsStage(Game game) {
        super(game, 1);   // Stage(Game, int) 생성자에 정확히 맞춰 호출
    }

    @Override
    public void init() {
        // --- 1) 화성 배경 로드 ---
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        // --- 2) 외계인 배치 ---
        double moveSpeed = 90;
        int alienRows = 3;
        double firingChance = 0.0;
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
    }

    @Override
    public void update(long delta) {
        elapsedTime += delta;

        double phase = elapsedTime / 400.0; // 천천히 움직임
        List<Entity> list = getEntities();

        for (Entity e : list) {
            if (e instanceof AlienEntity) {
                int baseY = e.getY();
                int offset = (int)(Math.sin(phase + baseY * 0.01) * 3); // ±3px 흔들기
                e.setLocation(e.getX(), baseY + offset);
            }
        }
    }


    @Override
    public boolean isCompleted() {
        for (Entity e : getEntities()) {
            if (e instanceof AlienEntity)
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Mars – Low Gravity";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_mars.png";
    }
}
