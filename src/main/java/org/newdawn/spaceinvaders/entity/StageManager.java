package org.newdawn.spaceinvaders.entity;

import java.util.HashMap;

public class StageManager {

    private int currentStage = 1; // 1 = 화성
    private final HashMap<Integer, StageData> stages = new HashMap<>();

    public StageManager() {
        // Stage 1 - 화성
        stages.put(1, new StageData(
                "Mars",
                150,                            // 적 이동속도
                700,                            // 발사 텀
                "sprites/bg_mars.png",
                1,                              // 화성형 패턴
                false                           // 보스 없음
        ));

        // Stage 2 - 목성
        stages.put(2, new StageData(
                "Jupiter",
                190,
                600,
                "sprites/bg_jupiter.png",
                2,
                false
        ));

        // Stage 3 - 토성
        stages.put(3, new StageData(
                "Saturn",
                210,
                550,
                "sprites/bg_saturn.png",
                3,
                false
        ));

        // Stage 4 - 천왕성
        stages.put(4, new StageData(
                "Uranus",
                240,
                500,
                "sprites/bg_uranus.png",
                4,
                false
        ));

        // Stage 5 - 해왕성
        stages.put(5, new StageData(
                "Neptune",
                260,
                450,
                "sprites/bg_neptune.png",
                5,
                false
        ));

        // Stage 6 - 블랙홀 보스
        stages.put(6, new StageData(
                "Black Hole",
                300,
                400,
                "sprites/bg_blackhole.png",
                99,                         // 특수 패턴
                true                        // 블랙홀은 보스전
        ));
    }

    public StageData getCurrentStage() {
        return stages.get(currentStage);
    }

    public void nextStage() {
        if (currentStage < stages.size()) {
            currentStage++;
        }
    }

    public boolean isLastStage() {
        return currentStage >= stages.size();
    }

    public int getStageNumber() {
        return currentStage;
    }
}
