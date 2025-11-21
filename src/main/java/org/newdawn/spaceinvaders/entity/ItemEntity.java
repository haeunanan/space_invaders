package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.stage.Stage;

public class ItemEntity extends Entity {
    private Game game;
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
            game.removeEntity(this);
        }
    }

    @Override
    public void collidedWith(Entity other) {
        // 플레이어와 충돌 시 효과 발동
        if (other instanceof ShipEntity) {
            game.removeEntity(this); // 아이템 제거

            // 현재 스테이지의 안정제 효과 발동
            Stage currentStage = game.getCurrentStage();
            if (currentStage != null) {
                currentStage.activateStabilizer();
            }
        }
    }
}
