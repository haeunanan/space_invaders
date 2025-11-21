package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
    private Game game;
    private double moveSpeed = 75;
    private double firingChance;
    private boolean alive = true;

    // [추가] 체력 시스템
    private int hp = 1;

    // [추가] 피격 애니메이션용 변수
    private Sprite normalSprite; // 평상시 모습
    private Sprite hitSprite;    // 맞았을 때 모습 (아파하는 표정 or 붉은색 등)
    private long hitTimer = 0;   // 피격 상태 지속 시간 타이머

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);

        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        // 1. 기본 스프라이트 저장
        this.normalSprite = this.sprite;

        // 2. 피격 스프라이트 로드
        // 파일명 규칙: "이름.png" -> "이름_hit.png"
        // 확장자(.gif, .png) 앞부분에 _hit를 붙여서 로드합니다.
        String hitRef = ref.replace(".", "_hit.");
        try {
            this.hitSprite = SpriteStore.get().getSprite(hitRef);
        } catch (Exception e) {
            // 만약 _hit 이미지가 없으면 그냥 기본 이미지를 씁니다.
            this.hitSprite = this.normalSprite;
            System.out.println("Warning: Hit sprite not found for " + ref);
        }
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * 데미지를 입는 메소드
     * @return 체력이 0 이하가 되어 죽었으면 true, 살았으면 false
     */
    public boolean takeDamage(int damage) {
        this.hp -= damage;

        // [추가] 피격 애니메이션 발동!
        // 100ms(0.1초) 동안 피격 이미지를 보여줍니다.
        this.hitTimer = 100;
        this.sprite = this.hitSprite; // 즉시 이미지 교체

        if (this.hp <= 0) {
            this.alive = false;
            return true; // 사망
        }
        return false; // 생존
    }

    @Override
    public void move(long delta) {
        // [추가] 피격 타이머 업데이트
        if (hitTimer > 0) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                // 시간이 다 되면 원래 이미지로 복구
                this.sprite = this.normalSprite;
            }
        }

        // 화면 경계 체크
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        if ((dx > 0) && (x > 750)) {
            game.updateLogic();
        }

        // 총알 발사
        if (Math.random() < firingChance) {
            fire();
        }

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
        // 충돌 처리
    }
}