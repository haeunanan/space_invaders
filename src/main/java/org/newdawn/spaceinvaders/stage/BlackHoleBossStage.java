package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.BossEntity;
import org.newdawn.spaceinvaders.entity.DebrisEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

public class BlackHoleBossStage extends Stage {

    private BossEntity boss;
    private static final int BOSS_START_X = 350;
    private static final int BOSS_START_Y = 80;

    // 중력 설정
    private final double GRAVITY_X = 400; // 끌려갈 중심 X
    private final double GRAVITY_Y = 50;  // 끌려갈 중심 Y
    private final double PULL_STRENGTH = 30; // 끌어당기는 힘

    public BlackHoleBossStage(Game game) {
        super(game, 6);
    }

    @Override
    public void init() {
        background = SpriteStore.get()
                .getSprite(getBackgroundSpriteRef())
                .getImage();

        // 보스 생성 (체력 등은 BossEntity 내부에서 설정됨)
        boss = new org.newdawn.spaceinvaders.entity.BossEntity(game, BOSS_START_X, BOSS_START_Y);
        game.getEntityManager().addEntity(boss);
    }

    @Override
    public void update(long delta) {
        ShipEntity player = (ShipEntity) game.getShip();

        if (player != null) {
            // ==========================================
            // [기믹 1] 끝없는 인력 (Constant Pull)
            // ==========================================
            double px = player.getX();
            double py = player.getY();

            // 플레이어를 블랙홀 중심 방향으로 서서히 당김
            double dx = GRAVITY_X - px;
            double dy = GRAVITY_Y - py;
            double dist = Math.sqrt(dx*dx + dy*dy);

            if (dist > 0) {
                // 속도 벡터에 중력을 더하여 부드럽게 끌려가도록 구현
                double gravityX = (dx / dist) * PULL_STRENGTH;
                double gravityY = (dy / dist) * PULL_STRENGTH;

                // 키 입력으로 설정된 현재 속도에 중력 속도를 합산
                player.setDX(player.getDX() + gravityX);
                player.setDY(player.getDY() + gravityY);
            }

            // ==========================================
            // [기믹 2] 시간 왜곡 (Time Dilation)
            // ==========================================
            // 화면 상단(Y < 300)인 '사건의 지평선' 근처에 가면 느려짐
            if (player.getY() < 300) {
                game.getPlayerController().applySlow(2000);
            }
        }

        // ==========================================
        // [기믹 3] 스파게티화 (잔해 생성)
        // ==========================================
        if (Math.random() < 0.02) {
            spawnDebris();
        }

        // 기존 보스 탄환 궤적 굴절 로직
        for (Entity e : getEntities()) {
            if (e instanceof ShotEntity) {
                double curve = (e.getX() < 400) ? 10 : -10;
                e.setLocation(e.getX() + (int)(curve * delta / 1000.0), e.getY());
            }
        }
    }

    private void spawnDebris() {
        // 화면 좌우/하단 가장자리 랜덤 위치 계산
        int startX, startY;
        if (Math.random() < 0.5) {
            startX = (Math.random() < 0.5) ? -20 : 820; // 좌우 끝
            startY = (int) (Math.random() * 600);
        } else {
            startX = (int) (Math.random() * 800);
            startY = 620; // 하단 끝
        }

        DebrisEntity debris = new DebrisEntity(game, "sprites/debris.png", startX, startY);
        game.getEntityManager().addEntity(debris);
    }

    @Override
    public boolean isCompleted() {
        for (Entity e : getEntities()) {
            if (e instanceof BossEntity) return false;
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Event Horizon – ESCAPE GRAVITY";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_blackhole.png";
    }
}