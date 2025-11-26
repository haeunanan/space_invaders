package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class NetworkManager {
    private final Game game;
    private final FirebaseClientService clientService;

    private Thread matchmakingThread;
    private Thread networkThread;

    private String currentMatchId;
    private volatile String player1_uid;
    private volatile String player2_uid;

    // 키 상수 (하드코딩 방지)
    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";

    public NetworkManager(Game game) {
        this.game = game;
        this.clientService = new FirebaseClientService();
    }

    public void stopAllThreads() {
        if (matchmakingThread != null && matchmakingThread.isAlive()) {
            matchmakingThread.interrupt();
        }
        if (networkThread != null && networkThread.isAlive()) {
            networkThread.interrupt();
        }
    }

    // --- 매치메이킹 (PVP) ---
    public void startMatchmakingLoop() {
        matchmakingThread = new Thread(() -> {
            String myUid = CurrentUserManager.getInstance().getUid();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("상대방 찾는 중...");
                    String opponentUid = clientService.findOpponent(myUid);
                    boolean matched;

                    if (opponentUid != null) {
                        matched = tryProcessHostMatchmaking(myUid, opponentUid);
                    } else {
                        matched = tryProcessGuestMatchmaking(myUid);
                    }

                    if (matched) break;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        matchmakingThread.start();
    }

    private boolean tryProcessHostMatchmaking(String myUid, String opUid) {
        if (myUid.compareTo(opUid) < 0) { // 내가 방장
            String matchId = clientService.createMatch(myUid, opUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                this.player1_uid = myUid;
                this.player2_uid = opUid;
                clientService.deleteFromQueue(myUid);
                clientService.deleteFromQueue(opUid);
                SwingUtilities.invokeLater(() -> game.changeState(Gamestate.PLAYING_PVP));
                return true;
            }
        }
        return false;
    }

    private boolean tryProcessGuestMatchmaking(String myUid) {
        if (!clientService.isUserInQueue(myUid)) { // 큐에서 사라짐 (매칭됨)
            String matchId = clientService.findMyMatch(myUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                Map<String, Object> matchData = clientService.getMatchData(matchId);
                if (matchData != null) {
                    this.player1_uid = (String) matchData.get("player1");
                    this.player2_uid = (String) matchData.get("player2");
                    SwingUtilities.invokeLater(() -> game.changeState(Gamestate.PLAYING_PVP));
                    return true;
                }
            }
        }
        return false;
    }

    // --- 매치메이킹 (협동) ---
    public void startCoopMatchmakingLoop() {
        matchmakingThread = new Thread(() -> {
            String myUid = CurrentUserManager.getInstance().getUid();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("협동 상대 찾는 중...");
                    String opponentUid = clientService.findCoopOpponent(myUid);
                    boolean matched;

                    if (opponentUid != null) {
                        matched = tryProcessCoopHost(myUid, opponentUid);
                    } else {
                        matched = tryProcessCoopGuest(myUid);
                    }

                    if (matched) break;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        matchmakingThread.start();
    }

    private boolean tryProcessCoopHost(String myUid, String opUid) {
        if (myUid.compareTo(opUid) < 0) {
            String matchId = clientService.createMatch(myUid, opUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                this.player1_uid = myUid;
                this.player2_uid = opUid;
                clientService.deleteFromCoopQueue(myUid);
                clientService.deleteFromCoopQueue(opUid);
                SwingUtilities.invokeLater(() -> game.changeState(Gamestate.PLAYING_COOP));
                return true;
            }
        }
        return false;
    }

    private boolean tryProcessCoopGuest(String myUid) {
        if (!clientService.isUserInCoopQueue(myUid)) {
            String matchId = clientService.findMyMatch(myUid);
            if (matchId != null) {
                this.currentMatchId = matchId;
                Map<String, Object> matchData = clientService.getMatchData(matchId);
                if (matchData != null) {
                    this.player1_uid = (String) matchData.get("player1");
                    this.player2_uid = (String) matchData.get("player2");
                    SwingUtilities.invokeLater(() -> game.changeState(Gamestate.PLAYING_COOP));
                    return true;
                }
            }
        }
        return false;
    }

    // --- 인게임 네트워크 루프 ---
    public void startNetworkLoop() {
        networkThread = new Thread(() -> {
            while (isPlayingState() && !Thread.currentThread().isInterrupted()) {
                try {
                    sendMyStatus();
                    updateOpponentStatus();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        networkThread.start();
    }

    private boolean isPlayingState() {
        return game.getCurrentState() == Gamestate.PLAYING_PVP ||
                game.getCurrentState() == Gamestate.PLAYING_COOP;
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
        for (Entity entity : game.getEntities()) {
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
            // 위치 동기화
            if (opponentState.get("x") instanceof Number && opponentState.get("y") instanceof Number) {
                double opX = ((Number) opponentState.get("x")).doubleValue();
                double opY = ((Number) opponentState.get("y")).doubleValue();
                opponentShip.setLocation((int) opX, (int) opY);
            }

            // 체력 동기화
            if (opponentState.get(KEY_HEALTH) instanceof Number) {
                int opHealth = ((Number) opponentState.get(KEY_HEALTH)).intValue();
                ((ShipEntity) opponentShip).setCurrentHealth(opHealth);

                // PVP 승리 조건 체크 (상대 체력 0)
                if (opHealth <= 0 && game.getCurrentState() == Gamestate.PLAYING_PVP) {
                    SwingUtilities.invokeLater(game::notifyWinPVP);
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // 총알 동기화
            updateOpponentShots(opponentState);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateOpponentShots(Map<String, Object> opponentState) {
        String opponentUid = amIPlayer1() ? player2_uid : player1_uid;

        // 기존 상대방 총알 제거
        List<Entity> toRemove = new ArrayList<>();
        for (Entity entity : game.getEntities()) {
            if (entity instanceof ShotEntity && opponentUid.equals(((ShotEntity) entity).getOwnerUid())) {
                toRemove.add(entity);
            }
        }
        toRemove.forEach(game::removeEntity);

        // 새 총알 생성
        if (opponentState.get(KEY_SHOTS) instanceof List) {
            List<Map<String, Double>> shotList = (List<Map<String, Double>>) opponentState.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", sData.get("x").intValue(), sData.get("y").intValue(), 0, -300);
                shot.setOwnerUid(opponentUid);
                game.addEntity(shot);
            }
        }
    }

    public boolean amIPlayer1() {
        String myUid = CurrentUserManager.getInstance().getUid();
        return myUid != null && player1_uid != null && myUid.equals(player1_uid);
    }

    public String getPlayer1Uid() { return player1_uid; }
    public String getPlayer2Uid() { return player2_uid; }
}
