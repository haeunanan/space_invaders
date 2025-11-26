package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class StartMenuPanel extends JPanel {

    private transient Game game;
    private transient Sprite titleLogoSprite;
    private transient Sprite shipImageSprite;
    private transient Sprite backgroundSprite;

    public StartMenuPanel(Game game) {
        this.game = game; // 생성자에서 Game 객체를 받아 저장
        setLayout(null); // 직접 위치를 지정하기 위해 레이아웃 매니저를 null로 설정

        // 이미지 로드
        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");
        shipImageSprite = SpriteStore.get().getSprite("sprites/start-ship.png");
        backgroundSprite = SpriteStore.get().getSprite("sprites/main_background.jpg");

        JButton startButton = new JButton("Start");
        startButton.setBounds(350, 450, 100, 40);
        startButton.setBackground(Color.WHITE); // 버튼 잘 보이게 배경 흰색 설정
        startButton.setFocusPainted(false);
        add(startButton);

        // 버튼 클릭 이벤트 처리
        startButton.addActionListener(e -> {
            // 버튼이 클릭되면 Game 클래스에 알려 상태를 변경하도록 요청
            game.changeState(Gamestate.SIGN_IN);
        });
    }

    // 이 패널이 화면에 그려져야 할 때 자동으로 호출되는 메소드
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 그리기 기능을 먼저 호출

        // 1. 배경 이미지 그리기 (화면 크기에 맞춰 꽉 차게)
        if (backgroundSprite != null) {
            backgroundSprite.draw(g, 0, 0, getWidth(), getHeight());
        } else {
            // 배경 이미지가 없을 경우 기존처럼 검은색 배경
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. 로고 이미지 그리기 (상단 중앙 정렬)
        if (titleLogoSprite != null) {
            // 화면 너비의 절반에서 이미지 너비의 절반을 뺍니다.
            int x = (getWidth() - titleLogoSprite.getWidth()) / 2;
            int y = -115; // 상단 여백 (필요에 따라 조절하세요)
            titleLogoSprite.draw(g, x, y);
        }
        if (shipImageSprite != null) {
            shipImageSprite.draw(g, 30, 250);
        }
    }
}