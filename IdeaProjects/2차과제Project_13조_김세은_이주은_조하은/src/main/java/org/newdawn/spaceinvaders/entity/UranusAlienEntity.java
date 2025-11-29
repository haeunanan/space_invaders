package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class UranusAlienEntity extends AlienEntity {

    public UranusAlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(game, ref, x, y, moveSpeed, firingChance);

        // [수정] 생성 직후 애니메이션 비활성화 (갑옷 입은 상태 고정)
        disableAnimation();
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
        // 1. 데미지 처리 (부모 클래스 로직 수행)
        boolean died = super.takeDamage(damage);

        // 2. [기믹] 체력이 1이 남으면 갑옷 깨짐 이미지로 영구 변경
        if (!died && getHp() == 1) {
            // 현재 이미지 경로에서 _2가 포함된 경로 생성 (예: alien_uranus.gif -> alien_uranus_2.gif)
            String brokenRef = this.spriteRef.replace(".", "_2.");

            // 이미지 교체
            updateSpriteRef(brokenRef);

            // 교체 후 다시 애니메이션 비활성화 (깨진 상태 고정)
            disableAnimation();
        }

        return died;
    }
}
