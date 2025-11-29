package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import java.util.List;
import java.util.Map;

import static org.newdawn.spaceinvaders.NetworkStateConverter.KEY_COINS;
import static org.newdawn.spaceinvaders.NetworkStateConverter.KEY_SCORE;

public class NetworkSyncHelper {
    private final Game game;
    private final NetworkEntityParser entityParser;
    private final NetworkShipSynchronizer shipSynchronizer; // [추가]

    private static final String KEY_ALIENS = "aliens";
    private static final String KEY_HITS = "hits";
    private static final String KEY_ENEMY_SHOTS = "enemy_shots";
    private static final String KEY_ITEMS = "items";

    public NetworkSyncHelper(Game game) {
        this.game = game;
        this.entityParser = new NetworkEntityParser(game);
        this.shipSynchronizer = new NetworkShipSynchronizer(game); // 초기화
    }

    public void syncOpponent(String matchId, boolean amIPlayer1, Map<String, Object> opponentState) {
        Entity opponentShip = game.getOpponentShip();
        if (opponentState == null || opponentShip == null) return;

        // [변경] Ship 관련 동기화 위임 (여기서 WMC 대폭 감소)
        shipSynchronizer.syncBasicShipState(opponentShip, opponentState, amIPlayer1);

        if (amIPlayer1) {
            syncOpponentHits(opponentState);
        } else {
            syncGuestWorldState(opponentState);
        }
    }

    private void syncOpponentHits(Map<String, Object> state) {
        if (state.containsKey(KEY_HITS)) {
            processOpponentHits((List<String>) state.get(KEY_HITS));
        }
    }

    private void syncGuestWorldState(Map<String, Object> state) {
        syncEconomy(state);
        syncWorldEntities(state);
        syncStageFlow(state);
    }

    private void syncEconomy(Map<String, Object> state) {
        if (state.containsKey(KEY_SCORE)) {
            game.getPlayerStats().setScore(((Number) state.get(KEY_SCORE)).intValue());
        }
        if (state.containsKey(KEY_COINS)) {
            game.getPlayerStats().setCoins(((Number) state.get(KEY_COINS)).intValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void syncWorldEntities(Map<String, Object> state) {
        if (state.containsKey(KEY_ALIENS)) {
            entityParser.parseAndSyncAliens((List<Map<String, Object>>) state.get(KEY_ALIENS));
        }
        if (state.containsKey(KEY_ENEMY_SHOTS)) {
            entityParser.parseAndSyncEnemyShots((List<Map<String, Object>>) state.get(KEY_ENEMY_SHOTS));
        }
        if (state.containsKey(KEY_ITEMS)) {
            entityParser.parseAndSyncItems((List<Map<String, Object>>) state.get(KEY_ITEMS));
        }
    }

    private void syncStageFlow(Map<String, Object> state) {
        LevelManager lm = game.getLevelManager();
        boolean hostWaiting = Boolean.TRUE.equals(state.get("waiting"));
        if (hostWaiting && !lm.isWaitingForKeyPress()) {
            lm.setMessage("Stage Clear! Waiting for Host...");
            lm.setWaitingForKeyPress(true);
        }

        if (state.containsKey("stage")) {
            int hostStage = ((Number) state.get("stage")).intValue();
            if (hostStage != 0 && hostStage != lm.getStageIndex()) {
                System.out.println("Syncing Stage to Host: " + hostStage);
                lm.setStageIndex(hostStage);
                lm.nextStage();
                lm.setWaitingForKeyPress(false);
            }
        }
    }

    private void processOpponentHits(List<String> hitIds) {
        EntityManager em = game.getEntityManager();
        for (Entity e : em.getEntities()) {
            if (e instanceof AlienEntity) {
                AlienEntity alien = (AlienEntity) e;
                if (hitIds.contains(alien.getNetworkId()) && alien.takeDamage(1)) {
                    alien.markDead();
                    em.removeEntity(alien);
                    game.getResultHandler().notifyAlienKilled(); // [변경] 직접 호출
                    SoundManager.get().playSound("sounds/explosion.wav");
                }
            }
        }
    }
}