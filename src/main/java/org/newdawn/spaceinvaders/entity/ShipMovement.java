package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;

public class ShipMovement {
    private final Entity ship;
    private double targetX = -999;
    private double targetY = -999;
    private static final double SMOOTHING_FACTOR = 0.2;

    private boolean boosterActive = false;
    private long boosterTimer = 0;

    public ShipMovement(Entity ship) {
        this.ship = ship;
    }

    public void setTargetLocation(double x, double y) {
        this.targetX = x;
        this.targetY = y;
        if (ship.getX() == 0 && ship.getY() == 0) {
            ship.setLocation((int)x, (int)y);
        }
    }

    public void activateBooster(long duration) {
        this.boosterActive = true;
        this.boosterTimer = duration;
    }

    public boolean isBoosterActive() { return boosterActive; }

    public void update(long delta) {
        // 1. 부스터 타이머 처리
        if (boosterActive) {
            boosterTimer -= delta;
            if (boosterTimer <= 0) boosterActive = false;
        }

        // 2. 네트워크 보간 이동 (상대방)
        if (targetX != -999 && targetY != -999) {
            interpolatePosition();
            return;
        }

        // 3. 경계 체크 (로컬 플레이어)
        checkBoundaries();
    }

    private void interpolatePosition() {
        double currentX = ship.getX();
        double currentY = ship.getY();

        double diffX = targetX - currentX;
        double diffY = targetY - currentY;

        if (Math.abs(diffX) < 1.0) ship.setLocation((int)targetX, (int)ship.getY());
        else ship.setX(currentX + diffX * SMOOTHING_FACTOR);

        if (Math.abs(diffY) < 1.0) ship.setLocation((int)ship.getX(), (int)targetY);
        else ship.setY(currentY + diffY * SMOOTHING_FACTOR);
    }

    private void checkBoundaries() {
        if (ship.getX() < 10) ship.setX(10);
        int maxX = Constants.WINDOW_WIDTH - ship.getSpriteWidth() - 10;
        if (ship.getX() > maxX) ship.setX(maxX);

        if (ship.getY() < 10) ship.setY(10);
        if (ship.getY() > 550) ship.setY(550);
    }
}
