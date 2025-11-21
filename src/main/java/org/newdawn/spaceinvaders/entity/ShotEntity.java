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
    private double dx;
    private double dy;
	
	/**
	 * Create a new shot from the player
	 * 
	 * @param game The game in which the shot has been created
	 * @param sprite The sprite representing this shot
	 * @param x The initial x location of the shot
	 * @param y The initial y location of the shot
	 */
    public ShotEntity(Game game, String sprite, int x, int y, double dx, double dy) {
        super(sprite, x, y);
        this.game = game;
        this.dx = dx;
        this.dy = dy;
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
    @Override
    public void move(long delta) {
        x += dx * delta / 1000.0;
        y += dy * delta / 1000.0;

        // 화면 밖 나가면 제거
        if (y < -50 || y > 650) {
            game.removeEntity(this);
        }
    }

    /**
     * Notification that this shot has collided with another entity
     *
     * @param other The other entity with which we've collided
     */
    @Override
    public void collidedWith(Entity other) {
        // 1. 이미 어딘가에 부딪힌 총알이라면 중복 처리 방지
        if (used) {
            return;
        }

        // 2. 싱글 플레이 모드 로직
        if (game.getCurrentState() == Game.GameState.PLAYING_SINGLE) {
            // 적(Alien)과 충돌했는지 확인
            if (other instanceof AlienEntity) {
                AlienEntity alien = (AlienEntity) other;

                // 이미 죽은 적이면 무시
                if (!alien.isAlive()) {
                    return;
                }

                // [수정] 총알은 명중했으므로 무조건 화면에서 제거하고 사용됨 처리
                game.removeEntity(this);
                used = true;

                // [수정] 적에게 1의 데미지를 입힘
                // takeDamage(1)이 true(사망)를 반환할 때만 킬 처리를 수행
                // false(생존)를 반환하면 체력만 깎이고 피격 애니메이션이 재생됨
                if (alien.takeDamage(1)) {
                    alien.markDead();            // 적 상태를 '죽음'으로 변경
                    game.removeEntity(alien);    // 적을 화면에서 제거
                    game.notifyAlienKilled();    // 점수 획득 알림

                    // [수정] 아이템 드랍 로직
                    if (Math.random() < 0.1 && game.getCurrentStage() != null && game.getCurrentStage().isItemAllowed()) {

                        // 현재 스테이지에 설정된 아이템 이미지 가져오기
                        String itemRef = game.getCurrentStage().getItemSpriteRef();

                        ItemEntity item = new ItemEntity(
                                game,
                                itemRef, // 변수로 변경됨
                                alien.getX(),
                                alien.getY()
                        );
                        game.addEntity(item);
                    }
                }
            }
        }
        // 3. PVP 모드 로직
        else if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
            // 상대방 플레이어와 충돌했는지 확인
            if (other == game.getOpponentShip()) {
                used = true;
                game.removeEntity(this); // 총알 제거
                // PVP 모드에서는 상대방 클라이언트가 데미지를 처리하므로,
                // 여기서는 내 화면의 총알만 지워주면 됩니다.
            }
        }
    }
}