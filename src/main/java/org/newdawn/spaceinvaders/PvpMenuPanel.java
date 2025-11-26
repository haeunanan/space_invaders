package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class PvpMenuPanel extends JPanel {
    private Game game;
    private Sprite titleLogoSprite;
    private Sprite backgroundSprite;
    private JButton myPageButton;
    private JButton soloPlayButton;
    private JButton pvpPlayButton;
    private JButton logoutButton;

    public PvpMenuPanel(Game game) {
        this.game = game;
        setLayout(null);
        // setBackground(Color.BLACK);
        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");

        backgroundSprite = SpriteStore.get().getSprite("sprites/main_background.jpg");


        myPageButton = createStyledButton("마이페이지");
        myPageButton.setBounds(300, 300, 200, 40);
        add(myPageButton);

        myPageButton.addActionListener(e -> {
            game.changeState(Game.GameState.MY_PAGE);
        });

        // --- 버튼 3개를 중앙에 일렬로 배치하기 위한 계산 ---
        int buttonWidth = 180;
        int buttonHeight = 50;
        int gap = 20; // 버튼 사이 간격 (3개라 간격을 조금 좁힘)

        // 전체 너비 = (버튼폭 * 3) + (간격 * 2)
        int totalWidth = (buttonWidth * 3) + (gap * 2);
        int startX = (Constants.WINDOW_WIDTH - totalWidth) / 2; // 전체 그룹의 시작 X 좌표

        // 1. 혼자하기 버튼 (맨 왼쪽)
        soloPlayButton = createStyledButton("혼자하기");
        logoutButton.setBounds((Constants.WINDOW_WIDTH - buttonWidth) / 2, 450, buttonWidth, buttonHeight);
        add(soloPlayButton);

        // 2. 대결하기 버튼 (가운데)
        pvpPlayButton = createStyledButton("대결하기");
        pvpPlayButton.setBounds(startX + buttonWidth + gap, 365, buttonWidth, buttonHeight);
        add(pvpPlayButton);

        // 3. 협동하기 버튼 (맨 오른쪽)
        JButton coopPlayButton = createStyledButton("협동하기");
        coopPlayButton.setBounds(startX + (buttonWidth + gap) * 2, 365, buttonWidth, buttonHeight);
        add(coopPlayButton);

        // 4. 로그아웃 버튼 (아래쪽 중앙)
        logoutButton = createStyledButton("로그아웃"); // [중요] 반드시 먼저 생성해야 함!
        logoutButton.setBounds((Constants.WINDOW_WIDTH - buttonWidth) / 2, 450, buttonWidth, buttonHeight);
        add(logoutButton);

        // 리스너 추가
        coopPlayButton.addActionListener(e -> {
            // 1. 로그인 정보 확인
            String uid = CurrentUserManager.getInstance().getUid();
            String nickname = CurrentUserManager.getInstance().getNickname();
            if (uid == null) { JOptionPane.showMessageDialog(this, "로그인 정보가 없습니다."); return; }

            // 2. 협동 모드 매치메이킹 시작
            FirebaseClientService clientService = new FirebaseClientService();
            boolean success = clientService.startCoopMatchmaking(uid, nickname); // coop 메소드 호출

            // 3. 로비로 이동
            if (success) {
                game.changeState(Game.GameState.COOP_LOBBY); // COOP_LOBBY로 이동
            } else {
                JOptionPane.showMessageDialog(this, "서버 접속 실패");
            }
        });

        // --- 버튼 클릭 이벤트 리스너 ---
        soloPlayButton.addActionListener(e -> {
            // PLAYING_SINGLE 대신 PLAYING_SINGLE로 상태 변경
            game.changeState(Game.GameState.PLAYING_SINGLE);
        });
        pvpPlayButton.addActionListener(e -> {
            // 1. 현재 로그인된 사용자 정보를 가져옵니다.
            String uid = CurrentUserManager.getInstance().getUid();
            String nickname = CurrentUserManager.getInstance().getNickname();

            if (uid == null || nickname == null) {
                JOptionPane.showMessageDialog(this, "로그인 정보가 없습니다.");
                return;
            }

            // 2. Firebase에 매치메이킹 시작을 요청합니다.
            FirebaseClientService clientService = new FirebaseClientService();
            boolean success = clientService.startMatchmaking(uid, nickname);

            // 3. 요청이 성공하면 로비 화면으로 이동합니다.
            if (success) {
                game.changeState(Game.GameState.PVP_LOBBY);
            } else {
                JOptionPane.showMessageDialog(this, "매칭 서버에 접속할 수 없습니다.");
            }
        });
        logoutButton.addActionListener(e -> {
            CurrentUserManager.getInstance().logout();
            // 로그아웃 로직 추가 후 SIGN_IN 상태로 변경 (필요시)
            game.changeState(Game.GameState.SIGN_IN);
        });
    }
    // 버튼 스타일을 통일하기 위한 헬퍼 메소드
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false); // 클릭 시 테두리 제거
        button.setBorderPainted(true); // 테두리 보이게 설정
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 1. 배경 이미지 그리기
        if (backgroundSprite != null) {
            backgroundSprite.draw(g, 0, 0, getWidth(), getHeight());
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // 2. 타이틀 로고 그리기 (상단 중앙 정렬)
        if (titleLogoSprite != null) {
            int x = (getWidth() - titleLogoSprite.getWidth()) / 2;
            int y = -115; // 상단 여백
            titleLogoSprite.draw(g, x, y);
        }
    }
}