package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.stage.*;

/**
 * 스테이지 생성을 전담하는 팩토리 클래스
 * (Refactoring: Game.java의 loadStage 메서드 분리)
 */
public class StageFactory {

    public static Stage createStage(Game game, int stageIndex) {
        switch (stageIndex) {
            case 1: return new MarsStage(game);
            case 2: return new JupiterStage(game);
            case 3: return new SaturnStage(game);
            case 4: return new UranusStage(game);
            case 5: return new NeptuneStage(game);
            case 6: return new BlackHoleBossStage(game);
            default: return null; // 스테이지가 끝났거나 잘못된 인덱스
        }
    }
}