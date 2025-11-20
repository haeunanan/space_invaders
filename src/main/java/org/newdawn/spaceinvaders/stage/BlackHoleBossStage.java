package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

public class BlackHoleBossStage extends Stage {

    private BossEntity boss;

    public BlackHoleBossStage(Game game) {
        super(game, 6);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        boss = new BossEntity(game, 350, 80);
        game.addEntity(boss);
    }

    @Override
    public void update(long delta) {
        for (Entity e : getEntities()) {
            if (e instanceof ShotEntity) {
                double curve = (e.getX() < 400) ? 10 : -10;
                e.setLocation(
                        e.getX() + (int)(curve * delta / 1000.0),
                        e.getY()
                );
            }
        }
    }

    @Override
    public boolean isCompleted() {
        // 보스가 제거되었는지 체크
        for (Entity e : getEntities()) {
            if (e instanceof BossEntity) return false;
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Black Hole – Final Boss";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_blackhole.png";
    }
}
