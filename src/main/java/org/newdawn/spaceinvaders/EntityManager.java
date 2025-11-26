package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.AlienEntity;

import java.awt.*;
import java.util.ArrayList;

public class EntityManager {
    private Game game;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> removeList = new ArrayList<>();

    public EntityManager(Game game) {
        this.game = game;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public void clear() {
        entities.clear();
        removeList.clear();
    }

    public int getAlienCount() {
        int count = 0;
        for (Entity e : entities) {
            if (e instanceof AlienEntity) count++;
        }
        return count;
    }

    // --- 게임 루프에서 호출될 메서드들 ---

    public void moveEntities(long delta) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            // PVP 상대방 배는 네트워크가 위치를 잡으므로 이동 로직 제외
            if (game.getCurrentState() == Game.GameState.PLAYING_PVP && entity == game.getOpponentShip()) {
                continue;
            }
            entity.move(delta);
        }
    }

    public void checkCollisions() {
        // 1. 일반 충돌 (Entity끼리)
        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                Entity me = entities.get(p);
                Entity him = entities.get(s);

                if (removeList.contains(me) || removeList.contains(him)) continue;
                if (game.getCurrentState() == Game.GameState.PLAYING_PVP) continue; // PVP는 별도 처리

                if (me.collidesWith(him)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                }
            }
        }

        // 2. PVP 전용 충돌 (Game 클래스의 로직을 가져오거나, 여기서 처리)
        if (game.getCurrentState() == Game.GameState.PLAYING_PVP) {
            checkPvpCollisions();
        }
    }

    private void checkPvpCollisions() {
        String myUid = CurrentUserManager.getInstance().getUid();
        for (int p = 0; p < entities.size(); p++) {
            for (int s = p + 1; s < entities.size(); s++) {
                Entity me = entities.get(p);
                Entity him = entities.get(s);

                if (removeList.contains(me) || removeList.contains(him)) continue;

                // 충돌 감지 로직 (Game.java에 있던 getVisualBounds 활용 필요)
                // 편의상 여기서는 로직을 단순화하거나 Game의 메서드를 호출해야 함
                // 구조상 Game.getVisualBounds를 public으로 열고 여기서 호출하는 것이 빠름
                Rectangle meRect = game.getVisualBounds(me);
                Rectangle himRect = game.getVisualBounds(him);

                if (meRect.intersects(himRect)) {
                    // PVP 충돌 처리 로직 (Game.java에 있던 handlePvpCollision 내용을 여기로 이동)
                    handlePvpCollision(me, him, myUid);
                }
            }
        }
    }

    private void handlePvpCollision(Entity me, Entity him, String myUid) {
        Entity ship = game.getShip();
        Entity opponentShip = game.getOpponentShip();

        if (me instanceof ShotEntity && ((ShotEntity)me).isOwnedBy(myUid) && him == opponentShip) {
            removeEntity(me);
        } else if (him instanceof ShotEntity && ((ShotEntity)him).isOwnedBy(myUid) && me == opponentShip) {
            removeEntity(him);
        } else if (me instanceof ShotEntity && !((ShotEntity)me).isOwnedBy(myUid) && him == ship) {
            removeEntity(me);
            ((ShipEntity)him).takeDamage();
        } else if (him instanceof ShotEntity && !((ShotEntity)him).isOwnedBy(myUid) && me == ship) {
            removeEntity(him);
            ((ShipEntity)me).takeDamage();
        }
    }

    public void removeDeadEntities() {
        entities.removeAll(removeList);
        removeList.clear();
    }

    public void doLogic() {
        for (Entity entity : entities) {
            entity.doLogic();
        }
    }
}
