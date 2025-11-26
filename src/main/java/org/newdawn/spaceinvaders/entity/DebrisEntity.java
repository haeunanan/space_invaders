package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

public class DebrisEntity extends Entity {
    private double centerX = 400; // 블랙홀 중심 X
    private double centerY = 100; // 블랙홀 중심 Y
    private double speed = 200;

    public DebrisEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
    }

    @Override
    public void move(long delta) {
        // 1. 블랙홀 중심을 향한 벡터 계산
        double dx = centerX - x;
        double dy = centerY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance < 20) {
            game.getEntityManager().removeEntity(this); // 블랙홀에 흡수됨
            return;
        }

        // 2. 빨려 들어가는 움직임 (정규화 후 이동)
        x += (dx / distance) * speed * delta / 1000.0;
        y += (dy / distance) * speed * delta / 1000.0;

        // (선택사항) 회전하며 빨려가는 느낌을 주기 위해 약간의 접선 방향 힘 추가 가능
    }

    @Override
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            ShipEntity ship = (ShipEntity) other;

            // 자신(잔해) 제거
            game.getEntityManager().removeEntity(this);

            // [수정] 데미지 처리 및 로그 출력
            // 만약 방어막(Shield)이 있다면 방어막만 까짐
            if (ship.isShieldActive()) {
                ship.takeDamage(); // ShipEntity 내부에서 실드 있으면 실드만 해제함
                System.out.println("Debris blocked by Shield!");
            } else {
                // 방어막 없으면 체력 감소
                ship.takeDamage();
                System.out.println("Debris Hit! Player HP: " + ship.getCurrentHealth());
            }
        }
    }
}