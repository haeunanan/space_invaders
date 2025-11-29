package org.newdawn.spaceinvaders;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankingManager {
    private List<ScoreEntry> scores;
    private static final String RANKING_FILE = "ranking.dat"; // 저장될 파일 이름
    private static final int MAX_SCORES = 10; // 최대 10위까지만 저장

    public RankingManager() {
        scores = loadScores();
    }

    // 파일에서 랭킹을 불러오는 메서드
    @SuppressWarnings("unchecked")
    public List<ScoreEntry> loadScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RANKING_FILE))) {
            return (List<ScoreEntry>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("랭킹 파일이 없어 새로 생성합니다.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 파일에 랭킹을 저장하는 메서드
    public void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RANKING_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 새로운 점수를 랭킹에 추가하는 메서드
    public void addScore(int score, String playerName) {
        scores.add(new ScoreEntry(score, playerName));
        Collections.sort(scores); // ScoreEntry의 compareTo 메서드를 기준으로 정렬

        // 최대 순위를 넘는 점수는 제거
        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }
        saveScores(); // 변경사항을 파일에 저장
    }

    // 현재 점수가 랭킹에 들 수 있는지 확인하는 메서드
    public boolean isHighScore(int score) {
        if (score <= 0) return false;
        // 랭킹이 꽉 차지 않았거나, 현재 점수가 꼴찌 점수보다 높으면 true
        return scores.size() < MAX_SCORES || score > scores.get(scores.size() - 1).getScore();
    }

    public List<ScoreEntry> getScores() {
        return scores;
    }
}