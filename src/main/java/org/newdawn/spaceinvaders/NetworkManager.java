package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkManager {
    private final Game game;
    private final FirebaseClientService clientService;
    private Thread networkThread;

    private String currentMatchId;
    private String player1_uid;
    private String player2_uid;

    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";

    public NetworkManager(Game game) {
        this.game = game;
        this.clientService = new FirebaseClientService();
    }

    // [추가] MatchmakingManager에서 호출하여 게임 정보를 세팅
    public void setMatchInfo(String matchId, String p1, String p2) {
        this.currentMatchId = matchId;
        this.player1_uid = p1;
        this.player2_uid = p2;
    }

    public void stopAllThreads() {
        if (networkThread != null && networkThread.isAlive()) {
            networkThread.interrupt();
        }
    }

    public void startNetworkLoop() {
        stopAllThreads();
        networkThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (game.getGameStateManager().isPlayingState()) {
                        sendMyStatus();
                        updateOpponentStatus();
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        networkThread.start();
    }

    private void sendMyStatus() {
        if (game.getShip() == null) return;

        String myUid = CurrentUserManager.getInstance().getUid();
        Map<String, Object> myState = new HashMap<>();
        myState.put("x", game.getShip().getX());
        myState.put("y", game.getShip().getY());

        if (game.getShip() instanceof ShipEntity) {
            myState.put(KEY_HEALTH, ((ShipEntity) game.getShip()).getCurrentHealth());
        }

        List<Map<String, Integer>> myShotsData = new ArrayList<>();
        for (Entity entity : game.getEntityManager().getEntities()) { // EntityManager 사용
            if (entity instanceof ShotEntity) {
                ShotEntity shot = (ShotEntity) entity;
                if (myUid.equals(shot.getOwnerUid())) {
                    Map<String, Integer> shotData = new HashMap<>();
                    shotData.put("x", shot.getX());
                    shotData.put("y", shot.getY());
                    myShotsData.add(shotData);
                }
            }
        }
        myState.put(KEY_SHOTS, myShotsData);

        String myPlayerNode = amIPlayer1() ? "player1_state" : "player2_state";
        clientService.updatePlayerState(currentMatchId, myPlayerNode, myState);
    }

    private void updateOpponentStatus() {
        String opponentNode = amIPlayer1() ? "player2_state" : "player1_state";
        Map<String, Object> opponentState = clientService.getOpponentState(currentMatchId, opponentNode);

        Entity opponentShip = game.getOpponentShip();
        if (opponentState != null && opponentShip != null) {
            syncPosition(opponentShip, opponentState);
            syncHealth(opponentShip, opponentState);
            updateOpponentShots(opponentState);
        }
    }

    private void syncPosition(Entity opponentShip, Map<String, Object> state) {
        if (state.get("x") instanceof Number && state.get("y") instanceof Number) {
            double opX = ((Number) state.get("x")).doubleValue();
            double opY = ((Number) state.get("y")).doubleValue();
            opponentShip.setLocation((int) opX, (int) opY);
        }
    }

    private void syncHealth(Entity opponentShip, Map<String, Object> state) {
        if (state.get(KEY_HEALTH) instanceof Number) {
            int opHealth = ((Number) state.get(KEY_HEALTH)).intValue();
            ((ShipEntity) opponentShip).setCurrentHealth(opHealth);
            checkPvpWinCondition(opHealth);
        }
    }

    private void checkPvpWinCondition(int opHealth) {
        if (opHealth <= 0 && game.getCurrentState() == GameState.PLAYING_PVP) {
            SwingUtilities.invokeLater(() -> game.getLevelManager().notifyWinPVP());
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateOpponentShots(Map<String, Object> opponentState) {
        String opponentUid = amIPlayer1() ? player2_uid : player1_uid;
        EntityManager em = game.getEntityManager(); // EntityManager 사용

        // 기존 상대방 총알 제거
        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : em.getEntities()) {
            if (entity instanceof ShotEntity && opponentUid.equals(((ShotEntity) entity).getOwnerUid())) {
                toRemove.add(entity);
            }
        }
        toRemove.forEach(em::removeEntity);

        // 새 총알 생성
        if (opponentState.get(KEY_SHOTS) instanceof List) {
            List<Map<String, Double>> shotList = (List<Map<String, Double>>) opponentState.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", sData.get("x").intValue(), sData.get("y").intValue(), 0, -300);
                shot.setOwnerUid(opponentUid);
                em.addEntity(shot);
            }
        }
    }

    public boolean amIPlayer1() {
        String myUid = CurrentUserManager.getInstance().getUid();
        return myUid != null && player1_uid != null && myUid.equals(player1_uid);
    }
}