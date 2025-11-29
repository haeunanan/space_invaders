package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PvpLobbyPanel extends JPanel {
    // [수정 1] 직렬화 경고 해결: Game 객체를 직렬화에서 제외
    private transient Game game;

    private JLabel waitingLabel;
    private Timer animationTimer;
    private int dotCount = 0;

    public PvpLobbyPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // [수정 2] 정적 멤버 접근 수정: JLabel.CENTER -> SwingConstants.CENTER
        // (javax.swing.* 임포트로 인해 SwingConstants 사용 가능)
        waitingLabel = new JLabel("상대방을 기다리는 중.", SwingConstants.CENTER);

        waitingLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 30));
        waitingLabel.setForeground(Color.WHITE);
        add(waitingLabel, BorderLayout.CENTER);

        // --- '매칭 취소' 버튼 스타일 및 크기 수정 ---
        JButton cancelButton = new JButton("매칭 취소");
        cancelButton.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        cancelButton.setPreferredSize(new Dimension(200, 50));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);

        // 버튼을 남쪽에 배치하기 위해 새로운 패널 사용
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> {
            stopAnimation();
            game.changeState(GameState.PVP_MENU);
        });

        startAnimation();
    }

    /**
     * '...' 애니메이션을 시작하는 메소드
     */
    public void startAnimation() {
        animationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount = (dotCount + 1) % 4;
                String dots = "";
                for (int i = 0; i < dotCount; i++) {
                    dots += ".";
                }
                waitingLabel.setText("상대방을 기다리는 중" + dots);
            }
        });
        animationTimer.start();
    }

    /**
     * 애니메이션을 중지하는 메소드
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

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