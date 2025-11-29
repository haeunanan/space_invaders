package org.newdawn.spaceinvaders;

import javax.swing.SwingUtilities;
import java.util.Map;

public class MatchmakingManager {
    private final Game game;
    private final FirebaseClientService clientService;
    private Thread matchmakingThread;

    public MatchmakingManager(Game game) {
        this.game = game;
        this.clientService = new FirebaseClientService();
    }

    public void stopMatchmaking() {
        if (matchmakingThread != null && matchmakingThread.isAlive()) {
            matchmakingThread.interrupt();
        }
    }

    public void startMatchmakingLoop() {
        // [수정] boolean 플래그 전달
        startLoop("상대방 찾는 중...", false);
    }

    public void startCoopMatchmakingLoop() {
        // [수정] boolean 플래그 전달
        startLoop("협동 상대 찾는 중...", true);
    }

    // [리팩토링] CogC 20 -> 획기적으로 감소
    private void startLoop(String logMsg, boolean isCoop) {
        stopMatchmaking();
        matchmakingThread = new Thread(() -> runMatchmakingLoop(logMsg, isCoop));
        matchmakingThread.start();
    }

    // [분리된 메서드 1] 루프 전체 흐름 담당
    private void runMatchmakingLoop(String logMsg, boolean isCoop) {
        String myUid = CurrentUserManager.getInstance().getUid();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                System.out.println(logMsg);
                // 한 번의 매칭 시도 로직을 분리하여 호출
                if (attemptMatch(myUid, isCoop)) {
                    break;
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // [분리된 메서드 2] 단일 매칭 시도 로직 (복잡도 분산)
    private boolean attemptMatch(String myUid, boolean isCoop) {
        String opponentUid = isCoop
                ? clientService.findCoopOpponent(myUid)
                : clientService.findOpponent(myUid);

        if (opponentUid != null) {
            return tryProcessHostMatchmaking(myUid, opponentUid, isCoop);
        } else {
            return isCoop ? tryProcessCoopGuest() : tryProcessGuestMatchmaking();
        }
    }

    // [리팩토링] tryProcessHostMatchmaking (ev(G) 4 -> 1로 감소)
    private boolean tryProcessHostMatchmaking(String myUid, String opUid, boolean isCoop) {
        // Guard Clause: 내가 방장이 아니면 즉시 종료
        if (myUid.compareTo(opUid) >= 0) return false;

        String matchId = clientService.createMatch(myUid, opUid);
        if (matchId == null) return false;

        // 성공 시 처리 로직 분리
        finalizeHostMatch(matchId, myUid, opUid, isCoop);
        return true;
    }

    // [분리된 메서드 3] 호스트 매칭 확정 및 큐 삭제
    private void finalizeHostMatch(String matchId, String myUid, String opUid, boolean isCoop) {
        if (isCoop) {
            clientService.deleteFromCoopQueue(myUid);
            clientService.deleteFromCoopQueue(opUid);
            joinMatch(matchId, myUid, opUid, GameState.PLAYING_COOP);
        } else {
            clientService.deleteFromQueue(myUid);
            clientService.deleteFromQueue(opUid);
            joinMatch(matchId, myUid, opUid, GameState.PLAYING_PVP);
        }
    }

    private boolean tryProcessGuestMatchmaking() {
        String myUid = CurrentUserManager.getInstance().getUid();
        if (clientService.isUserInQueue(myUid)) return false;

        String matchId = clientService.findMyMatch(myUid);
        if (matchId == null) return false;

        return joinMatchForGuest(matchId, GameState.PLAYING_PVP);
    }

    private boolean tryProcessCoopGuest() {
        String myUid = CurrentUserManager.getInstance().getUid();
        if (clientService.isUserInCoopQueue(myUid)) return false;

        String matchId = clientService.findMyMatch(myUid);
        if (matchId == null) return false;

        return joinMatchForGuest(matchId, GameState.PLAYING_COOP);
    }

    private boolean joinMatchForGuest(String matchId, GameState targetState) {
        Map<String, Object> matchData = clientService.getMatchData(matchId);
        if (matchData != null) {
            String p1 = (String) matchData.get("player1");
            String p2 = (String) matchData.get("player2");
            joinMatch(matchId, p1, p2, targetState);
            return true;
        }
        return false;
    }

    private void joinMatch(String matchId, String p1, String p2, GameState targetState) {
        game.getNetworkManager().setMatchInfo(matchId, p1, p2);
        SwingUtilities.invokeLater(() -> game.getGameStateManager().changeState(targetState));
    }
}