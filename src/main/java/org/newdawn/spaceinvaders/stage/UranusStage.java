package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.*;

public class UranusStage extends Stage {

    public UranusStage(Game game) {
        super(game, 4);
    }

    @Override
    public void init() {
        // 배경 이미지 로드
        background = org.newdawn.spaceinvaders.SpriteStore.get().getSprite(getBackgroundSpriteRef()).getImage();

        // [수정] 복잡한 for문 삭제 -> 부모 클래스의 setupAliens 메서드 한 줄로 대체!
        // 파라미터: (이미지경로, 행, 열, 시작Y, 가로간격, 세로간격, 속도, 공격확률)
        setupAliens("sprites/alien_uranus.gif", 4, 10, 70, 50, 35, 120, 0.001);
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
    @Override
    protected void customizeAlien(org.newdawn.spaceinvaders.entity.AlienEntity alien) {
        // 1. 크리스털 실드: 체력 2 설정
        alien.setHp(2);

        // 2. 얼음탄 사용 설정
        alien.setBulletType("ICE");
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
    protected AlienEntity createAlien(String ref, int x, int y, double speed, double chance) {
        // 천왕성에서는 일반 AlienEntity 대신 UranusAlienEntity 생성
        return new UranusAlienEntity(game, ref, x, y, speed, chance);
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