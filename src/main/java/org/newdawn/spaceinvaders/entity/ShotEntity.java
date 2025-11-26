package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * An entity representing a shot fired by the player's ship
 * 
 * @author Kevin Glass
 */
public class ShotEntity extends Entity {
	/** The game in which this entity exists */
	private Game game;
	/** True if this shot has been "used", i.e. its hit something */
	private boolean used = false;
	private String ownerUid;
	
	/**
	 * Create a new shot from the player
	 * 
	 * @param game The game in which the shot has been created
	 * @param sprite The sprite representing this shot
	 * @param x The initial x location of the shot
	 * @param y The initial y location of the shot
	 */
	public ShotEntity(Game game,String sprite,int x,int y, String ownerUid) {
		super(sprite,x,y);
		this.game = game;
		this.ownerUid = ownerUid;
		dy = -300;
	}

	public String getOwnerUid() { // <-- Getter 추가
		return ownerUid;
	}
	// 내가 쏜 총알인지 확인하는 헬퍼 메소드
	public boolean isOwnedBy(String uid) {
		if (uid == null || ownerUid == null) return false;
		return ownerUid.equals(uid);
	}

	/**
	 * Request that this shot moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		// proceed with normal move
		super.move(delta);
		
		// if we shot off the screen, remove ourselfs
		if (y < -100) {
			game.removeEntity(this);
		}
	}
	
	/**
	 * Notification that this shot has collided with another
	 * entity
	 * 
	 * @parma other The other entity with which we've collided
	 */
	public void collidedWith(Entity other) {
		// prevents double kills, if we've already hit something,
		// don't collide
		if (used) {
			return;
		}
		if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
			// if we've hit an alien, kill it!
			if (other instanceof AlienEntity) {
				AlienEntity alien = (AlienEntity) other;
				// only handle if alien still alive
				if (!alien.isAlive()) {
					return;
				}
				// mark as dead to prevent other shots from double-counting
				alien.markDead();
				// remove the affected entities
				game.removeEntity(this);
				game.removeEntity(other);

				// notify the game that the alien has been killed
				game.notifyAlienKilled();
				used = true;
			}
		} else if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
			if (other == game.getOpponentShip()) {
				used = true;
				game.removeEntity(this);
				// 데미지 처리는 상대방 클라이언트가 스스로 하므로, 여기서는 총알만 제거합니다.
			}
		}
	}
}