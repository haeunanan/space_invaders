package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkStateConverter {

    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
    private static final String KEY_ALIENS = "aliens";
    private static final String KEY_HITS = "hits";
    // [추가] 새로운 키 정의
    private static final String KEY_ENEMY_SHOTS = "enemy_shots";
    private static final String KEY_ITEMS = "items";

    public Map<String, Object> createMyState(Game game, boolean isPlayer1) {
        Entity ship = game.getShip();
        if (ship == null) return new HashMap<>();

        Map<String, Object> myState = new HashMap<>();
        myState.put("x", ship.getX());
        myState.put("y", ship.getY());

        Set<String> hits = game.getNetworkManager().getAndClearPendingHitAlienIds();
        if (!hits.isEmpty()) {
            myState.put(KEY_HITS, new ArrayList<>(hits));
        }

        if (ship instanceof ShipEntity) {
            myState.put(KEY_HEALTH, ((ShipEntity) ship).getCurrentHealth());
        }

        myState.put(KEY_SHOTS, createShotData(game));

        if (isPlayer1) {
            myState.put(KEY_ALIENS, createAlienData(game));
            // [추가] 호스트는 적 총알과 아이템 정보도 전송
            myState.put(KEY_ENEMY_SHOTS, createEnemyShotData(game));
            myState.put(KEY_ITEMS, createItemData(game));

            myState.put("stage", game.getLevelManager().getStageIndex());
            myState.put("waiting", game.getLevelManager().isWaitingForKeyPress());
        }

        return myState;
    }

    // ... (기존 createShotData, mapShotToData 등 유지) ...
    private List<Map<String, Integer>> createShotData(Game game) {
        String myUid = CurrentUserManager.getInstance().getUid();
        if (myUid == null) return Collections.emptyList();
        return game.getEntityManager().getEntities().stream()
                .filter(e -> e instanceof ShotEntity)
                .map(e -> (ShotEntity) e)
                .filter(shot -> myUid.equals(shot.getOwnerUid()))
                .map(this::mapShotToData)
                .collect(Collectors.toList());
    }
    private Map<String, Integer> mapShotToData(ShotEntity shot) {
        Map<String, Integer> data = new HashMap<>();
        data.put("x", shot.getX());
        data.put("y", shot.getY());
        return data;
    }
    private List<Map<String, Object>> createAlienData(Game game) {
        return game.getEntityManager().getEntities().stream()
                .filter(e -> e instanceof AlienEntity)
                .map(e -> (AlienEntity) e)
                .map(this::mapAlienToData)
                .collect(Collectors.toList());
    }
    private Map<String, Object> mapAlienToData(AlienEntity alien) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", alien.getNetworkId());
        data.put("x", alien.getX());
        data.put("y", alien.getY());
        data.put("ref", alien.getSpriteRef());
        return data;
    }

    // [추가] 적 총알 데이터 생성
    private List<Map<String, Object>> createEnemyShotData(Game game) {
        return game.getEntityManager().getEntities().stream()
                .filter(e -> (e instanceof AlienShotEntity) || (e instanceof AlienIceShotEntity) || (e instanceof BossShotEntity))
                .map(this::mapEnemyShotToData)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapEnemyShotToData(Entity shot) {
        Map<String, Object> data = new HashMap<>();
        data.put("x", shot.getX());
        data.put("y", shot.getY());

        // 총알 종류 구분
        if (shot instanceof AlienIceShotEntity) data.put("type", "ice");
        else if (shot instanceof BossShotEntity) data.put("type", "boss");
        else data.put("type", "normal");

        return data;
    }

    // [추가] 아이템 데이터 생성
    private List<Map<String, Object>> createItemData(Game game) {
        return game.getEntityManager().getEntities().stream()
                .filter(e -> e instanceof ItemEntity)
                .map(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("x", e.getX());
                    data.put("y", e.getY());
                    // 아이템 이미지는 스테이지마다 다르므로 현재 스테이지에서 참조하거나 아이템 객체에서 가져올 수 있음
                    // 여기서는 ItemEntity 생성 시 사용된 ref를 가져올 수 없으므로(private),
                    // 스테이지 정보를 통해 추론하거나, ItemEntity에 getSpriteRef를 추가하는 것이 좋으나,
                    // 간단히 스테이지 정보를 이용합니다.
                    data.put("ref", game.getCurrentStage().getItemSpriteRef());
                    return data;
                })
                .collect(Collectors.toList());
    }
}