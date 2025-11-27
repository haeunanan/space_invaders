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
        this.boosterTimer = 2000;
        System.out.println("Thrust Booster Activated! (2s)");
    }
    public boolean isBoosterActive() {
        return boosterActive;
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

    public ShipEntity(Game game,String ref,int x,int y) {
        super(ref,x,y);
        this.game = game;
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
        if (boosterActive) {
            boosterTimer -= delta;
            if (boosterTimer <= 0) {
                boosterActive = false;
                boosterTimer = 0;
                System.out.println("Booster Deactivated.");
            }
        }

        super.move(delta);

        // 1. 왼쪽 경계 (X 최소값: 10픽셀 여백 유지)
        if (x < 10) {
            x = 10;
        }

        // 2. 오른쪽 경계 (X 최대값: WINDOW_WIDTH - 스프라이트 너비 - 10픽셀 여백)
        int maxX = Constants.WINDOW_WIDTH - getSpriteWidth() - 10;
        if (x > maxX) {
            x = maxX;
        }

        if (y < 10) y = 10;
        if (y > 550) y = 550;
    }

    public void tryToFire() {
        long interval = game.getPlayerStats().getFiringInterval();
        if (System.currentTimeMillis() - game.lastFire < interval) {
            return;
        }
        game.lastFire = System.currentTimeMillis();

        int baseX = (int) x + 10;
        int baseY = (int) y - 30;
        double shotDX = 0;
        double shotDY = -300;

        if (game.getCurrentState() == GameState.PLAYING_SINGLE && game.getCurrentStage() != null) {
            shotDY = game.getCurrentStage().getPlayerShotVelocity();
        }

        String myUid = org.newdawn.spaceinvaders.CurrentUserManager.getInstance().getUid();

        for (int i = 0; i < game.playerStats.getMissileCount(); i++) {
            int offset = (i - (game.playerStats.getMissileCount() - 1) / 2) * 10;
            ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", baseX + offset, baseY, shotDX, shotDY);

            if (game.getCurrentState() == GameState.PLAYING_PVP || game.getCurrentState() == GameState.PLAYING_COOP) {
                shot.setOwnerUid(myUid);
            }
            game.getEntityManager().addEntity(shot);
        }

        org.newdawn.spaceinvaders.SoundManager.get().playSound("sounds/shoot.wav");
    }

    public void collidedWith(Entity other) {
        if (other instanceof AlienEntity || other instanceof BossEntity) {
            // [수정] Game 클래스가 아니라 LevelManager를 통해 사망 처리 호출
            game.getLevelManager().notifyDeath();
        }
    }
}