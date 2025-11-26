package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.stage.NeptuneStage;

public class PlayerController {
    private final Game game;
    private final InputManager inputManager;

    // 조작할 대상 (매번 바뀔 수 있으므로 Setter 제공)
    private ShipEntity currentShip;

    public PlayerController(Game game, InputManager inputManager) {
        this.game = game;
        this.inputManager = inputManager;
    }

    public void setShip(ShipEntity ship) {
        this.currentShip = ship;
    }

    // [Game.java에서 processPlayerInput 대체]
    public void update() {
        if (currentShip == null || game.isWaitingForKeyPress()) return;

        handleMovement();
        handleEnvironmentalEffects();

        if (inputManager.isFirePressed()) {
            currentShip.tryToFire();
        }
    }

    // [Game.java에서 handleMovement 이동]
    private void handleMovement() {
        currentShip.setHorizontalMovement(0);
        currentShip.setDY(0);

        // Game의 private 변수에 접근하기 위해 getter가 필요할 수 있음
        // 여기서는 Game의 로직을 가져왔으므로 Game을 통해 필요한 값에 접근
        // (slowTimer 등은 Game에 public getter를 추가하거나 인자로 받아야 함.
        //  간단하게는 Game 내부에 getter가 있다고 가정하거나 직접 접근권한을 줍니다.)

        // ※ 주의: Game.java에 getMoveSpeed(), isSlowed() 같은 메서드가 필요할 수 있음.
        // 리팩토링 편의상 직접 로직을 수행하도록 작성합니다.

        double speed = game.getMoveSpeed(); // Getter 필요 (아래 Game.java 수정 참고)

        int dx = 0;
        int dy = 0;

        if (inputManager.isLeftPressed())  dx -= 1;
        if (inputManager.isRightPressed()) dx += 1;
        if (inputManager.isUpPressed())    dy -= 1;
        if (inputManager.isDownPressed())  dy += 1;

        if (game.isReverseControls()) { // Getter 필요
            dx *= -1;
            dy *= -1;
        }

        currentShip.setHorizontalMovement(dx * speed);
        currentShip.setDY(dy * speed);
    }

    // [Game.java에서 handleEnvironmentalEffects 이동]
    private void handleEnvironmentalEffects() {
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE
                && game.getCurrentStage() instanceof NeptuneStage) {

            NeptuneStage ns = (NeptuneStage) game.getCurrentStage();
            double wind = ns.getCurrentWindForce();

            if (wind != 0 && !currentShip.isBoosterActive()) {
                currentShip.setHorizontalMovement(currentShip.getDX() + wind);
            }
        }
    }
}
