package org.newdawn.spaceinvaders;

import org.newdawn.spaceinvaders.entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePlayPanel extends JPanel {
    private Game game; // 게임 데이터에 접근하기 위한 Game 객체

    public GamePlayPanel(Game game) {
        this.game = game;
    }

    // 이 패널이 다시 그려져야 할 때마다 호출됩니다. (repaint()가 불릴 때)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 그리기 기능을 먼저 호출

        // 배경을 검게 칠합니다.
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Game 객체로부터 엔티티 리스트를 가져와 그립니다.
        ArrayList<Entity> entities = game.getEntities();
        for (Entity entity : entities) {
            entity.draw(g);
        }

        // "Press any key" 같은 메시지를 그립니다.
        if (game.isWaitingForKeyPress()) {
            g.setColor(Color.white);
            String message = game.getMessage();
            g.drawString(message, (800 - g.getFontMetrics().stringWidth(message)) / 2, 250);
            g.drawString("Press any key", (800 - g.getFontMetrics().stringWidth("Press any key")) / 2, 300);
        }
    }
}