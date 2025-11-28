package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.*;
import javax.swing.SwingUtilities;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkSyncHelper {
    private final Game game;
    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";
    private static final String KEY_ALIENS = "aliens";
    private static final String KEY_HITS = "hits";

    public NetworkSyncHelper(Game game) {
        this.game = game;
    }

    public void syncOpponent(String matchId, boolean amIPlayer1, Map<String, Object> opponentState) {
        Entity opponentShip = game.getOpponentShip();

        // 상대방 데이터가 없거나 기체가 없으면 중단
        if (opponentState == null || opponentShip == null) return;

        // 1. 공통 동기화 (위치, 체력, 총알)
        syncPosition(opponentShip, opponentState);
        syncHealth(opponentShip, opponentState);
        updateOpponentShots(opponentState, amIPlayer1);

        if (amIPlayer1 && opponentState.containsKey(KEY_HITS)) {
            processOpponentHits((List<String>) opponentState.get(KEY_HITS));
        }

        // 2. 게스트(Player 2) 전용 동기화 (적, 스테이지)
        if (!amIPlayer1) {
            // 적(Alien) 동기화
            if (opponentState.containsKey(KEY_ALIENS)) {
                syncAliens((List<Map<String, Object>>) opponentState.get(KEY_ALIENS));
            }

            // 스테이지 및 대기 상태 동기화
            LevelManager lm = game.getLevelManager();

            // (A) 호스트가 클리어 대기 중인지 확인
            boolean hostWaiting = Boolean.TRUE.equals(opponentState.get("waiting"));
            // 호스트는 대기 중인데 나는 아직 대기 중이 아니라면 -> 나도 대기 상태로 전환
            if (hostWaiting && !lm.isWaitingForKeyPress()) {
                lm.setMessage("Stage Clear! Waiting for Host...");
                lm.setWaitingForKeyPress(true);
            }

            // (B) 호스트의 스테이지 번호 확인 및 강제 이동
            if (opponentState.containsKey("stage")) {
                int hostStage = ((Number) opponentState.get("stage")).intValue();
                // 호스트가 다음 스테이지로 넘어갔다면 나도 즉시 이동
                if (hostStage > lm.getStageIndex()) {
                    lm.setStageIndex(hostStage); // 스테이지 번호 맞춤
                    lm.nextStage();              // 다음 스테이지 시작 (LevelManager.nextStage 호출)
                }
            }
        }
    }

    private void processOpponentHits(List<String> hitIds) {
        EntityManager em = game.getEntityManager();
        for (Entity e : em.getEntities()) {
            if (e instanceof AlienEntity) {
                AlienEntity alien = (AlienEntity) e;
                // ID가 일치하면 데미지 처리 (호스트 권한으로 죽임)
                if (hitIds.contains(alien.getNetworkId())) {
                    if (alien.takeDamage(1)) {
                        // AlienEntity 내부 로직으로는 점수/사운드 처리가 부족할 수 있으니
                        // 명시적으로 사망 처리 메서드 호출 (ShotEntity에 있던 로직과 유사하게)
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
                game.getNetworkManager().getPlayer1Uid(); // Getter 필요 (NetworkManager에 추가)

        // UID를 가져오기 번거롭다면, NetworkManager에서 p1, p2 UID를 public으로 열거나
        // 단순히 "내가 아닌 모든 총알"을 관리하는 방식으로 처리할 수도 있습니다.
        // 여기서는 가장 확실한 방법인 NetworkManager에 Getter를 추가한다고 가정합니다.

        if (opponentUid == null) return;

        EntityManager em = game.getEntityManager();

        // 1. 기존 상대방 총알 제거
        List<Entity> toRemove = em.getEntities().stream()
                .filter(e -> e instanceof ShotEntity)
                .map(e -> (ShotEntity) e)
                .filter(s -> opponentUid.equals(s.getOwnerUid()))
                .collect(Collectors.toList());
        toRemove.forEach(em::removeEntity);

        // 2. 새 총알 생성
        if (opponentState.get(KEY_SHOTS) instanceof List) {
            List<Map<String, Double>> shotList = (List<Map<String, Double>>) opponentState.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                // 상대방 총알이므로 위치 보정 필요 없음 (화면 그릴 때 GamePlayPanel에서 반전 처리됨)
                int sx = sData.get("x").intValue();
                int sy = sData.get("y").intValue();

                // 총알 속도: 상대방 화면 기준 위(-300)로 쏘는 것 -> 내 화면에선 아래(+300)로 내려와야 함?
                // 아니오, 좌표계가 반전되므로 로직상으로는 똑같이 -300으로 두고, 그릴 때만 위치를 뒤집습니다.
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

        // 1. 서버 데이터를 기반으로 로컬 적 업데이트 또는 생성
        for (Map<String, Object> data : serverAliens) {
            String id = (String) data.get("id");
            double x = ((Number) data.get("x")).doubleValue();
            double y = ((Number) data.get("y")).doubleValue();
            // [추가] 서버에서 보낸 이미지 경로 받기 (기본값 설정)
            String ref = (String) data.getOrDefault("ref", "sprites/alien.gif");

            Optional<AlienEntity> localAlien = localEntities.stream()
                    .filter(e -> e instanceof AlienEntity)
                    .map(e -> (AlienEntity) e)
                    .filter(a -> a.getNetworkId().equals(id))
                    .findFirst();

            if (localAlien.isPresent()) {
                localAlien.get().setLocation((int) x, (int) y);
            } else {
                // 없으면 새로 생성 (받아온 ref 사용)
                AlienEntity newAlien = new AlienEntity(game, ref, (int)x, (int)y, 0, 0);
                newAlien.setNetworkId(id);
                em.addEntity(newAlien);
            }
        }

        // ... (삭제 로직은 기존 유지) ...
    }
}
