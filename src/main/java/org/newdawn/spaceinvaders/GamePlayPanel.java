package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePlayPanel extends JPanel {
    // [수정] 직렬화 제외를 위해 transient 키워드 추가
    private transient Game game;
    private transient UIRenderer uiRenderer;

    // [추가] 스테이지 이펙트(번개, 운석, 바람 등)를 담당할 렌더러
    private transient StageEffectRenderer effectRenderer;

    public GamePlayPanel(Game game) {
        this.game = game;
        this.uiRenderer = new UIRenderer(game);
        this.effectRenderer = new StageEffectRenderer(); // [초기화]

        setFocusable(true);
        // 패널이 화면에 보여질 때 포커스를 요청하여 키 입력을 받을 수 있게 함
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(this::requestFocusInWindow);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. 배경 그리기
        drawBackground(g2d);

        // 2. 엔티티(우주선, 적, 총알 등) 그리기
        drawEntities(g2d);

        // 3. [수정] 스테이지 특수 효과 그리기 (StageEffectRenderer 위임)
        if (game.getCurrentStage() != null) {
            effectRenderer.drawStageEffects(g2d, game.getCurrentStage(), getWidth(), getHeight());
        }

        // 4. UI(HUD, 상점, 메시지 등) 그리기
        uiRenderer.drawUI(g2d, getWidth(), getHeight());
    }

    private void drawBackground(Graphics2D g2d) {
        // [수정] 싱글 모드뿐만 아니라 협동 모드(PLAYING_COOP)일 때도 배경을 그리도록 변경
        if ((game.getGameStateManager().getCurrentState() == GameState.PLAYING_SINGLE ||
                game.getGameStateManager().getCurrentState() == GameState.PLAYING_COOP) &&
                game.getCurrentStage() != null &&
                game.getCurrentStage().getBackground() != null) {

            g2d.drawImage(
                    game.getCurrentStage().getBackground(),
                    0, 0, getWidth(), getHeight(),
                    null
            );
        } else {
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    private void drawEntities(Graphics2D g2d) {
        // 게임이 대기 상태(메시지 표시 중 등)가 아닐 때만 엔티티를 그립니다.
        // (필요에 따라 이 조건을 완화할 수도 있습니다)
        if (!game.getLevelManager().isWaitingForKeyPress()) {
            List<Entity> entities = game.getEntityManager().getEntities();

            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                int drawX = entity.getX();
                int drawY = entity.getY();

                // PVP 모드일 때 상대방 진영의 엔티티 위치를 반전시켜 그리기
                boolean isOpponentEntity = false;
                if (game.getGameStateManager().getCurrentState() == GameState.PLAYING_PVP) {
                    String myUid = CurrentUserManager.getInstance().getUid();
                    // 상대방 기체이거나, 내가 쏘지 않은 총알인 경우 반전 대상
                    if (entity == game.getOpponentShip() ||
                            (entity instanceof ShotEntity && myUid != null && !((ShotEntity)entity).isOwnedBy(myUid))) {
                        isOpponentEntity = true;
                    }
                }

                if (isOpponentEntity) {
                    // 화면 높이 기준으로 Y축 반전
                    drawY = getHeight() - entity.getY() - entity.getSpriteHeight();
                }

                entity.draw(g2d, drawX, drawY);
            }
        }
    }

    // [삭제됨] drawStageEffects, drawJupiterEffects, drawSaturnEffects, drawNeptuneEffects
    // 메서드들은 모두 StageEffectRenderer 클래스로 이동했으므로 여기서 삭제되었습니다.
}