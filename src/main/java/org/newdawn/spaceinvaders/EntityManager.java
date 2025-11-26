package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import java.util.ArrayList;

public class EntityManager {
    private Game game;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> removeList = new ArrayList<>();

    // [추가] 충돌 담당 매니저
    private CollisionManager collisionManager;

    public EntityManager(Game game) {
        this.game = game;
        // [추가] CollisionManager 초기화
        this.collisionManager = new CollisionManager(game, this);
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

    // [기존 moveEntities 교체]
    public void moveEntities(long delta) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (shouldMove(entity)) { // [분리] 조건 체크
                entity.move(delta);
            }
        }
    }

    // [새로 추가] 이동 가능 여부 판단
    private boolean shouldMove(Entity entity) {
        // PVP 모드일 때, 상대방 우주선은 로컬에서 이동시키지 않음 (네트워크 동기화 의존)
        if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_PVP
                && entity == game.getOpponentShip()) {
            return false;
        }
        return true;
    }

    // [수정] 직접 처리하던 로직을 매니저에게 위임
    public void checkCollisions() {
        collisionManager.checkCollisions();
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