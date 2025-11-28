package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import javax.swing.*;
import java.util.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NetworkManager {
    private final Game game;
    private final FirebaseClientService clientService;
    private Thread networkThread;

    private String currentMatchId;
    private String player1_uid;
    private String player2_uid;

    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
    private static final String KEY_ALIENS = "aliens";

    private final NetworkStateConverter stateConverter = new NetworkStateConverter();
    private final NetworkSyncHelper syncHelper;
    private final Set<String> pendingHitAlienIds = Collections.synchronizedSet(new HashSet<>());

    public NetworkManager(Game game) {
        this.game = game;
        this.clientService = new FirebaseClientService();
        this.syncHelper = new NetworkSyncHelper(game); // 초기화
    }

    // [추가] MatchmakingManager에서 호출하여 게임 정보를 세팅
    public void setMatchInfo(String matchId, String p1, String p2) {
        this.currentMatchId = matchId;
        this.player1_uid = p1;
        this.player2_uid = p2;
    }

    public Set<String> getAndClearPendingHitAlienIds() {
        synchronized (pendingHitAlienIds) {
            Set<String> hits = new HashSet<>(pendingHitAlienIds);
            pendingHitAlienIds.clear();
            return hits;
        }
    }

    public void notifyAlienHit(String alienId) {
        pendingHitAlienIds.add(alienId);
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
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        networkThread.start();
    }

    private void sendMyStatus() {
        if (game.getShip() == null) return;

        // 복잡한 Map 생성 로직 제거 -> Converter 호출로 대체
        boolean amIPlayer1 = amIPlayer1();
        Map<String, Object> myState = stateConverter.createMyState(game, amIPlayer1);

        String myPlayerNode = amIPlayer1 ? "player1_state" : "player2_state";
        clientService.updatePlayerState(currentMatchId, myPlayerNode, myState);
    }

    private void updateOpponentStatus() {
        String opponentNode = amIPlayer1() ? "player2_state" : "player1_state";
        Map<String, Object> opponentState = clientService.getOpponentState(currentMatchId, opponentNode);

        // 모든 동기화 로직 위임
        syncHelper.syncOpponent(currentMatchId, amIPlayer1(), opponentState);
    }

    // 클래스 하단에 추가
    public String getPlayer1Uid() { return player1_uid; }
    public String getPlayer2Uid() { return player2_uid; }

    public boolean amIPlayer1() {
        String myUid = CurrentUserManager.getInstance().getUid();
        return myUid != null && player1_uid != null && myUid.equals(player1_uid);
    }
}