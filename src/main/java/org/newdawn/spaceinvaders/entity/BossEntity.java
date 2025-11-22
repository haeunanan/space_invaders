package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class BossEntity extends Entity {

    private int maxHp = 3000; // 보스 총 체력
    private int hp = maxHp;

    private long lastShot = 0;
    private long patternTimer = 0;

    private Game game;
    private double moveSpeed = 100;
    private boolean movingLeft = true;

    // 페이즈 상태
    private int currentPhase = 1;
    private boolean isGammaRayActive = false;

    public BossEntity(Game game, int x, int y) {
        super("sprites/boss.gif", x, y);
        this.game = game;
        this.hp = maxHp;
    }

    @Override
    public void move(long delta) {
        // 1. 보스 이동 (좌우 왕복) - 감마선 쏠 때는 멈춤
        if (!isGammaRayActive) {
            if (movingLeft) {
                dx = -moveSpeed;
                if (x <= 50) movingLeft = false;
            } else {
                dx = moveSpeed;
                if (x >= 650) movingLeft = true;
            }
            super.move(delta);
        }

        // 2. 페이즈 관리
        updatePhase();

        // 3. 공격 패턴 실행
        executePattern(delta);
    }

    private void updatePhase() {
        double hpRatio = (double) hp / maxHp;

        if (hpRatio > 0.6) {
            currentPhase = 1; // 체력 60% 이상: 페이즈 1
            game.reverseControls = false; // 조작 정상
        } else if (hpRatio > 0.3) {
            currentPhase = 2; // 체력 30%~60%: 페이즈 2
            game.reverseControls = true; // [기믹] 조작 반전
        } else {
            currentPhase = 3; // 체력 30% 이하: 페이즈 3 (궁극기)
            game.reverseControls = false; // 조작 정상 복구 (피해야 하므로)
        }
    }

    private void executePattern(long delta) {
        long now = System.currentTimeMillis();

        // 페이즈 1: 포식 (잡동사니 뱉기)
        if (currentPhase == 1) {
            if (now - lastShot > 800) { // 0.8초마다 발사
                lastShot = now;
                // 플레이어 쪽으로 파편 발사
                fireDebris();
            }
        }
        // 페이즈 2: 중력 붕괴 (조작 반전 + 빠른 탄막)
        else if (currentPhase == 2) {
            if (now - lastShot > 600) { // 0.6초마다 발사 (더 빠름)
                lastShot = now;
                // 3방향 확산탄
                fireSpreadShot();
            }
        }
        // 페이즈 3: 감마선 폭발
        else if (currentPhase == 3) {
            if (!isGammaRayActive && now - lastShot > 5000) { // 5초마다 궁극기 시도
                lastShot = now;
                startGammaRaySequence();
            }
        }
    }

    // [페이즈 1 패턴] 파편 뱉기
    private void fireDebris() {
        // ShotEntity를 재활용하되, 이미지를 debris.png로 사용
        // 플레이어 방향으로 쏘면 좋겠지만, 간단히 아래로 부채꼴 발사
        game.addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, -100, 200));
        game.addEntity(new ShotEntity(game, "sprites/debris.png", (int)x+50, (int)y+50, 100, 200));
    }

    // [페이즈 2 패턴] 확산탄
    private void fireSpreadShot() {
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, 0, 300));
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, -150, 250));
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, 150, 250));
    }

    // [페이즈 3 패턴] 감마선 시퀀스 (스레드 사용)
    private void startGammaRaySequence() {
        isGammaRayActive = true; // 이동 멈춤

        new Thread(() -> {
            try {
                // 1. 중앙으로 이동 (텔레포트 느낌)
                x = 275; // (800 - 250)/2

                // 2. 경고 (2초)
                game.addEntity(new GammaRayEntity(game, 275, 0, 2000, true));
                Thread.sleep(2000);

                // 3. 발사! (3초 지속)
                game.addEntity(new GammaRayEntity(game, 275, 0, 3000, false));
                Thread.sleep(3000);

                // 4. 종료
                isGammaRayActive = false;
                lastShot = System.currentTimeMillis(); // 쿨타임 리셋

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어 총알에 맞았을 때
        if (other instanceof ShotEntity && ((ShotEntity)other).getDY() < 0) {
            hp -= 10; // 데미지
            game.removeEntity(other);

            if (hp <= 0) {
                game.removeEntity(this);
                game.bossKilled();
                // 보스 죽으면 조작 반전 해제 보장
                game.reverseControls = false;
            }
        }
        if (other instanceof ShipEntity) {
            // 게임의 notifyDeath()를 호출하여 즉사 처리
            // (무적 상태여도 블랙홀 보스 접촉은 사망 처리하는 것이 기믹상 자연스럽습니다)
            game.notifyDeath();
        }
    }

    public int getHP() { return hp; }
    public int getMaxHP() { return maxHp; }
}