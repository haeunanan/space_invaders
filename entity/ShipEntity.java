package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;

/**
 * The entity that represents the players ship
 * 
 * @author Kevin Glass
 */
public class ShipEntity extends Entity {
	/** The game in which the ship exists */
	private Game game;
	
	/**
	 * Create a new entity to represent the players ship
	 *  
	 * @param game The game in which the ship is being created
	 * @param ref The reference to the sprite to show for the ship
	 * @param x The initial x location of the player's ship
	 * @param y The initial y location of the player's ship
	 */
	public ShipEntity(Game game,String ref,int x,int y) {
		super(ref,x,y);
		
		this.game = game;
	}
	
	/**
	 * Request that the ship move itself based on an elapsed ammount of
	 * time
	 * 
	 * @param delta The time that has elapsed since last move (ms)
	 */
	public void move(long delta) {
		// compute bounds using the game's dimensions and this sprite's size
		int leftMargin = 10;
		int topLimit = game.getHeight() / 3; // 화면의 1/3까지만 위로 이동 가능 
		int rightLimit = game.getWidth() - sprite.getWidth() - 10;
		int bottomLimit = game.getHeight() - sprite.getHeight() - 10;

		// horizontal bounds
		if ((dx < 0) && (x < leftMargin)) {
			return;
		}
		if ((dx > 0) && (x > rightLimit)) {
			return;
		}

		// perform the movement
		super.move(delta);

		// clamp vertical position so the ship stays within the bottom half of the screen
		if (y < topLimit) {
			y = topLimit;
		}
		if (y > bottomLimit) {
			y = bottomLimit;
		}
	}
	
	/**
	 * Notification that the player's ship has collided with something
	 * 
	 * @param other The entity with which the ship has collided
	 */
	public void collidedWith(Entity other) {
		// if its an alien, notify the game that the player
		// is dead
		if (other instanceof AlienEntity) {
			game.notifyDeath();
		}
	}
}
