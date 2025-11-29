package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import java.util.*;

public class NetworkEntityParser {
    private final Game game;

    public NetworkEntityParser(Game game) {
        this.game = game;
    }

    // --- Alien(외계인) 동기화 ---
    public void parseAndSyncAliens(List<Map<String, Object>> alienData) {
        if (alienData == null) return;

        EntityManager em = game.getEntityManager();
        List<Entity> localEntities = em.getEntities();
        Set<String> serverIds = new HashSet<>();

        // 1. 서버 데이터를 순회하며 로컬 엔티티 생성 또는 갱신
        for (Map<String, Object> data : alienData) {
            String id = (String) data.get("id");
            serverIds.add(id);
            syncSingleAlien(em, localEntities, data, id);
        }

        // 2. 서버에 존재하지 않는 로컬 좀비 에일리언 제거
        localEntities.removeIf(e ->
                e instanceof AlienEntity && !serverIds.contains(((AlienEntity) e).getNetworkId())
        );
    }

    private void syncSingleAlien(EntityManager em, List<Entity> localEntities, Map<String, Object> data, String id) {
        double x = ((Number) data.get("x")).doubleValue();
        double y = ((Number) data.get("y")).doubleValue();
        String ref = (String) data.getOrDefault("ref", "sprites/alien.gif");

        Optional<AlienEntity> localAlien = localEntities.stream()
                .filter(e -> e instanceof AlienEntity)
                .map(e -> (AlienEntity) e)
                .filter(a -> a.getNetworkId().equals(id))
                .findFirst();

        if (localAlien.isPresent()) {
            AlienEntity entity = localAlien.get();
            entity.setLocation((int) x, (int) y);
            // 이미지 참조가 변경되었으면 업데이트 (예: 갑옷 깨짐 등)
            if (!entity.getSpriteRef().equals(ref)) {
                entity.updateSpriteRef(ref);
            }
        } else {
            createAlien(em, id, ref, x, y);
        }
    }

    private void createAlien(EntityManager em, String id, String ref, double x, double y) {
        AlienEntity newAlien;
        // 천왕성 스테이지의 경우 특수 에일리언 생성
        if (game.getCurrentStage() instanceof org.newdawn.spaceinvaders.stage.UranusStage) {
            newAlien = new UranusAlienEntity(game, ref, (int) x, (int) y, 0, 0);
        } else {
            newAlien = new AlienEntity(game, ref, (int) x, (int) y, 0, 0);
        }
        newAlien.setNetworkId(id);
        em.addEntity(newAlien);
    }

    // --- Enemy Shots(적 탄환) 동기화 ---
    public void parseAndSyncEnemyShots(List<Map<String, Object>> shotData) {
        if (shotData == null) return;

        EntityManager em = game.getEntityManager();

        // 기존 적 총알 일괄 제거 (위치 보정보다 재생성이 효율적일 수 있음)
        // 만약 부드러운 이동이 필요하다면 ID 부여 후 Alien처럼 동기화해야 하지만,
        // 탄환은 수가 많고 생명주기가 짧아 보통 일괄 갱신 방식을 많이 사용합니다.
        em.getEntities().removeIf(e ->
                (e instanceof AlienShotEntity) || (e instanceof AlienIceShotEntity) || (e instanceof BossShotEntity)
        );

        for (Map<String, Object> data : shotData) {
            createEnemyShot(em, data);
        }
    }

    private void createEnemyShot(EntityManager em, Map<String, Object> data) {
        int x = ((Number) data.get("x")).intValue();
        int y = ((Number) data.get("y")).intValue();
        String type = (String) data.get("type");

        Entity newShot;
        if ("ice".equals(type)) {
            newShot = new AlienIceShotEntity(game, "sprites/ice_shot.png", x, y);
        } else if ("boss".equals(type)) {
            newShot = new BossShotEntity(game, Constants.BOSS_SHOT_SPRITE, x, y, 0);
        } else {
            // 기본 탄환
            newShot = new AlienShotEntity(game, "sprites/alien_shot.gif", x, y);
        }
        em.addEntity(newShot);
    }

    // --- Items(아이템) 동기화 ---
    public void parseAndSyncItems(List<Map<String, Object>> itemData) {
        if (itemData == null) return;

        EntityManager em = game.getEntityManager();

        // 기존 아이템 제거
        em.getEntities().removeIf(e -> e instanceof ItemEntity);

        for (Map<String, Object> data : itemData) {
            int x = ((Number) data.get("x")).intValue();
            int y = ((Number) data.get("y")).intValue();
            String ref = (String) data.get("ref");

            em.addEntity(new ItemEntity(game, ref, x, y));
        }
    }
}