package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.stage.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StageFactory {
    // 스테이지 생성 로직을 담을 저장소
    private static final Map<Integer, Function<Game, Stage>> stageMap = new HashMap<>();

    static {
        stageMap.put(1, MarsStage::new);
        stageMap.put(2, JupiterStage::new);
        stageMap.put(3, SaturnStage::new);
        stageMap.put(4, UranusStage::new);
        stageMap.put(5, NeptuneStage::new);
        stageMap.put(6, BlackHoleBossStage::new);
    }

    public static Stage createStage(Game game, int stageIndex) {
        Function<Game, Stage> creator = stageMap.get(stageIndex);
        if (creator != null) {
            return creator.apply(game);
        }
        return null; // 잘못된 인덱스
    }
}