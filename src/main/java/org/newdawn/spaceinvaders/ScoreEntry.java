package org.newdawn.spaceinvaders;

import java.io.Serializable;

// Serializable 인터페이스는 객체를 파일에 저장하기 위해 필요합니다.
public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    private static final long serialVersionUID = 1L; // 객체 직렬화를 위한 버전 ID
    private int score;
    private String playerName;

    public ScoreEntry(int score, String playerName) {
        this.score = score;
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    // 점수를 기준으로 내림차순 정렬하기 위한 비교 메서드
    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.getScore(), this.score);
    }
}