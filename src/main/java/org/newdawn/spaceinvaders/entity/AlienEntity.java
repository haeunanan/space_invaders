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

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);
        this.spriteRef = ref;
        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        initAnimations(ref);
        initHitSprite(ref);

        this.networkId = UUID.randomUUID().toString();
    }

    public String getNetworkId() { return networkId; }
    public void setNetworkId(String id) { this.networkId = id; }

    protected void initAnimations(String ref) {
        frames[0] = sprite;
        frames[1] = sprite;
        frames[2] = sprite;
        frames[3] = sprite;

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

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setBulletType(String type) {
        this.bulletType = type;
    }

    public boolean takeDamage(int damage) {
        this.hp -= damage;
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
        // [수정] 협동 모드이고 내가 게스트(Player 2)라면, 자체 이동 로직을 수행하지 않음
        // (NetworkManager를 통해 호스트가 보내준 위치로 강제 이동됨)
        if (game.getCurrentState() == GameState.PLAYING_COOP &&
                !game.getNetworkManager().amIPlayer1()) {

            // 애니메이션은 계속 업데이트해야 자연스러움
            updateAnimation(delta);
            return;
        }

        // 기존 이동 로직 (싱글 플레이 or 호스트)
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

    public void doLogic() {
        dx = -dx;
        y += 10;
        // [수정] Game 클래스가 아니라 LevelManager를 통해 사망 처리 호출
        if (y > 570) game.getLevelManager().notifyDeath();
    }
    public String getSpriteRef() {
        return this.spriteRef;
    }

    public boolean isAlive() {
        return alive;
    }

    public void markDead() {
        this.alive = false;
    }

    public void collidedWith(Entity other) {// 충돌 처리는 ShotEntity(피격 시) 또는 ShipEntity(충돌 시)에서 담당하므로
        // AlienEntity 자체에는 별도의 충돌 로직이 필요하지 않습니다.}
    }
}