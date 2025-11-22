package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.IceShardEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;

public class UranusStage extends Stage {

    public UranusStage(Game game) {
        super(game, 4);
    }

    @Override
    public void init() {
        background = SpriteStore.get().getSprite(getBackgroundSpriteRef()).getImage();

        double moveSpeed = 120;
        int alienRows = 4;
        double firingChance = 0.001; // 얼음탄 발사 확률
        int startY = 70;

        // [적 배치]
        for (int row = 0; row < alienRows; row++) {
            for (int x = 0; x < 10; x++) {
                AlienEntity alien = new AlienEntity(
                        game,
                        "sprites/alien_uranus.gif", // 천왕성 적 이미지
                        100 + (x * 50),
                        startY + row * 35,
                        moveSpeed,
                        firingChance
                );

                // 1. 크리스털 실드: 체력 2 설정
                alien.setHp(2);

                // 2. 얼음탄 사용 설정
                alien.setBulletType("ICE");

                game.addEntity(alien);
            }
        }
    }

    @Override
    public void update(long delta) {
        // [기믹] 얼음 파편 낙하 (배경에서 랜덤 생성)
        // 약 1% 확률로 프레임마다 파편 생성
        if (Math.random() < 0.01) {
            int randX = (int) (Math.random() * 750); // 랜덤 X 위치
            IceShardEntity shard = new IceShardEntity(
                    game,
                    "sprites/ice_shard.png",
                    randX,
                    -50 // 화면 위에서 시작
            );
            game.addEntity(shard);
        }
    }

    // [아이템] 히트 모듈 (방어막)
    @Override
    public void activateItem() {
        if (game.getShip() instanceof ShipEntity) {
            ((ShipEntity) game.getShip()).activateShield();
        }
    }

    @Override
    public String getItemSpriteRef() {
        return "sprites/item_heat_module.png"; // 아이템 이미지
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