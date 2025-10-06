package org.newdawn.spaceinvaders;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class StartMenuPanel extends JPanel {

    private Game game; // 상태 변경을 위해 Game 객체를 저장할 변수
    private Sprite titleLogoSprite;
    private Sprite shipImageSprite;

    public StartMenuPanel(Game game) {
        this.game = game; // 생성자에서 Game 객체를 받아 저장
        setLayout(null); // 직접 위치를 지정하기 위해 레이아웃 매니저를 null로 설정

        // 이미지 로드
        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");
        shipImageSprite = SpriteStore.get().getSprite("sprites/start-ship.png");

        // 'Start' 버튼 생성
        JButton startButton = new JButton("Start");
        startButton.setBounds(350, 450, 100, 40); // 버튼 위치와 크기 설정
        add(startButton); // 패널에 버튼 추가

        // 버튼 클릭 이벤트 처리
        startButton.addActionListener(e -> {
            // 버튼이 클릭되면 Game 클래스에 알려 상태를 변경하도록 요청
            game.changeState(Game.GameState.SIGN_IN);
        });
    }

    // 이 패널이 화면에 그려져야 할 때 자동으로 호출되는 메소드
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 그리기 기능을 먼저 호출

        // 배경을 검은색으로 칠하기
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 로드한 이미지 그리기
        if (titleLogoSprite != null) {
            titleLogoSprite.draw(g, 130, 100);
        }
        if (shipImageSprite != null) {
            shipImageSprite.draw(g, 30, 250);
        }
    }
}