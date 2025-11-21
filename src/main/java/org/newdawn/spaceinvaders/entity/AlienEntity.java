package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
    private Game game;
    private double moveSpeed = 75;
    private double firingChance;
    private boolean alive = true;

    // [복구] 기본 애니메이션(움직임)을 위한 변수들
    private Sprite[] frames = new Sprite[4]; // 4프레임 배열
    private long lastFrameChange = 0;
    private long frameDuration = 250; // 프레임 전환 속도 (0.25초)
    private int frameNumber = 0;

    // [추가] 체력 시스템 (목성)
    private int hp = 1;

    // [추가] 피격 애니메이션용 변수 (목성)
    private Sprite normalSprite; // 평상시 모습 (현재 프레임)
    private Sprite hitSprite;    // 맞았을 때 모습
    private long hitTimer = 0;   // 피격 상태 지속 시간

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);

        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        // ==========================================
        // 1. 기본 애니메이션 설정 (토성 스테이지 등)
        // ==========================================
        // 일단 모든 프레임을 기본 이미지로 초기화
        frames[0] = sprite;
        frames[1] = sprite;
        frames[2] = sprite;
        frames[3] = sprite;

        // "_2" 이미지가 있는지 확인 (토성 적의 고리 회전용)
        String ref2 = ref.replace(".", "_2.");
        java.net.URL url = this.getClass().getClassLoader().getResource(ref2);

        if (url != null) {
            // 파일이 있으면 1, 3번 프레임에 적용 (0, 2번은 기본 이미지)
            Sprite sprite2 = SpriteStore.get().getSprite(ref2);
            frames[1] = sprite2;
            frames[3] = sprite2;
        }

        // ==========================================
        // 2. 피격(Hit) 스프라이트 설정 (목성 스테이지 등)
        // ==========================================
        this.normalSprite = this.sprite; // 초기 상태 저장

        String hitRef = ref.replace(".", "_hit.");
        try {
            // _hit 이미지가 있으면 로드, 없으면 기본 이미지 사용
            java.net.URL hitUrl = this.getClass().getClassLoader().getResource(hitRef);
            if (hitUrl != null) {
                this.hitSprite = SpriteStore.get().getSprite(hitRef);
            } else {
                this.hitSprite = this.normalSprite;
            }
        } catch (Exception e) {
            this.hitSprite = this.normalSprite;
        }
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * 데미지 처리 메소드
     * @return 죽었으면 true
     */
    public boolean takeDamage(int damage) {
        this.hp -= damage;

        // 피격 애니메이션 발동 (0.1초간)
        this.hitTimer = 100;
        // 현재 보여지는 스프라이트를 피격 이미지로 즉시 교체
        this.sprite = this.hitSprite;

        if (this.hp <= 0) {
            this.alive = false;
            return true; // 사망
        }
        return false; // 생존
    }

    @Override
    public void move(long delta) {
        // 1. 피격 타이머 업데이트 (피격 상태 복구)
        if (hitTimer > 0) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                // 시간이 다 되면 원래대로 복구해야 하는데,
                // 애니메이션 중일 수 있으므로 현재 프레임으로 복구합니다.
                this.sprite = frames[frameNumber];
            }
        }
        // 2. 기본 애니메이션 프레임 업데이트 (피격 중이 아닐 때만)
        else {
            lastFrameChange += delta;
            if (lastFrameChange > frameDuration) {
                lastFrameChange = 0;
                frameNumber++;
                if (frameNumber >= frames.length) {
                    frameNumber = 0;
                }
                // 프레임 교체
                this.sprite = frames[frameNumber];
                // 다음 피격 시 돌아올 이미지도 갱신
                this.normalSprite = this.sprite;
            }
        }

        // 3. 화면 경계 체크 및 이동 방향 전환
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        if ((dx > 0) && (x > 750)) {
            game.updateLogic();
        }

        // 4. 총알 발사 시도
        if (Math.random() < firingChance) {
            fire();
        }

        // 5. 실제 위치 이동 (Entity.move 호출)
        super.move(delta);
    }

    private void fire() {
        game.addEntity(new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20));
    }

    public void doLogic() {
        dx = -dx;
        y += 10;
        if (y > 570) {
            game.notifyDeath();
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void markDead() {
        this.alive = false;
    }

    public void collidedWith(Entity other) {
        // 충돌 로직은 ShotEntity 등에서 처리
    }
}