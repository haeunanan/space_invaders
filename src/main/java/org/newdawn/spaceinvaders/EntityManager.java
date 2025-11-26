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

    public void moveEntities(long delta) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            if (game.getCurrentState() == Gamestate.PLAYING_PVP && entity == game.getOpponentShip()) {
                continue;
            }
            entity.move(delta);
        }
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