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
    private boolean spriteChanged = false; // 이미지가 바뀌었는지 체크

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
            game.reverseControls = false; // 조작 정상 복구

            // [수정] 3페이즈 진입 시 이미지 교체 (조건문 안으로 이동)
            if (!spriteChanged) {
                changeSprite("sprites/boss_phase3.gif"); // 3페이즈 이미지 파일명
                spriteChanged = true;
                System.out.println("Boss entered Phase 3: Visual Changed!");
            }
        }
    }

    private void executePattern(long delta) {
        long now = System.currentTimeMillis();

        // 페이즈 1: 포식 (잡동사니 뱉기)
        if (currentPhase == 1) {
            if (now - lastShot > 800) {
                lastShot = now;
                fireDebris();
            }
        }
        // 페이즈 2: 중력 붕괴 (조작 반전 + 확산탄)
        else if (currentPhase == 2) {
            // [난이도 하향] 발사 간격 증가 (0.6초 -> 1.2초)
            // 조작이 반전된 상태에서 탄막이 너무 빠르면 피하기 힘드므로 느리게 변경
            if (now - lastShot > 1200) {
                lastShot = now;
                fireSpreadShot();
            }
        }
        // 페이즈 3: 감마선 폭발
        else if (currentPhase == 3) {
            // [난이도 하향] 궁극기 쿨타임 증가 (5초 -> 7초)
            // 공격 기회를 더 많이 줍니다.
            if (!isGammaRayActive && now - lastShot > 7000) {
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
        // [대폭 하향] 탄환 속도를 100~150 수준으로 낮춤 (플레이어 기본 속도보다 느림)
        // 플레이어가 보고 "아, 반대지!" 하고 반응할 수 있게 됩니다.
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, 0, 150));
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, -80, 100));
        game.addEntity(new ShotEntity(game, "sprites/boss_shot.gif", (int)x+50, (int)y+50, 80, 100));
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
    // [추가] 스프라이트 교체 헬퍼 메소드
    private void changeSprite(String ref) {
        try {
            this.sprite = org.newdawn.spaceinvaders.SpriteStore.get().getSprite(ref);
        } catch (Exception e) {
            System.err.println("Failed to load phase 3 sprite: " + ref);
        }
    }


    @Override
    public void collidedWith(Entity other) {
        // 플레이어 총알에 맞았을 때
        if (other instanceof ShotEntity && ((ShotEntity)other).getDY() < 0) {
            hp -= 100; // 데미지
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