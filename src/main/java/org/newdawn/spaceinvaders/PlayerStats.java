package org.newdawn.spaceinvaders;

public class PlayerStats {
    // 점수 및 재화
    private int score = 0;
    private int coins = 0;

    // 플레이어 능력치 (업그레이드 레벨)
    private int attackLevel = 0;
    private int moveLevel = 0;
    private int missileLevel = 0;

    // 실제 게임에 적용되는 능력치
    private int missileCount = 1; // 기본 미사일 개수

    // --- 점수(Score) 관련 메서드 ---
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int value) {
        this.score += value;
    }

    public void resetScore() {
        this.score = 0;
    }

    // --- 코인(Coins) 관련 메서드 ---
    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int value) {
        this.coins += value;
    }

    /**
     * 코인을 사용합니다.
     * @param value 사용할 코인 양
     * @return 사용 성공 시 true, 코인이 부족하면 false
     */
    public boolean spendCoins(int value) {
        if (this.coins >= value) {
            this.coins -= value;
            return true;
        }
        return false;
    }

    public long getFiringInterval() {
        long interval = 500;
        for (int i = 0; i < attackLevel; i++) {
            interval *= 0.9;
        }
        return Math.max(100, interval);
    }

    // --- 미사일 개수(Missile Count) 관련 메서드 ---
    public int getMissileCount() {
        return missileCount;
    }

    public void setMissileCount(int missileCount) {
        this.missileCount = missileCount;
    }

    // --- 업그레이드 레벨(Levels) Getter/Setter ---
    public int getAttackLevel() {
        return attackLevel;
    }

    public void setAttackLevel(int attackLevel) {
        this.attackLevel = attackLevel;
    }

    public void increaseAttackLevel() {
        this.attackLevel++;
    }

    public int getMoveLevel() {
        return moveLevel;
    }

    public void setMoveLevel(int moveLevel) {
        this.moveLevel = moveLevel;
    }

    public void increaseMoveLevel() {
        this.moveLevel++;
    }

    public int getMissileLevel() {
        return missileLevel;
    }

    public void setMissileLevel(int missileLevel) {
        this.missileLevel = missileLevel;
    }

    public void increaseMissileLevel() {
        this.missileLevel++;
    }
}