package org.newdawn.spaceinvaders;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] argv) {
        // Game 클래스를 생성하여 실행
        SwingUtilities.invokeLater(Game::new);
    }
}
