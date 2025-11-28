package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;
import org.newdawn.spaceinvaders.CurrentUserManager;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SoundManager;
import org.newdawn.spaceinvaders.GameState;

/**
 * The entity that represents the players ship
 * * @author Kevin Glass
 */
public class ShipEntity extends Entity {
    /** The game in which the ship exists */
    private boolean shieldActive = false;

    private int maxHealth = 1; // 최대 체력
    private int currentHealth; // 현재 체력
    private boolean boosterActive = false; // 부스터 활성화 여부
    private long boosterTimer = 0;
    private double targetX = -999; // 초기값은 유효하지 않은 값으로 설정
    private double targetY = -999;
    private static final double SMOOTHING_FACTOR = 0.2;
    private final ShipWeapon weapon;
    private final ShipMovement movement;

    public void setHealth(int health) {
        this.maxHealth = health;
        this.currentHealth = health;
    }

    public void activateShield() {
        this.shieldActive = true;
        System.out.println("Heat Shield Activated!");
    }
    public void activateBooster() {
        movement.activateBooster(2000);
    }
    public boolean isBoosterActive() {
        return boosterActive;
    }

    public void setTargetLocation(double x, double y) {
        movement.setTargetLocation(x, y);
    }

    /**
     * 데미지를 입었을 때 호출됩니다.
     */
    public void takeDamage() {
        // [수정] 이미 파괴되었거나 게임이 대기 상태(사망 처리 중)라면 추가 데미지/사운드 무시
        if (currentHealth <= 0 || game.getLevelManager().isWaitingForKeyPress()) {
            return;
        }

        SoundManager.get().playSound("sounds/hit.wav");

        if (shieldActive) {
            shieldActive = false;
            System.out.println("Shield blocked the damage!");
            return;
        }

        currentHealth--;
        if (currentHealth <= 0) {
            game.getLevelManager().notifyDeath();
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int health) {
        this.currentHealth = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public ShipEntity(Game game, String ref, int x, int y) {
        super(ref, x, y);
        this.game = game;
        this.weapon = new ShipWeapon(game); // 초기화
        this.movement = new ShipMovement(this);
    }

    public boolean isShieldActive() {
        return this.shieldActive;
    }

    @Override
    public void draw(java.awt.Graphics g) {
        super.draw(g);

        if (shieldActive) {
            g.setColor(new java.awt.Color(255, 0, 0, 150));
            g.drawOval((int)x - 5, (int)y - 5, getSpriteWidth() + 10, getSpriteHeight() + 10);
            g.setColor(java.awt.Color.RED);
            g.drawOval((int)x - 6, (int)y - 6, getSpriteWidth() + 12, getSpriteHeight() + 12);
        }
        if (boosterActive) {
            g.setColor(new java.awt.Color(255, 215, 0, 150));
            g.fillOval((int)x + 10, (int)y + getSpriteHeight(), 10, 15);
            g.fillOval((int)x + getSpriteWidth() - 20, (int)y + getSpriteHeight(), 10, 15);
        }
    }

    @Override
    public void move(long delta) {
        // 1. 이동 관련 로직 위임
        movement.update(delta);

        // 2. 만약 내 기체라면(보간 타겟이 없다면) 부모의 기본 이동(velocity 기반) 수행
        // (ShipMovement에서 처리가 안 된 부분만 수행)
        super.move(delta);
    }

    public void tryToFire() {
        // 복잡한 로직 제거 -> 위임
        weapon.tryToFire(this.x, this.y);
    }

    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity || other instanceof BossEntity) {
            // [수정] Game 클래스가 아니라 LevelManager를 통해 사망 처리 호출
            game.getLevelManager().notifyDeath();
        }
    }
}