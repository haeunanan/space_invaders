package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;
import org.newdawn.spaceinvaders.CurrentUserManager;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GameState;
import org.newdawn.spaceinvaders.SoundManager;

public class ShipWeapon {
    private final Game game;

    public ShipWeapon(Game game) {
        this.game = game;
    }

    /**
     * 무기 발사를 시도합니다.
     * @param x 발사 주체의 x 좌표
     * @param y 발사 주체의 y 좌표
     */
    public void tryToFire(double x, double y) {
        // 1. 발사 간격(쿨타임) 체크
        long interval = game.getPlayerStats().getFiringInterval();
        if (System.currentTimeMillis() - game.lastFire < interval) {
            return;
        }
        game.lastFire = System.currentTimeMillis();

        // 2. 발사 위치 및 속도 설정
        int baseX = (int) x + 10;
        int baseY = (int) y - 30;
        double shotDY = -300; // 기본 총알 속도

        // 싱글 플레이 시 스테이지별 탄속 적용
        if (game.getCurrentState() == GameState.PLAYING_SINGLE && game.getCurrentStage() != null) {
            shotDY = game.getCurrentStage().getPlayerShotVelocity();
        }

        // 3. 총알 생성 (미사일 레벨에 따른 개수 처리)
        String myUid = CurrentUserManager.getInstance().getUid();
        int missileCount = game.getPlayerStats().getMissileCount();

        for (int i = 0; i < missileCount; i++) {
            // 여러 발 발사 시 간격(offset) 조정
            int offset = (i - (missileCount - 1) / 2) * 10;

            ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", baseX + offset, baseY, 0, shotDY);

            // PVP 또는 협동 모드일 경우 소유자 ID 설정
            if (game.getCurrentState() == GameState.PLAYING_PVP || game.getCurrentState() == GameState.PLAYING_COOP) {
                shot.setOwnerUid(myUid);
            }

            game.getEntityManager().addEntity(shot);
        }

        // 4. 발사 효과음 재생
        SoundManager.get().playSound("sounds/shoot.wav");
    }
}