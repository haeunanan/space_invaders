package org.newdawn.spaceinvaders.entity;

import java.awt.Graphics;
import java.awt.Rectangle;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

/**
 * An entity represents any element that appears in the game. The
 * entity is responsible for resolving collisions and movement
 * based on a set of properties defined either by subclass or externally.
 * 
 * Note that doubles are used for positions. This may seem strange
 * given that pixels locations are integers. However, using double means
 * that an entity can move a partial pixel. It doesn't of course mean that
 * they will be display half way through a pixel but allows us not lose
 * accuracy as we move.
 * 
 * @author Kevin Glass
 */
public abstract class Entity {
	/**
	 * The current x location of this entity
	 */
	protected double x;
	/**
	 * The current y location of this entity
	 */
	protected double y;
	/**
	 * The sprite that represents this entity
	 */
	protected Sprite sprite;
	/**
	 * The current speed of this entity horizontally (pixels/sec)
	 */
	protected double dx;
	/**
	 * The current speed of this entity vertically (pixels/sec)
	 */
	protected double dy;

    protected Game game;

    /**
	 * The rectangle used for this entity during collisions  resolution
	 */
	private Rectangle me = new Rectangle();
	/**
	 * The rectangle used for other entities during collision resolution
	 */
	private Rectangle him = new Rectangle();

	/**
	 * Sets the location of this entity.
	 *
	 * @param x The new x coordinate
	 * @param y The new y coordinate
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Construct a entity based on a sprite image and a location.
	 *
	 * @param ref The reference to the image to be displayed for this entity
	 * @param x   The initial x location of this entity
	 * @param y   The initial y location of this entity
	 */
	public Entity(String ref, int x, int y) {
		this.sprite = SpriteStore.get().getSprite(ref);
		this.x = x;
		this.y = y;
	}

	/**
	 * Request that this entity move itself based on a certain ammount
	 * of time passing.
	 *
	 * @param delta The ammount of time that has passed in milliseconds
	 */
	public void move(long delta) {
		// update the location of the entity based on move speeds
		x += (delta * dx) / 1000;
		y += (delta * dy) / 1000;
	}

	/**
	 * Get the width of the sprite representing this entity
	 *
	 * @return The width in pixels of this entity
	 */
	public int getSpriteWidth() {
		if (sprite == null) return 0;
		return sprite.getWidth();
	}

	public int getSpriteHeight() {
		if (sprite == null) return 0;
		return sprite.getHeight();
	}

	/**
	 * Set the horizontal speed of this entity
	 *
	 * @param dx The horizontal speed of this entity (pixels/sec)
	 */
	public void setHorizontalMovement(double dx) {
		this.dx = dx;
	}
	public void setVerticalMovement(double dy) {
		this.dy = dy;
	}
	/**
	 * 이 엔티티를 지정된 좌표에 그립니다.
	 *
	 * @param g 그래픽 컨텍스트
	 * @param x 화면에 그릴 x 좌표
	 * @param y 화면에 그릴 y 좌표
	 */
	public void draw(Graphics g, int x, int y) {
		sprite.draw(g, x, y);
	}

	/**
	 * 이 엔티티를 자신의 내부 좌표에 그립니다. (기존 코드 호환용)
	 *
	 * @param g 그래픽 컨텍스트
	 */
	public void draw(Graphics g) {
		draw(g, (int) x, (int) y);
	}

	/**
	 * Do the logic associated with this entity. This method
	 * will be called periodically based on game events
	 */
	public void doLogic() {
	}

	/**
	 * Get the x location of this entity
	 *
	 * @return The x location of this entity
	 */
	public int getX() {
		return (int) x;
	}

	/**
	 * Get the y location of this entity
	 *
	 * @return The y location of this entity
	 */
	public int getY() {
		return (int) y;
	}

	/**
	 * Check if this entity collised with another.
	 *
	 * @param other The other entity to check collision against
	 * @return True if the entities collide with each other
	 */
	public boolean collidesWith(Entity other) {
        // [수정 후] getSpriteWidth(), getSpriteHeight() 메소드를 사용하여
        // GammaRayEntity처럼 크기를 재정의한 엔티티도 정상적으로 충돌 처리되도록 함
        me.setBounds((int) x, (int) y, getSpriteWidth(), getSpriteHeight());
        him.setBounds((int) other.x, (int) other.y, other.getSpriteWidth(), other.getSpriteHeight());

		return me.intersects(him);
	}
    public double getDX() {
        return dx;
    }

    public void setDX(double value) {
        this.dx = value;
    }
    public double getDY() {
        return dy;
    }

    public void setDY(double dy) {
        this.dy = dy;
    }
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
	 * Notification that this entity collided with another.
	 * 
	 * @param other The entity with which this entity collided.
	 */
	public abstract void collidedWith(Entity other);
}