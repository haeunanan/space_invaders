package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
	// === dev 브랜치와 boss 브랜치의 모든 변수를 통합 ===
	private Game game;
	private double moveSpeed = 75;
	private double firingChance; // boss 브랜치의 발사 확률 변수

	// dev 브랜치의 생존 상태 변수
	private boolean alive = true;

	// dev 브랜치의 애니메이션 관련 변수
    private Sprite[] frames = new Sprite[9]; // 3x3 = 9프레임
    private long lastFrameChange;
	private long frameDuration = 180;
	private int frameNumber;

	/**
	 * 최종 통합된 생성자 (boss 브랜치 기반)
	 * 6개의 인자를 모두 받아 처리합니다.
	 */
	public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
		super(ref, x, y); // Entity의 생성자 호출

		// 애니메이션 프레임 초기화 (dev 브랜치 로직)
        frames[0] = SpriteStore.get().getSprite("sprites/mars_enemy_0.png");
        frames[1] = SpriteStore.get().getSprite("sprites/mars_enemy_1.png");
        frames[2] = SpriteStore.get().getSprite("sprites/mars_enemy_2.png");
        frames[3] = SpriteStore.get().getSprite("sprites/mars_enemy_3.png");
        frames[4] = SpriteStore.get().getSprite("sprites/mars_enemy_4.png");
        frames[5] = SpriteStore.get().getSprite("sprites/mars_enemy_5.png");
        frames[6] = SpriteStore.get().getSprite("sprites/mars_enemy_6.png");
        frames[7] = SpriteStore.get().getSprite("sprites/mars_enemy_7.png");
        frames[8] = SpriteStore.get().getSprite("sprites/mars_enemy_8.png");


        this.game = game;
		this.moveSpeed = moveSpeed;
		this.firingChance = firingChance;
		this.dx = -this.moveSpeed; // 초기 이동 방향 설정
	}

	/**
	 * move 메소드 통합
	 * dev의 애니메이션 로직과 boss의 발사 로직을 모두 포함합니다.
	 */
	@Override
	public void move(long delta) {
		// 애니메이션 프레임 변경 로직 (from dev)
		lastFrameChange += delta;
		if (lastFrameChange > frameDuration) {
			lastFrameChange = 0;
			frameNumber++;
			if (frameNumber >= frames.length) {
				frameNumber = 0;
			}
			sprite = frames[frameNumber];
		}

		// 화면 경계 체크 로직 (양쪽 공통)
		if ((dx < 0) && (x < 10)) {
			game.updateLogic();
		}
		if ((dx > 0) && (x > 750)) {
			game.updateLogic();
		}

		// 총알 발사 로직 (from boss)
		if (Math.random() < firingChance) {
			fire();
		}

		// 최종 이동 처리
		super.move(delta);
	}

	// fire 메소드 (from boss)
	private void fire() {
		game.addEntity(new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20));
	}

	// doLogic 메소드 (양쪽 공통)
	public void doLogic() {
		dx = -dx;
		y += 10;
		if (y > 570) {
			game.notifyDeath();
		}
	}

	// 생존 상태 관련 메소드 (from dev)
	public boolean isAlive() {
		return alive;
	}

	public void markDead() {
		this.alive = false;
	}

	// collidedWith 메소드 (양쪽 공통)
	public void collidedWith(Entity other) {
		// 충돌 처리는 다른 곳에서 하므로 비워둡니다.
	}
}