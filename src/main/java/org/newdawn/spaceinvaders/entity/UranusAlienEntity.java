package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class UranusAlienEntity extends AlienEntity {

    public UranusAlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(game, ref, x, y, moveSpeed, firingChance);
    }

    @Override
    protected void initAnimations(String ref) {
        frames[0] = sprite;
        frames[1] = sprite;
        frames[2] = sprite;
        frames[3] = sprite;
        // 부모 클래스와 달리 _2 이미지를 로드하여 프레임에 섞지 않음 (초기에는 갑옷 상태)
    }

    // [추가] 네트워크 동기화로 인해 이미지가 변경될 때 호출됨
    @Override
    public void updateSpriteRef(String newRef) {
        super.updateSpriteRef(newRef); // 기본 이미지 로드

        // 만약 서버에서 온 이미지가 "깨진 갑옷(_2)" 상태라면
        if (newRef.contains("_2")) {
            // 모든 프레임을 깨진 이미지로 고정
            for (int i = 0; i < frames.length; i++) {
                frames[i] = this.sprite;
            }
            this.normalSprite = this.sprite;
            this.hitSprite = this.sprite;
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        this.hp -= damage;

        // [특수 기믹] 체력이 1이 남으면(갑옷 깨짐) 이미지 변경
        if (this.hp == 1) {
            String brokenRef = this.spriteRef.replace(".", "_2.");
            try {
                Sprite brokenSprite = SpriteStore.get().getSprite(brokenRef);

                this.sprite = brokenSprite;
                this.normalSprite = brokenSprite;
                this.hitSprite = brokenSprite;
                this.spriteRef = brokenRef; // ref 업데이트 (네트워크 전송용)

                // 애니메이션 프레임도 모두 교체
                for (int i = 0; i < frames.length; i++) {
                    frames[i] = brokenSprite;
                }
            } catch (Exception e) {
                System.err.println("Broken armor sprite not found for Uranus alien.");
            }
        }

        this.hitTimer = 100;
        this.sprite = this.hitSprite;

        if (this.hp <= 0) {
            this.alive = false;
            return true;
        }
        return false;
    }
}