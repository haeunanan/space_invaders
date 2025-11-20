package org.newdawn.spaceinvaders.entity;

public class StageData {
    private final String name;                 // 행성 이름
    private final int alienSpeed;              // 기본 적 이동 속도
    private final int alienFireRate;           // 적 발사 딜레이(ms)
    private final String backgroundPath;       // 배경 이미지 경로
    private final int alienType;               // 행성별 적 종류 (패턴 전환용)
    private final boolean hasBoss;             // 스테이지에 보스 존재 여부

    public StageData(
            String name,
            int alienSpeed,
            int alienFireRate,
            String backgroundPath,
            int alienType,
            boolean hasBoss
    ) {
        this.name = name;
        this.alienSpeed = alienSpeed;
        this.alienFireRate = alienFireRate;
        this.backgroundPath = backgroundPath;
        this.alienType = alienType;
        this.hasBoss = hasBoss;
    }

    public String getName() {
        return name;
    }

    public int getAlienSpeed() {
        return alienSpeed;
    }

    public int getAlienFireRate() {
        return alienFireRate;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public int getAlienType() {
        return alienType;
    }

    public boolean hasBoss() {
        return hasBoss;
    }
}
