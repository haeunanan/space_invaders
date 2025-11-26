package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowManager {
    private JFrame container;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // 패널들
    private StartMenuPanel startMenuPanel;
    private SignInPanel signInPanel;
    private SignUpPanel signUpPanel;
    private GamePlayPanel gamePlayPanel;
    private PvpMenuPanel pvpMenuPanel;
    private PvpLobbyPanel pvpLobbyPanel;
    private MyPagePanel myPagePanel;
    private long lastFpsTime;
    private int fps;
    private String windowTitle = "Space Invaders 102";

    // 카드 이름 상수
    public static final String CARD_START = "START";
    public static final String CARD_SIGN_IN = "SIGN_IN";
    public static final String CARD_SIGN_UP = "SIGN_UP";
    public static final String CARD_PLAYING_SINGLE = "PLAYING_SINGLE";
    public static final String CARD_PVP_MENU = "PVP_MENU";
    public static final String CARD_PVP_LOBBY = "PVP_LOBBY";
    public static final String CARD_MY_PAGE = "MY_PAGE";

    public WindowManager(Game game) {
        // 1. 프레임 생성
        container = new JFrame("Space Invaders 102");

        // 2. 메인 패널 및 레이아웃 설정
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));

        // 3. 각 화면 패널 생성
        startMenuPanel = new StartMenuPanel(game);
        signInPanel = new SignInPanel(game);
        signUpPanel = new SignUpPanel(game);
        gamePlayPanel = new GamePlayPanel(game);
        pvpMenuPanel = new PvpMenuPanel(game);
        pvpLobbyPanel = new PvpLobbyPanel(game);
        myPagePanel = new MyPagePanel(game);

        // 4. 패널 추가
        mainPanel.add(startMenuPanel, CARD_START);
        mainPanel.add(signInPanel, CARD_SIGN_IN);
        mainPanel.add(signUpPanel, CARD_SIGN_UP);
        mainPanel.add(gamePlayPanel, CARD_PLAYING_SINGLE);
        mainPanel.add(pvpMenuPanel, CARD_PVP_MENU);
        mainPanel.add(pvpLobbyPanel, CARD_PVP_LOBBY);
        mainPanel.add(myPagePanel, CARD_MY_PAGE);

        // 5. 프레임에 추가
        container.getContentPane().add(mainPanel);

        // 6. 윈도우 설정
        container.pack();
        container.setResizable(false);
        container.setLocationRelativeTo(null);

        // 종료 이벤트
        container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    public void updateFPS(long delta) {
        lastFpsTime += delta;
        fps++;
        if (lastFpsTime >= 1000) {
            setWindowTitle(windowTitle + " (FPS: " + fps + ")");
            lastFpsTime = 0;
            fps = 0;
        }
    }

    public void showWindow() {
        container.setVisible(true);
    }

    public void changeCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
    }

    public void repaint() {
        mainPanel.repaint();
    }

    public void gamePanelRepaint() {
        gamePlayPanel.repaint();
    }

    public void gamePanelRequestFocus() {
        gamePlayPanel.requestFocusInWindow();
    }

    public void addGameKeyListener(KeyListener listener) {
        gamePlayPanel.addKeyListener(listener);
    }

    public void addGameMouseListener(MouseListener listener) {
        gamePlayPanel.addMouseListener(listener);
    }

    public void updateMyPage() {
        myPagePanel.updateUser();
    }

    public void setWindowTitle(String title) {
        container.setTitle(title);
    }

    public JFrame getContainer() {
        return container;
    }
}