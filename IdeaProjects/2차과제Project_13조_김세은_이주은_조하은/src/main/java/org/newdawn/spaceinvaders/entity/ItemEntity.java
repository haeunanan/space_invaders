package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.stage.Stage;

public class ItemEntity extends Entity {
    private double moveSpeed = 150; // 아이템 낙하 속도

    public ItemEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.dy = moveSpeed; // 아래로 떨어지도록 설정
    }

    @Override
    public void move(long delta) {
        super.move(delta);
        // 화면 아래로 벗어나면 제거
        if (y > 600) {
            game.getEntityManager().removeEntity(this);
        }
    }

    // collidedWith 메소드 내부 수정
    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            game.getEntityManager().removeEntity(this);

            Stage currentStage = game.getCurrentStage();
            if (currentStage != null) {
                // [수정] 스테이지별 고유 아이템 효과 발동
                currentStage.activateItem();
                System.out.println("Item acquired! Stage effect activated.");
            }
        }
    }
}