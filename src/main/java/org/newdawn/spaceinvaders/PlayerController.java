package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.stage.NeptuneStage;

public class PlayerController {
    private final Game game;
    private final InputManager inputManager;
    private ShipEntity currentShip;

    // [이동됨] Game 클래스에서 가져온 이동 속도 관련 변수들
    private double moveSpeed = 300;
    private long slowTimer = 0;
    private boolean reverseControls = false;

    public PlayerController(Game game, InputManager inputManager) {
        this.game = game;
        this.inputManager = inputManager;
    }

    public void setReverseControls(boolean reverse) {
        this.reverseControls = reverse;
    }

    public boolean isReverseControls() {
        return reverseControls;
    }

    public void setShip(ShipEntity ship) {
        this.currentShip = ship;
    }

    public void update() {
        if (currentShip == null || game.getLevelManager().isWaitingForKeyPress()) return;

        handleMovement();
        handleEnvironmentalEffects();

        if (inputManager.isFirePressed()) {
            currentShip.tryToFire();
        }
    }

    // [추가] 타이머 업데이트 (Game의 gameLoop에서 호출)
    public void updateTimer(long delta) {
        if (slowTimer > 0) {
            slowTimer -= delta;
        }
    }

    // [추가] 상점에서 속도 업그레이드 시 호출
    public void upgradeMoveSpeed() {
        double baseSpeed = 300;
        // 플레이어의 현재 이동 레벨만큼 속도 증가 (10%씩)
        for (int i = 0; i < game.getPlayerStats().getMoveLevel(); i++) {
            baseSpeed *= 1.1;
        }
        this.moveSpeed = baseSpeed;
    }

    // [추가] 외부(아이템 등)에서 슬로우 효과를 걸 때 사용
    public void applySlow(long duration) {
        this.slowTimer = duration;
    }

    // [내부] 현재 실제 이동 속도 계산 (슬로우 디버프 적용 여부 확인)
    private double getCurrentMoveSpeed() {
        return (slowTimer > 0) ? moveSpeed * 0.5 : moveSpeed;
    }

    private void handleMovement() {
        currentShip.setHorizontalMovement(0);
        currentShip.setDY(0);

        // [수정] Game의 메서드가 아닌 자신의 메서드 사용
        double speed = getCurrentMoveSpeed();

        int dx = 0;
        int dy = 0;

        if (inputManager.isLeftPressed())  dx -= 1;
        if (inputManager.isRightPressed()) dx += 1;
        if (inputManager.isUpPressed())    dy -= 1;
        if (inputManager.isDownPressed())  dy += 1;

        if (this.reverseControls) {
            dx *= -1;
            dy *= -1;
        }

        currentShip.setHorizontalMovement(dx * speed);
        currentShip.setDY(dy * speed);
    }

    private void handleEnvironmentalEffects() {
        if (game.getCurrentState() == GameState.PLAYING_SINGLE
                && game.getCurrentStage() instanceof NeptuneStage) {

            NeptuneStage ns = (NeptuneStage) game.getCurrentStage();
            double wind = ns.getCurrentWindForce();

            if (wind != 0 && !currentShip.isBoosterActive()) {
                currentShip.setHorizontalMovement(currentShip.getDX() + wind);
            }
        }
    }
}