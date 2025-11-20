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

	private int maxHealth = 1; // 최대 체력
	private int currentHealth; // 현재 체력

	/**
	 * 게임 모드에 맞게 체력을 설정합니다.
	 * @param health 설정할 체력 값
	 */
	public void setHealth(int health) {
		this.maxHealth = health;
		this.currentHealth = health;
	}

	/**
	 * 데미지를 입었을 때 호출됩니다.
	 */
	public void takeDamage() {
		currentHealth--;
		if (currentHealth <= 0) {
			game.notifyDeath(); // 체력이 0이 되면 게임 오버 처리
		}
	}

	public int getCurrentHealth() {
		return currentHealth;
	}
	/**
	 * 현재 체력을 특정 값으로 설정합니다. (네트워크 동기화용)
	 */
	public void setCurrentHealth(int health) {
		this.currentHealth = health;
	}

	public int getMaxHealth() {
		return maxHealth;
	}
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
		// if we're moving left and have reached the left hand side
		// of the screen, don't move
		if ((dx < 0) && (x < 10)) {
			return;
		}
		// if we're moving right and have reached the right hand side
		// of the screen, don't move
		if ((dx > 0) && (x > 750)) {
			return;
		}
		
		super.move(delta);
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