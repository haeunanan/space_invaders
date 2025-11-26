package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.CurrentUserManager;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.GameState;
import org.newdawn.spaceinvaders.SoundManager;
import org.newdawn.spaceinvaders.stage.Stage;

/**
 * An entity representing a shot fired by the player's ship
 */
public class ShotEntity extends Entity {
	private boolean used = false;
	private String ownerUid;

	public ShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
		super(sprite, x, y);
		this.game = game;
		this.dx = dx;
		this.dy = dy;
	}

	public String getOwnerUid() {
		return ownerUid;
	}

	public void setOwnerUid(String uid) {
		this.ownerUid = uid;
	}

	public boolean isOwnedBy(String uid) {
		if (uid == null || ownerUid == null) return false;
		return ownerUid.equals(uid);
	}

	@Override
	public void move(long delta) {
		super.move(delta); // 기본 이동 처리

		// [추가] 해왕성 바람 효과 (싱글 플레이어 미사일만 해당)
		if (game.getCurrentState() == GameState.PLAYING_SINGLE && dy < 0) {
			if (game.getCurrentStage() instanceof org.newdawn.spaceinvaders.stage.NeptuneStage) {
				org.newdawn.spaceinvaders.stage.NeptuneStage ns =
						(org.newdawn.spaceinvaders.stage.NeptuneStage) game.getCurrentStage();
				double wind = ns.getCurrentWindForce();
				if (wind != 0) {
					x += wind * delta / 1000.0;
				}
			}
		}

		// 화면 밖 제거
		if (y < -50 || y > 650) {
			game.getEntityManager().removeEntity(this);
		}
	}

    @Override
    public void collidedWith(Entity other) {
        // 이미 사용된 총알이면 무시
        if (used) return;

        // 1. 싱글 또는 협동 모드일 때 외계인 충돌 처리
        if (isSingleOrCoopMode()) {
            handleAlienCollision(other);
        }
        // 2. PVP 모드일 때 플레이어 간 충돌 처리
        else if (game.getCurrentState() == GameState.PLAYING_PVP) {
            handlePvpCollision(other);
        }
    }
    private boolean isSingleOrCoopMode() {
        return game.getCurrentState() == GameState.PLAYING_SINGLE ||
                game.getCurrentState() == GameState.PLAYING_COOP;
    }
    private void handleAlienCollision(Entity other) {
        if (other instanceof AlienEntity) {
            AlienEntity alien = (AlienEntity) other;
            if (!alien.isAlive()) return;

            // 총알 제거 및 사용 처리
            game.getEntityManager().removeEntity(this);
            used = true;

            // 데미지 적용 및 사망 처리
            if (alien.takeDamage(1)) {
                processAlienDeath(alien);
            }
        }
    }
    private void processAlienDeath(AlienEntity alien) {
        alien.markDead();
        game.getEntityManager().removeEntity(alien);
        game.getLevelManager().notifyAlienKilled();
        SoundManager.get().playSound("sounds/explosion.wav");

        tryDropItem(alien);
    }
    private void tryDropItem(AlienEntity alien) {
        // 싱글 모드이고 아이템이 허용된 스테이지인 경우에만
        if (game.getCurrentState() == GameState.PLAYING_SINGLE &&
                game.getCurrentStage() != null &&
                game.getCurrentStage().isItemAllowed()) {

            // 10% 확률로 아이템 드랍
            if (Math.random() < 0.1) {
                String itemRef = game.getCurrentStage().getItemSpriteRef();
                ItemEntity item = new ItemEntity(game, itemRef, alien.getX(), alien.getY());
                game.getEntityManager().addEntity(item);
            }
        }
    }
    private void handlePvpCollision(Entity other) {
        String myUid = CurrentUserManager.getInstance().getUid();

        // (A) 내가 쏜 총알이 상대방(OpponentShip)을 맞췄을 때
        if (other == game.getOpponentShip()) {
            if (this.isOwnedBy(myUid)) {
                used = true;
                game.getEntityManager().removeEntity(this);
                // 데미지 처리는 상대방 클라이언트에서 수행됨
            }
        }
        // (B) 상대방 총알이 나(Ship)를 맞췄을 때
        else if (other == game.getShip()) {
            if (!this.isOwnedBy(myUid)) { // 내 총알이 아닐 경우
                used = true;
                game.getEntityManager().removeEntity(this);

                // 데미지 처리 (쉴드 여부는 takeDamage 내부에서 확인)
                ((ShipEntity) other).takeDamage();
            }
        }
    }
}