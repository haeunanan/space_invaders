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

        // 보스 생성
        boss = new BossEntity(game, 350, 80);
        game.addEntity(boss);
    }

    @Override
    public void update(long delta) {
        ShipEntity player = (ShipEntity) game.getShip();

        if (player != null) {
            // ==========================================
            // 기믹 1: 끝없는 인력 (Constant Pull)
            // ==========================================
            double px = player.getX();
            double py = player.getY();

            // 플레이어를 블랙홀 중심 방향으로 서서히 당김
            double dx = GRAVITY_X - px;
            double dy = GRAVITY_Y - py;
            double dist = Math.sqrt(dx*dx + dy*dy);

            if (dist > 0) {
                // 거리에 반비례하게 당기면 너무 어려우므로, 일정 속도로 당김
                player.setLocation(
                        (int)(px + (dx / dist) * PULL_STRENGTH * delta / 1000.0),
                        (int)(py + (dy / dist) * PULL_STRENGTH * delta / 1000.0)
                );
            }

            // ==========================================
            // 기믹 2: 시간 왜곡 (Time Dilation)
            // ==========================================
            // 화면 상단(Y < 300)인 '사건의 지평선' 근처에 가면 느려짐
            if (player.getY() < 300) {
                game.applySlow(100); // 매 프레임 슬로우 갱신 (지속시간 짧게)
            }
        }

        // ==========================================
        // 기믹 3: 스파게티화 (잔해 생성)
        // ==========================================
        // 2% 확률로 화면 가장자리에서 잔해 생성
        if (Math.random() < 0.02) {
            spawnDebris();
        }

        // 기존 보스 탄환 궤적 굴절 로직 (유지)
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
        game.addEntity(debris);
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