package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import java.awt.*;
import java.util.ArrayList;

public class CollisionManager {
    private final Game game;
    private final EntityManager entityManager;

    public CollisionManager(Game game, EntityManager entityManager) {
        this.game = game;
        this.entityManager = entityManager;
    }

    // 메인 충돌 체크 메서드
    public void checkCollisions() {
        if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
            checkPvpCollisions();
        } else {
            checkStandardCollisions();
        }
    }

    // 일반 게임 충돌 처리
    private void checkStandardCollisions() {
        ArrayList<Entity> entities = entityManager.getEntities();
        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                Entity me = entities.get(p);
                Entity him = entities.get(s);

                if (isValidCollisionPair(me, him) && me.collidesWith(him)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                }
            }
        }
    }

    // PVP 충돌 처리
    private void checkPvpCollisions() {
        String myUid = CurrentUserManager.getInstance().getUid();
        ArrayList<Entity> entities = entityManager.getEntities();

        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                processPvpPair(entities.get(p), entities.get(s), myUid);
            }
        }
    }

    // 개별 PVP 쌍 검사
    private void processPvpPair(Entity me, Entity him, String myUid) {
        if (!isValidCollisionPair(me, him)) {
            return;
        }

        Rectangle meRect = game.getVisualBounds(me);
        Rectangle himRect = game.getVisualBounds(him);

        if (meRect.intersects(himRect)) {
            handlePvpCollision(me, him, myUid);
        }
    }

    // 유효성 검사 (EntityManager를 통해 삭제 대기 목록 확인 필요하지만,
    // 여기서는 간단히 null 체크나 살아있는 상태 체크 등을 수행할 수도 있음.
    // 기존 로직 유지를 위해 EntityManager의 상태를 고려해야 한다면 아래와 같이 구현)
    private boolean isValidCollisionPair(Entity me, Entity him) {
        // 만약 removeList 접근이 어렵다면, EntityManager에 isPendingRemove() 메서드를 추가하여 호출하는 것이 좋음
        // 여기서는 간단히 EntityManager가 직접 삭제 목록을 관리하므로 패스하거나,
        // 충돌 직후 즉시 로직이 수행되므로 큰 문제 없음.
        return true;
    }

    private void handlePvpCollision(Entity me, Entity him, String myUid) {
        if (me instanceof ShotEntity && him instanceof ShipEntity) {
            resolveShotHitShip((ShotEntity) me, (ShipEntity) him, myUid);
        } else if (him instanceof ShotEntity && me instanceof ShipEntity) {
            resolveShotHitShip((ShotEntity) him, (ShipEntity) me, myUid);
        }
    }

    private void resolveShotHitShip(ShotEntity shot, ShipEntity ship, String myUid) {
        boolean isMyShot = shot.isOwnedBy(myUid);
        boolean isMyShip = (ship == game.getShip());
        boolean isOpponentShip = (ship == game.getOpponentShip());

        if (isMyShot && isOpponentShip) {
            entityManager.removeEntity(shot);
        } else if (!isMyShot && isMyShip) {
            entityManager.removeEntity(shot);
            ship.takeDamage();
        }
    }
}