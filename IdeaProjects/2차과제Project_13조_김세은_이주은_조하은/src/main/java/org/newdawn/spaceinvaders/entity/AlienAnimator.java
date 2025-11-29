package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;
import java.net.URL;

public class AlienAnimator {
    private Sprite[] frames = new Sprite[4];
    private Sprite normalSprite;
    private Sprite hitSprite;
    private Sprite currentSprite;

    private long lastFrameChange = 0;
    private long frameDuration = 250;
    private int frameNumber = 0;
    private long hitTimer = 0;

    public AlienAnimator(String ref, AlienEntity entity) {
        // 초기화 로직 이동
        this.currentSprite = SpriteStore.get().getSprite(ref);
        initAnimations(ref);
        initHitSprite(ref);
    }

    public Sprite getCurrentSprite() {
        return currentSprite;
    }

    public void update(long delta) {
        if (hitTimer > 0) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                this.currentSprite = frames[frameNumber];
            }
        } else {
            lastFrameChange += delta;
            if (lastFrameChange > frameDuration) {
                lastFrameChange = 0;
                frameNumber = (frameNumber + 1) % frames.length;
                this.currentSprite = frames[frameNumber];
                this.normalSprite = this.currentSprite;
            }
        }
    }
    public void stopAnimation() {
        for (int i = 0; i < frames.length; i++) {
            frames[i] = currentSprite;
        }
        this.normalSprite = currentSprite;
        this.hitSprite = currentSprite;
    }


    public void startHitEffect() {
        this.hitTimer = 100;
        this.currentSprite = this.hitSprite;
    }

    public void updateSpriteRef(String newRef) {
        this.currentSprite = SpriteStore.get().getSprite(newRef);
        initAnimations(newRef);
        initHitSprite(newRef);
    }

    // 기존 AlienEntity에 있던 private 메소드들 이동
    private void initAnimations(String ref) {
        frames[0] = currentSprite;
        frames[1] = currentSprite;
        frames[2] = currentSprite;
        frames[3] = currentSprite;

        String ref2 = ref.replace(".", "_2.");
        URL url = this.getClass().getClassLoader().getResource(ref2);
        if (url != null) {
            Sprite sprite2 = SpriteStore.get().getSprite(ref2);
            frames[1] = sprite2;
            frames[3] = sprite2;
        }
    }

    private void initHitSprite(String ref) {
        this.normalSprite = this.currentSprite;
        String hitRef = ref.replace(".", "_hit.");
        try {
            URL hitUrl = this.getClass().getClassLoader().getResource(hitRef);
            if (hitUrl != null) {
                this.hitSprite = SpriteStore.get().getSprite(hitRef);
            } else {
                this.hitSprite = this.normalSprite;
            }
        } catch (Exception e) {
            this.hitSprite = this.normalSprite;
        }
    }
}