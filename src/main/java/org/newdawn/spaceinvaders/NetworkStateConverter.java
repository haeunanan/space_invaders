package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkStateConverter {

    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
    private static final String KEY_ALIENS = "aliens";
    private static final String KEY_HITS = "hits";


    /**
     * 현재 클라이언트의 게임 상태(기체, 총알, 적 등)를 Map 형태로 변환합니다.
     * @param game 게임 인스턴스
     * @param isPlayer1 현재 플레이어가 Player 1(호스트)인지 여부
     * @return 전송할 상태 데이터 Map
     */
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

        // 스트림 메소드 호출로 대체
        myState.put(KEY_SHOTS, createShotData(game));

        if (isPlayer1) {
            myState.put(KEY_ALIENS, createAlienData(game));

            // [추가] 호스트의 스테이지 정보와 대기 상태를 패킷에 포함
            myState.put("stage", game.getLevelManager().getStageIndex());
            myState.put("waiting", game.getLevelManager().isWaitingForKeyPress());
        }

        return myState;
    }

    private List<Map<String, Integer>> createShotData(Game game) {
        String myUid = CurrentUserManager.getInstance().getUid();
        if (myUid == null) return Collections.emptyList();

        // Stream을 사용하여 반복문과 조건문 제거
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
        // Stream을 사용하여 Alien 변환 로직 간소화
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
        // [수정] 실제 적의 이미지 경로를 담아서 보냄
        data.put("ref", alien.getSpriteRef());
        return data;
    }
}