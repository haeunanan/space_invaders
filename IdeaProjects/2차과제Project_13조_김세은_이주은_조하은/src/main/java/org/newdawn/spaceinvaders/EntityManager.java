package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class EntityManager {
    private Game game;
    private CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Entity> removeList = new CopyOnWriteArrayList<>();

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

    public List<Entity> getEntities() {
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

    private boolean shouldMove(Entity entity) {
        // 상대방 우주선은 로컬 로직(키보드)으로 움직이지 않음 -> Interpolation 로직인 entity.move()는 호출되어야 함
        // 따라서 shouldMove는 true를 반환하되, ShipEntity.move() 내부에서 분기 처리를 해야 합니다.

    /* [중요 수정]
       기존 코드에서는 opponentShip이면 move()를 안 부르고 있었을 수 있습니다.
       보간법(Interpolation)을 쓰려면 opponentShip도 move()가 매 프레임 호출되어야 합니다.
       따라서 아래와 같이 수정이 필요할 수 있습니다.
    */

        // 기존 로직:
        // if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_PVP
        //        && entity == game.getOpponentShip()) {
        //    return false; // <-- 이것 때문에 move()가 호출 안 될 수 있음
        // }

        return true; // 모든 엔티티의 move()를 호출하도록 변경하고,
        // ShipEntity.move() 내부에서 내것/상대것을 구분하는 것이 좋습니다.
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