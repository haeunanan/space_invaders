package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;

public class NetworkShipSynchronizer {
    private final Game game;
    private static final String KEY_HEALTH = "health";
    private static final String KEY_SHOTS = "shots";

    public NetworkShipSynchronizer(Game game) {
        this.game = game;
    }

    public void syncBasicShipState(Entity opponentShip, Map<String, Object> state, boolean amIPlayer1) {
        syncPosition(opponentShip, state);
        syncHealth(opponentShip, state);
        updateOpponentShots(state, amIPlayer1);
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
                SwingUtilities.invokeLater(() -> game.getResultHandler().notifyWinPVP());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateOpponentShots(Map<String, Object> state, boolean amIPlayer1) {
        String opponentUid = amIPlayer1 ?
                game.getNetworkManager().getPlayer2Uid() :
                game.getNetworkManager().getPlayer1Uid();

        if (opponentUid == null) return;

        EntityManager em = game.getEntityManager();
        em.getEntities().removeIf(e ->
                e instanceof ShotEntity && opponentUid.equals(((ShotEntity)e).getOwnerUid())
        );

        if (state.get(KEY_SHOTS) instanceof List) {
            List<Map<String, Double>> shotList = (List<Map<String, Double>>) state.get(KEY_SHOTS);
            for (Map<String, Double> sData : shotList) {
                createRemoteShot(em, sData, opponentUid);
            }
        }
    }

    private void createRemoteShot(EntityManager em, Map<String, Double> data, String ownerUid) {
        int sx = data.get("x").intValue();
        int sy = data.get("y").intValue();
        ShotEntity shot = new ShotEntity(game, "sprites/shot.gif", sx, sy, 0, -300);
        shot.setOwnerUid(ownerUid);
        em.addEntity(shot);
    }
}