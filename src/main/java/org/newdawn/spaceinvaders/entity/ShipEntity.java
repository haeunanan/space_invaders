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
    private boolean shieldActive = false;

	private int maxHealth = 1; // 최대 체력
	private int currentHealth; // 현재 체력
    private boolean boosterActive = false; // 부스터 활성화 여부
    private long boosterTimer = 0;

	/**
	 * 게임 모드에 맞게 체력을 설정합니다.
	 * @param health 설정할 체력 값
	 */
	public void setHealth(int health) {
		this.maxHealth = health;
		this.currentHealth = health;
	}

    public void activateShield() {
        this.shieldActive = true;
        System.out.println("Heat Shield Activated!");
    }
    public void activateBooster() {
        this.boosterActive = true;
        this.boosterTimer = 2000; // 2초(2000ms) 동안 유지
        System.out.println("Thrust Booster Activated! (2s)");
    }
    public boolean isBoosterActive() {
        return boosterActive;
    }

	/**
	 * 데미지를 입었을 때 호출됩니다.
	 */
	public void takeDamage() {
		currentHealth--;
		if (currentHealth <= 0) {
			game.notifyDeath(); // 체력이 0이 되면 게임 오버 처리
		}
        if (shieldActive) {
            shieldActive = false;
            System.out.println("Shield blocked the damage!");
            return;
        }
        this.currentHealth--;
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

    public boolean isShieldActive() {
        return this.shieldActive;
    }

    // ShipEntity.java 내부의 draw 메소드

    @Override
    public void draw(java.awt.Graphics g) {
        // 1. 원래 비행기 이미지 그리기
        super.draw(g);

        // 2. 방어막이 켜져 있다면 빨간색 원 그리기
        if (shieldActive) {
            // [수정] 색상을 빨간색(Red)으로 변경
            g.setColor(new java.awt.Color(255, 0, 0, 150)); // 반투명한 빨간색
            g.drawOval((int)x - 5, (int)y - 5, getSpriteWidth() + 10, getSpriteHeight() + 10);

            // [수정] 테두리는 더 진한 빨간색
            g.setColor(java.awt.Color.RED);
            g.drawOval((int)x - 6, (int)y - 6, getSpriteWidth() + 12, getSpriteHeight() + 12);
        }
        if (boosterActive) {
            g.setColor(new java.awt.Color(255, 215, 0, 150)); // 반투명한 황금색
            // 기체 아래쪽에 작은 원(엔진 분사) 그리기
            g.fillOval((int)x + 10, (int)y + getSpriteHeight(), 10, 15);
            g.fillOval((int)x + getSpriteWidth() - 20, (int)y + getSpriteHeight(), 10, 15);
        }
    }
	
	/**
	 * Request that the ship move itself based on an elapsed ammount of
	 * time
	 * 
	 * @param delta The time that has elapsed since last move (ms)
	 */
    // [수정] move 메소드에 타이머 로직 추가
    @Override
    public void move(long delta) {
        // 1. 부스터 타이머 체크 및 감소
        if (boosterActive) {
            boosterTimer -= delta;
            if (boosterTimer <= 0) {
                boosterActive = false;
                boosterTimer = 0;
                System.out.println("Booster Deactivated.");
            }
        }

        // 2. 기존 화면 경계 체크 로직 (원래 있던 코드 유지)
        if ((dx < 0) && (x < 10)) {
            return;
        }
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