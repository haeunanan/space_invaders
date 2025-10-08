package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;

/**
 * An entity which represents one of our space invader aliens.
 * 
 * @author Kevin Glass
 */
public class AlienEntity extends Entity {
	/** The speed at which the alient moves horizontally */
	private double moveSpeed = 75;
	/** The game in which the entity exists */
	private Game game;
    private double firingChance;
	/** The animation frames */
	private Sprite[] frames = new Sprite[4];
	/** The time since the last frame change took place */
	private long lastFrameChange;
	/** The frame duration in milliseconds, i.e. how long any given frame of animation lasts */
	private long frameDuration = 250;
	/** The current frame of animation being displayed */
	private int frameNumber;
	
	/**
     * Create a new alien entity
     *
     * @param game The game in which this entity is being created
     * @param x    The intial x location of this alien
     * @param y    The intial y location of this alient
     */
    public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
        super(ref, x, y); // 부모 클래스(Entity)의 생성자 호출
        this.game = game;
        this.moveSpeed = moveSpeed;
        this.firingChance = firingChance;
        dx = -this.moveSpeed; // 초기 이동 방향 및 속도 설정
    }

	/**
	 * Request that this alien moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
    @Override
    public void move(long delta) {
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        if ((dx > 0) && (x > 750)) {
            game.updateLogic();
        }
		
		// if we have reached the left hand side of the screen and
		// are moving left then request a logic update 
		if ((dx < 0) && (x < 10)) {
			game.updateLogic();
		}
		// and vice vesa, if we have reached the right hand side of 
		// the screen and are moving right, request a logic update
		if ((dx > 0) && (x > 750)) {
			game.updateLogic();
		}

        if (Math.random() < firingChance) {
            fire();
        }

        super.move(delta);
    }

	
	/**
	 * Update the game logic related to aliens
	 */
	public void doLogic() {
		// swap over horizontal movement and move down the
		// screen a bit
		dx = -dx;
		y += 10;
		
		// if we've reached the bottom of the screen then the player
		// dies
		if (y > 570) {
			game.notifyDeath();
		}
	}

    @Override
    public void collidedWith(Entity other) {

    }

    private void fire() {
        // 자신의 위치에 외계인 총알 생성
        AlienShotEntity shot = new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20);
        game.addEntity(shot);
    }
}
	
	/**
	 * Notification that this alien has collided with another entity
	 * 
	 * @param other The other entity
	 */
