package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
    private Game game;
    private double moveSpeed = 75;
    private double firingChance;
    private boolean alive = true;

    // 애니메이션 프레임 배열은 유지하되, 내용은 동일한 이미지로 채웁니다.
    private Sprite[] frames = new Sprite[4];
    private long lastFrameChange;
    private long frameDuration = 250;
    private int frameNumber;

    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y);

        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        this.dx = -this.moveSpeed;

        // [수정] 복잡한 애니메이션 로직 제거
        // 전달받은 이미지(ref -> sprite)를 모든 프레임에 동일하게 적용하여 이미지가 바뀌지 않게 합니다.
        // 이렇게 하면 alien_mars.gif 하나만 계속 떠 있게 됩니다.
        frames[0] = sprite;
        frames[1] = sprite;
        frames[2] = sprite;
        frames[3] = sprite;
    }

    @Override
    public void move(long delta) {
        // 프레임 변경 로직은 유지하지만, 모든 프레임이 같은 이미지이므로 겉보기엔 변화가 없습니다.
        lastFrameChange += delta;
        if (lastFrameChange > frameDuration) {
            lastFrameChange = 0;
            frameNumber++;
            if (frameNumber >= frames.length) {
                frameNumber = 0;
            }
            sprite = frames[frameNumber];
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