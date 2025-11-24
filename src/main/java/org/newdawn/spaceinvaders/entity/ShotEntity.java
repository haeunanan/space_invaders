package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.CurrentUserManager;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SoundManager;
import org.newdawn.spaceinvaders.stage.Stage;

/**
 * An entity representing a shot fired by the player's ship
 */
public class ShotEntity extends Entity {
	private Game game;
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
		if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE && dy < 0) {
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
			game.removeEntity(this);
		}
	}

	@Override
	public void collidedWith(Entity other) {
		if (used) return;

		// =============================================================
		// [수정] 1. 외계인 충돌 처리 (싱글 및 협동 모드 공통)
		// =============================================================
		if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE ||
				game.getCurrentState() == Game.GameState.PLAYING_COOP) {

			if (other instanceof AlienEntity) {
				AlienEntity alien = (AlienEntity) other;
				if (!alien.isAlive()) return;

				game.removeEntity(this);
				used = true;

				if (alien.takeDamage(1)) {
					alien.markDead();
					game.removeEntity(alien);
					game.notifyAlienKilled();
					SoundManager.get().playSound("sounds/explosion.wav");

					// 아이템 드랍 (싱글 모드에서만 혹은 협동에서도 허용 가능)
					if (game.getCurrentStage() != null && game.getCurrentStage().isItemAllowed()) {
						if (Math.random() < 0.1) {
							String itemRef = game.getCurrentStage().getItemSpriteRef();
							ItemEntity item = new ItemEntity(game, itemRef, alien.getX(), alien.getY());
							game.addEntity(item);
						}
					}
				}
			}
		}

		// =============================================================
		// [수정] 2. PVP 플레이어 충돌 처리
		// =============================================================
		else if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
			String myUid = CurrentUserManager.getInstance().getUid();

			// (A) 내가 쏜 총알이 상대방을 맞췄을 때 -> 내 화면에서 총알 제거 (데미지는 상대방이 처리)
			if (other == game.getOpponentShip()) {
				if (this.isOwnedBy(myUid)) {
					used = true;
					game.removeEntity(this);
				}
			}
			// (B) 상대방 총알이 나를 맞췄을 때 -> 데미지 처리
			else if (other == game.getShip()) {
				if (!this.isOwnedBy(myUid)) { // 내 총알이 아닐 경우
					used = true;
					game.removeEntity(this);

					// 쉴드 체크 및 체력 감소 로직
					ShipEntity myShip = (ShipEntity) other;
					if (myShip.isShieldActive()) {
						myShip.takeDamage(); // 쉴드만 제거됨
					} else {
						myShip.takeDamage(); // 체력 감소
						// HP 동기화는 GameLoop의 NetworkThread에서 처리됨
					}
				}
			}
		}
	}
}