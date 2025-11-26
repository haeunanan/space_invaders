package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
    // [수정] private -> protected로 변경하여 자식 클래스 접근 허용
    protected double moveSpeed = 75;
    protected double firingChance;
    protected boolean alive = true;
    protected String bulletType = "NORMAL";
    protected String spriteRef;

    protected Sprite[] frames = new Sprite[4];
    protected long lastFrameChange = 0;
    protected long frameDuration = 250;
    protected int frameNumber = 0;

    protected int hp = 1;

    protected Sprite normalSprite;
    protected Sprite hitSprite;
    protected long hitTimer = 0;

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);
        this.spriteRef = ref;
        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        initAnimations(ref);
        initHitSprite(ref);
    }

    // [추가] 생성자 로직 분리
    protected void initAnimations(String ref) {
        frames[0] = sprite;
        frames[1] = sprite;
        frames[2] = sprite;
        frames[3] = sprite;

        // 일반적인 외계인 움직임 애니메이션 (_2 이미지 로드)
        String ref2 = ref.replace(".", "_2.");
        java.net.URL url = this.getClass().getClassLoader().getResource(ref2);
        if (url != null) {
            Sprite sprite2 = SpriteStore.get().getSprite(ref2);
            frames[1] = sprite2;
            frames[3] = sprite2;
        }
    }

    protected void initHitSprite(String ref) {
        this.normalSprite = this.sprite;
        String hitRef = ref.replace(".", "_hit.");
        try {
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

    public void setHp(int hp) { this.hp = hp; }
    public void setBulletType(String type) { this.bulletType = type; }

    /**
     * 데미지 처리 (천왕성 갑옷 로직 제거 -> 자식 클래스로 이동)
     */
    public boolean takeDamage(int damage) {
        this.hp -= damage;

        // 피격 효과
        this.hitTimer = 100;
        this.sprite = this.hitSprite;

        if (this.hp <= 0) {
            this.alive = false;
            return true;
        }
        return false;
    }

    @Override
    public void move(long delta) {
        // [수정] updateNeptuneBehavior 제거됨
        updateAnimation(delta);
        checkBoundaries();
        tryToFire();
        super.move(delta);
    }

    protected void updateAnimation(long delta) {
        if (hitTimer > 0) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                this.sprite = frames[frameNumber];
            }
        } else {
            lastFrameChange += delta;
            if (lastFrameChange > frameDuration) {
                lastFrameChange = 0;
                frameNumber++;
                if (frameNumber >= frames.length) frameNumber = 0;
                this.sprite = frames[frameNumber];
                this.normalSprite = this.sprite;
            }
        }
    }

    protected void tryToFire() {
        if (Math.random() < firingChance) {
            fire();
        }
    }

    protected void checkBoundaries() {
        if ((dx < 0) && (x < 10)) game.updateLogic();
        if ((dx > 0) && (x > Constants.WINDOW_WIDTH - 50)) game.updateLogic();
    }

    protected void fire() {
        if ("ICE".equals(bulletType)) {
            game.addEntity(new AlienIceShotEntity(game, "sprites/ice_shot.png", getX() + 10, getY() + 20));
        } else {
            game.addEntity(new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20));
        }
    }

    public void doLogic() {
        dx = -dx;
        y += 10;
        if (y > 570) game.notifyDeath();
    }

    public boolean isAlive() { return alive; }
    public void markDead() { this.alive = false; }
    public void collidedWith(Entity other) {}
}