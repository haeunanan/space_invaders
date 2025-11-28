package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.*;
import java.util.UUID;

public class AlienEntity extends Entity {
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
    private String networkId;
    private final AlienAnimator animator;

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);
        this.game = game;
        this.spriteRef = ref;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        // 애니메이터 초기화
        this.animator = new AlienAnimator(ref, this);
        this.networkId = UUID.randomUUID().toString();
    }

    @Override
    public void move(long delta) {
        // 애니메이션 업데이트 위임
        animator.update(delta);
        this.sprite = animator.getCurrentSprite(); // 현재 스프라이트 갱신

        if (isCoopGuest()) return;

        checkBoundaries();
        tryToFire();
        super.move(delta);
    }

    private boolean isCoopGuest() {
        return game.getCurrentState() == GameState.PLAYING_COOP &&
                !game.getNetworkManager().amIPlayer1();
    }
    protected void tryToFire() {
        if (Math.random() < firingChance) {
            fire();
        }
    }

    protected void checkBoundaries() {
        if ((dx < 0) && (x < 10)) game.getEntityManager().doLogic();
        if ((dx > 0) && (x > Constants.WINDOW_WIDTH - 50)) game.getEntityManager().doLogic();
    }

    protected void fire() {
        if ("ICE".equals(bulletType)) {
            game.getEntityManager().addEntity(new AlienIceShotEntity(game, "sprites/ice_shot.png", getX() + 10, getY() + 20));
        } else {
            game.getEntityManager().addEntity(new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20));
        }
    }

    public void updateSpriteRef(String newRef) {
        this.spriteRef = newRef;
        animator.updateSpriteRef(newRef); // 위임
    }

    public boolean takeDamage(int damage) {
        this.hp -= damage;
        animator.startHitEffect(); // 피격 효과 위임

        if (this.hp <= 0) {
            this.alive = false;
            return true;
        }
        return false;
    }
    public void doLogic() {
        dx = -dx;
        y += 10;
        if (y > 570) game.getResultHandler().notifyDeath();
    }

    public void disableAnimation() {
        animator.stopAnimation();
    }

    // [추가] 현재 체력을 확인하는 getter (UranusAlienEntity에서 사용)
    public int getHp() {
        return hp;
    }

    // Getters / Setters
    public void setHp(int hp) { this.hp = hp; }
    public void setBulletType(String type) { this.bulletType = type; }
    public String getNetworkId() { return networkId; }
    public void setNetworkId(String id) { this.networkId = id; }
    public String getSpriteRef() { return this.spriteRef; }
    public boolean isAlive() { return alive; }
    public void markDead() { this.alive = false; }
    @Override
    public void collidedWith(Entity other) {
        // 충돌 로직은 ShotEntity(피격 시) 또는 ShipEntity(충돌 시)에서 처리하므로,
        // AlienEntity 자체에서는 별도의 충돌 처리를 하지 않습니다.
    }
}