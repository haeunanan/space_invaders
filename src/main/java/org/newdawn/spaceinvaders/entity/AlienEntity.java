package org.newdawn.spaceinvaders.entity;

import org.newdawn.spaceinvaders.Constants;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.Sprite;
import org.newdawn.spaceinvaders.SpriteStore;

public class AlienEntity extends Entity {
	private double moveSpeed = 75;
	private double firingChance;
	private boolean alive = true;
	private String bulletType = "NORMAL"; // 총알 타입 (NORMAL, ICE)
	private String spriteRef;
	private boolean isNeptuneMob = false;
	private boolean isDashing = false;
	private long dashTimer = 0;

	// [복구] 기본 애니메이션(움직임)을 위한 변수들
	private Sprite[] frames = new Sprite[4]; // 4프레임 배열
	private long lastFrameChange = 0;
	private long frameDuration = 250; // 프레임 전환 속도 (0.25초)
	private int frameNumber = 0;

	// [추가] 체력 시스템 (목성)
	private int hp = 1;

	// [추가] 피격 애니메이션용 변수 (목성)
	private Sprite normalSprite; // 평상시 모습 (현재 프레임)
	private Sprite hitSprite;    // 맞았을 때 모습
	private long hitTimer = 0;   // 피격 상태 지속 시간

	// AlienEntity 생성자 내부

	public AlienEntity(Game game, String ref, int x, int y, double moveSpeed, double firingChance) {
		super(ref, x, y);

		this.spriteRef = ref; // 이미지 경로 저장 (나중에 갑옷 깨질 때 씀)

		this.game = game;
		this.moveSpeed = moveSpeed;
		this.firingChance = firingChance;
		this.dx = -this.moveSpeed;

		// ==========================================
		// 1. 기본 애니메이션 설정
		// ==========================================
		// 일단 모든 프레임을 기본 이미지로 초기화 (움직임 없음)
		frames[0] = sprite;
		frames[1] = sprite;
		frames[2] = sprite;
		frames[3] = sprite;

		// [핵심 로직] 천왕성(uranus)이 아닐 때만 움직임 애니메이션(_2)을 적용합니다.
		// -> 토성(saturn)은 여기에 걸려서 애니메이션이 적용됩니다.
		if (!ref.contains("uranus")) {
			String ref2 = ref.replace(".", "_2.");
			java.net.URL url = this.getClass().getClassLoader().getResource(ref2);

			if (url != null) {
				Sprite sprite2 = SpriteStore.get().getSprite(ref2);
				// 1, 3번 프레임 교체 -> 움직이는 효과 발생
				frames[1] = sprite2;
				frames[3] = sprite2;
			}
		}
		if (ref.contains("neptune")) {
			this.isNeptuneMob = true;
		}


		// [삭제 완료] 여기에 있던 중복 코드는 지워졌습니다.

		// ==========================================
		// 2. 피격(Hit) 스프라이트 설정
		// ==========================================
		this.normalSprite = this.sprite;

		String hitRef = ref.replace(".", "_hit.");
		try {
			java.net.URL hitUrl = this.getClass().getClassLoader().getResource(hitRef);
			if (hitUrl != null) {
				this.hitSprite = SpriteStore.get().getSprite(hitRef);
			} else {
				this.hitSprite = this.normalSprite;
			}
		} catch (Exception e) {
			this.hitSprite = this.normalSprite;
		}
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public void setBulletType(String type) {
		this.bulletType = type;
	}

	/**
	 * 데미지 처리 메소드
	 * @return 죽었으면 true
	 */
	public boolean takeDamage(int damage) {
		this.hp -= damage;

		// [추가] 천왕성 적 기믹: 체력이 1이 남으면(갑옷이 깨지면) 이미지 변경
		if (this.hp == 1 && this.spriteRef != null && this.spriteRef.contains("uranus")) {
			// alien_uranus.gif -> alien_uranus_2.gif 로 경로 변경
			String brokenRef = this.spriteRef.replace(".", "_2.");

			try {
				// 새 이미지 로드
				Sprite brokenSprite = SpriteStore.get().getSprite(brokenRef);

				// 현재 모습과 평상시 모습(normalSprite)을 모두 깨진 상태로 변경
				this.sprite = brokenSprite;
				this.normalSprite = brokenSprite;

				// 애니메이션 프레임도 모두 깨진 상태로 고정 (움직여도 갑옷 안 생기게)
				for (int i = 0; i < frames.length; i++) {
					frames[i] = brokenSprite;
				}
			} catch (Exception e) {
				System.err.println("Broken armor sprite not found: " + brokenRef);
			}
		}

		// 피격 애니메이션 발동 (0.1초간)
		this.hitTimer = 100;
		// 현재 보여지는 스프라이트를 피격 이미지로 즉시 교체
		this.sprite = this.hitSprite;

		if (this.hp <= 0) {
			this.alive = false;
			return true; // 사망
		}
		return false; // 생존
	}

    @Override
    public void move(long delta) {
        // 1. 해왕성 적 특수 패턴 처리
        updateNeptuneBehavior(delta);

        // 2. 애니메이션 및 피격 효과 업데이트
        updateAnimation(delta);

        // 3. 화면 경계 체크
        checkBoundaries();

        // 4. 발사 시도
        tryToFire();

        // 5. 실제 위치 이동
        super.move(delta);
    }

    private void updateNeptuneBehavior(long delta) {
        if (isNeptuneMob && alive) {
            if (isDashing) {
                // 대시 중: 속도를 매우 빠르게!
                y += (moveSpeed * 3) * delta / 1000.0;
                dashTimer -= delta;
                if (dashTimer <= 0) {
                    isDashing = false;
                }
            } else if (Math.random() < 0.0002) {
                // 평상시: 아주 낮은 확률로 대시 시작
                isDashing = true;
                dashTimer = 500;
            }
        }
    }

    private void updateAnimation(long delta) {
        // 피격 타이머가 작동 중이면 프레임 변경을 하지 않음
        if (hitTimer > 0) {
            hitTimer -= delta;
            if (hitTimer <= 0) {
                this.sprite = frames[frameNumber]; // 원래 프레임 복구
            }
        } else {
            // 기본 애니메이션 수행
            lastFrameChange += delta;
            if (lastFrameChange > frameDuration) {
                lastFrameChange = 0;
                frameNumber++;
                if (frameNumber >= frames.length) {
                    frameNumber = 0;
                }
                this.sprite = frames[frameNumber];
                this.normalSprite = this.sprite;
            }
        }
    }

    private void tryToFire() {
        if (Math.random() < firingChance) {
            fire();
        }
    }

    private void checkBoundaries() {
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        if ((dx > 0) && (x > Constants.WINDOW_WIDTH - 50)) {
            game.updateLogic();
        }
    }

	private void fire() {
		if ("ICE".equals(bulletType)) {
			// 얼음탄 발사 (새로 만들 클래스)
			game.addEntity(new AlienIceShotEntity(game, "sprites/ice_shot.png", getX() + 10, getY() + 20));
		} else {
			// 일반탄 발사
			game.addEntity(new AlienShotEntity(game, "sprites/alien_shot.gif", getX() + 10, getY() + 20));
		}
	}


	public void doLogic() {
		dx = -dx;
		y += 10;
		if (y > 570) {
			game.notifyDeath();
		}
	}

	public boolean isAlive() {
		return alive;
	}

	public void markDead() {
		this.alive = false;
	}

	public void collidedWith(Entity other) {
		// 충돌 로직은 ShotEntity 등에서 처리
	}
}