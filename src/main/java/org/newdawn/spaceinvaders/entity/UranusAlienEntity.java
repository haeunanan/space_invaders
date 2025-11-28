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
        // 부모 클래스와 달리 _2 이미지를 로드하여 프레임에 섞지 않음
    }

    @Override
    public boolean takeDamage(int damage) {
        // 부모의 체력 감소 수행
        this.hp -= damage;

        // [특수 기믹] 체력이 1이 남으면(갑옷 깨짐) 이미지 변경
        if (this.hp == 1) {
            String brokenRef = this.spriteRef.replace(".", "_2.");
            try {
                Sprite brokenSprite = SpriteStore.get().getSprite(brokenRef);

                // 현재 모습과 평상시 모습을 깨진 상태로 영구 변경
                this.sprite = brokenSprite;
                this.normalSprite = brokenSprite;

                this.hitSprite = brokenSprite;

                // 애니메이션 프레임도 모두 교체 (움직여도 갑옷이 다시 생기지 않도록)
                for (int i = 0; i < frames.length; i++) {
                    frames[i] = brokenSprite;
                }
            } catch (Exception e) {
                System.err.println("Broken armor sprite not found for Uranus alien.");
            }
        }

        // 공통 피격 효과 (반짝임)
        this.hitTimer = 100;
        this.sprite = this.hitSprite;

        if (this.hp <= 0) {
            this.alive = false;
            return true;
        }
        return false;
    }
}
