package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import javax.swing.SwingUtilities;
import java.util.*;
import java.util.stream.Collectors;

import static org.newdawn.spaceinvaders.NetworkStateConverter.KEY_COINS;
import static org.newdawn.spaceinvaders.NetworkStateConverter.KEY_SCORE;

public class NetworkSyncHelper {
    private final Game game;
    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
    private static final String KEY_ALIENS = "aliens";
    private static final String KEY_HITS = "hits";
    private static final String KEY_ENEMY_SHOTS = "enemy_shots";
    private static final String KEY_ITEMS = "items";

    public NetworkSyncHelper(Game game) {
        this.game = game;
    }

    public void syncOpponent(String matchId, boolean amIPlayer1, Map<String, Object> opponentState) {
        Entity opponentShip = game.getOpponentShip();

        if (opponentState == null || opponentShip == null) return;

        syncPosition(opponentShip, opponentState);
        syncHealth(opponentShip, opponentState);
        updateOpponentShots(opponentState, amIPlayer1);

        if (amIPlayer1 && opponentState.containsKey(KEY_HITS)) {
            processOpponentHits((List<String>) opponentState.get(KEY_HITS));
        }

        if (!amIPlayer1) {
            if (opponentState.containsKey(KEY_SCORE)) {
                int score = ((Number) opponentState.get(KEY_SCORE)).intValue();
                game.getPlayerStats().setScore(score);
            }
            if (opponentState.containsKey(KEY_COINS)) {
                int coins = ((Number) opponentState.get(KEY_COINS)).intValue();
                game.getPlayerStats().setCoins(coins);
            }
            if (opponentState.containsKey(KEY_ALIENS)) {
                syncAliens((List<Map<String, Object>>) opponentState.get(KEY_ALIENS));
            }
            if (opponentState.containsKey(KEY_ENEMY_SHOTS)) {
                syncEnemyShots((List<Map<String, Object>>) opponentState.get(KEY_ENEMY_SHOTS));
            }
            if (opponentState.containsKey(KEY_ITEMS)) {
                syncItems((List<Map<String, Object>>) opponentState.get(KEY_ITEMS));
            }

            LevelManager lm = game.getLevelManager();
            boolean hostWaiting = Boolean.TRUE.equals(opponentState.get("waiting"));
            if (hostWaiting && !lm.isWaitingForKeyPress()) {
                lm.setMessage("Stage Clear! Waiting for Host...");
                lm.setWaitingForKeyPress(true);
            }

            if (opponentState.containsKey("stage")) {
                int hostStage = ((Number) opponentState.get("stage")).intValue();
                if (hostStage != 0 && hostStage != lm.getStageIndex()) {
                    System.out.println("Syncing Stage to Host: " + hostStage);
                    lm.setStageIndex(hostStage);
                    lm.nextStage();
                    lm.setWaitingForKeyPress(false);
                }
            }
        }
    }

    private void processOpponentHits(List<String> hitIds) {
        EntityManager em = game.getEntityManager();
        for (Entity e : em.getEntities()) {
            if (e instanceof AlienEntity) {
                AlienEntity alien = (AlienEntity) e;
                if (hitIds.contains(alien.getNetworkId())) {
                    if (alien.takeDamage(1)) {
                        alien.markDead();
                        em.removeEntity(alien);
                        game.getLevelManager().notifyAlienKilled();
                        SoundManager.get().playSound("sounds/explosion.wav");
                    }
                }
            }
        }
    }

    private void syncPosition(Entity opponentShip, Map<String, Object> state) {
        if (state.get("x") instanceof Number && state.get("y") instanceof Number) {
            double opX = ((Number) state.get("x")).doubleValue();
            double opY = ((Number) state.get("y")).doubleValue();

            if (opponentShip instanceof ShipEntity) {
                ((ShipEntity) opponentShip).setTargetLocation(opX, opY);
            } else {
                opponentShip.setLocation((int) opX, (int) opY);
            }
        }
    }

    private void syncHealth(Entity opponentShip, Map<String, Object> state) {
        if (state.get(KEY_HEALTH) instanceof Number) {
            int opHealth = ((Number) state.get(KEY_HEALTH)).intValue();
            ((ShipEntity) opponentShip).setCurrentHealth(opHealth);

            if (opHealth <= 0 && game.getCurrentState() == GameState.PLAYING_PVP) {
                SwingUtilities.invokeLater(() -> game.getLevelManager().notifyWinPVP());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateOpponentShots(Map<String, Object> opponentState, boolean amIPlayer1) {
        String opponentUid = amIPlayer1 ?
                game.getNetworkManager().getPlayer2Uid() :
                game.getNetworkManager().getPlayer1Uid();

        if (opponentUid == null) return;

        EntityManager em = game.getEntityManager();
        List<Entity> toRemove = em.getEntities().stream()
                .filter(e -> e instanceof ShotEntity)
                .map(e -> (ShotEntity) e)
                .filter(s -> opponentUid.equals(s.getOwnerUid()))
                .collect(Collectors.toList());
        toRemove.forEach(em::removeEntity);

        if (opponentState.get(KEY_SHOTS) instanceof List) {
            List<Map<String, Double>> shotList = (List<Map<String, Double>>) opponentState.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                int sx = sData.get("x").intValue();
                int sy = sData.get("y").intValue();
                ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", sx, sy, 0, -300);
                shot.setOwnerUid(opponentUid);
                em.addEntity(shot);
            }
        }
    }

    private void syncAliens(List<Map<String, Object>> serverAliens) {
        if (serverAliens == null) return;
        EntityManager em = game.getEntityManager();
        List<Entity> localEntities = em.getEntities();
        Set<String> serverIds = new HashSet<>();

        for (Map<String, Object> data : serverAliens) {
            String id = (String) data.get("id");
            serverIds.add(id);

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
                // [수정] 서버의 이미지 참조값(ref)이 로컬과 다르면 업데이트 (갑옷 깨짐 동기화)
                if (!entity.getSpriteRef().equals(ref)) {
                    entity.updateSpriteRef(ref);
                }
            } else {
                AlienEntity newAlien = new AlienEntity(game, ref, (int)x, (int)y, 0, 0);
                newAlien.setNetworkId(id);
                // 천왕성 에일리언 등의 특수 로직을 위해 스테이지 체크 후 형변환 생성 고려 가능하나,
                // 기본적으로 SpriteRef가 맞으면 이미지는 맞게 나옴.
                // 완벽한 동작을 위해선 UranusAlienEntity로 생성되어야 함.
                // 여기선 단순화를 위해 AlienEntity로 생성하되, updateSpriteRef가 작동하도록 함.
                if (game.getCurrentStage() instanceof org.newdawn.spaceinvaders.stage.UranusStage) {
                    // 이미 존재하는 객체를 제거하고 다시 만들거나, Factory 패턴 사용 필요.
                    // 하지만 현재 구조상 AlienEntity의 updateSpriteRef가 작동하면 이미지는 바뀜.
                    // 기믹(갑옷 깨짐 애니메이션)을 위해선 아래 updateSpriteRef 호출이 중요함.
                    newAlien = new UranusAlienEntity(game, ref, (int)x, (int)y, 0, 0);
                    newAlien.setNetworkId(id);
                }
                em.addEntity(newAlien);
            }
        }

        List<Entity> toRemove = localEntities.stream()
                .filter(e -> e instanceof AlienEntity)
                .map(e -> (AlienEntity) e)
                .filter(a -> !serverIds.contains(a.getNetworkId()))
                .collect(Collectors.toList());

        toRemove.forEach(em::removeEntity);
    }

    private void syncEnemyShots(List<Map<String, Object>> shotData) {
        EntityManager em = game.getEntityManager();
        List<Entity> toRemove = em.getEntities().stream()
                .filter(e -> (e instanceof AlienShotEntity) || (e instanceof AlienIceShotEntity) || (e instanceof BossShotEntity))
                .collect(Collectors.toList());
        toRemove.forEach(em::removeEntity);

        for (Map<String, Object> data : shotData) {
            int x = ((Number) data.get("x")).intValue();
            int y = ((Number) data.get("y")).intValue();
            String type = (String) data.get("type");

            Entity newShot;
            if ("ice".equals(type)) {
                newShot = new AlienIceShotEntity(game, "sprites/ice_shot.png", x, y);
            } else if ("boss".equals(type)) {
                newShot = new BossShotEntity(game, Constants.BOSS_SHOT_SPRITE, x, y, 0);
            } else {
                newShot = new AlienShotEntity(game, "sprites/alien_shot.gif", x, y);
            }
            em.addEntity(newShot);
        }
    }

    private void syncItems(List<Map<String, Object>> itemData) {
        EntityManager em = game.getEntityManager();
        List<Entity> toRemove = em.getEntities().stream()
                .filter(e -> e instanceof ItemEntity)
                .collect(Collectors.toList());
        toRemove.forEach(em::removeEntity);

        for (Map<String, Object> data : itemData) {
            int x = ((Number) data.get("x")).intValue();
            int y = ((Number) data.get("y")).intValue();
            String ref = (String) data.get("ref");
            em.addEntity(new ItemEntity(game, ref, x, y));
        }
    }
}