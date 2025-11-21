package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.entity.Entity;

import java.awt.Image;
import java.util.List;

public abstract class Stage {

    protected final Game game;
    protected final int stageNumber;

    protected Image background;

    public Stage(Game game, int stageNumber) {
        this.game = game;
        this.stageNumber = stageNumber;
    }

    /** 배경 이미지 반환 (GamePlayPanel에서 호출) */
    public Image getBackground() {
        return background;
    }

    /** 스테이지 진입 시 한 번만 호출 */
    public abstract void init();

    /** 매 프레임 기믹 업데이트 */
    public void update(long delta) {}

    public String getItemSpriteRef() {
        return "sprites/item_stabilizer.png"; // 기본값 (화성 등)
    }

    /** 스테이지 클리어 조건 */
    public abstract boolean isCompleted();

    /** UI용 텍스트 */
    public abstract String getDisplayName();

    /** 배경 스프라이트 파일 경로 */
    public abstract String getBackgroundSpriteRef();

    /** 헬퍼 */
    protected List<Entity> getEntities() {
        return game.getEntities();
    }

    /** 헬퍼 */
    protected boolean noAliensLeft(Class<? extends Entity> alienType) {
        for (Entity e : game.getEntities()) {
            if (alienType.isInstance(e)) return false;
        }
        return true;
    }
    public boolean isItemAllowed() {
        return true;
    }
    public double getPlayerShotVelocity() {
        return -300;
    }
    public void activateItem() {
        // 기본적으로 아무 효과 없음 (오버라이드용)
    }

}
