package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PvpLobbyPanel extends JPanel {
    private Game game;
    private JLabel waitingLabel;
    private Timer animationTimer; // 애니메이션을 위한 타이머
    private int dotCount = 0; // 점의 개수를 추적하는 변수

    public PvpLobbyPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout()); // 레이아웃을 BorderLayout으로 변경
        setBackground(Color.BLACK);

        // --- '기다리는 중' 라벨 설정 ---
        waitingLabel = new JLabel("상대방을 기다리는 중.", JLabel.CENTER);
        waitingLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 30)); // 폰트 크기 키움
        waitingLabel.setForeground(Color.WHITE);
        add(waitingLabel, BorderLayout.CENTER);

        // --- '매칭 취소' 버튼 스타일 및 크기 수정 ---
        JButton cancelButton = new JButton("매칭 취소");
        cancelButton.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        cancelButton.setPreferredSize(new Dimension(200, 50)); // 버튼 크기 설정
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);

        // 버튼을 남쪽에 배치하기 위해 새로운 패널 사용
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0)); // 하단 여백 추가
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> {
            // TODO: Firebase에서 내 UID를 삭제하는 로직 추가
            stopAnimation(); // 화면 전환 전에 애니메이션 중지
            game.changeState(Game.GameState.PVP_MENU);
        });

        startAnimation(); // 패널이 생성될 때 애니메이션 시작
    }

    /**
     * '...' 애니메이션을 시작하는 메소드
     */
    public void startAnimation() {
        // 0.5초(500ms)마다 ActionListener를 실행하는 타이머 생성
        animationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount = (dotCount + 1) % 4; // 점 개수를 0, 1, 2, 3으로 순환
                String dots = "";
                for (int i = 0; i < dotCount; i++) {
                    dots += ".";
                }
                waitingLabel.setText("상대방을 기다리는 중" + dots);
            }
        });
        animationTimer.start(); // 타이머 시작
    }

    /**
     * 애니메이션을 중지하는 메소드
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    // 패널이 화면에서 보이지 않게 될 때 호출 (Game 클래스에서 상태 변경 시 호출 필요)
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }
}