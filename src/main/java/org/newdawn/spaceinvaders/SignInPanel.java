package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class SignInPanel extends JPanel {
    private Game game;

    private Sprite titleLogoSprite;
    private Sprite backgroundSprite;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton signInButton;
    private JButton goToSignUpButton; // "sign up" 글자를 버튼으로 구현

    public SignInPanel(Game game) {
        this.game = game;
        setLayout(null);

        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");
        backgroundSprite = SpriteStore.get().getSprite("sprites/main_background.jpg");

        // 1. 이메일 입력창
        emailField = new JTextField("email");
        emailField.setBounds(300, 250, 200, 40);
        emailField.setHorizontalAlignment(JTextField.CENTER);
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(Color.GRAY);

        // 2. 비밀번호 입력창 - 입력 시 ●으로 표시
        passwordField = new JPasswordField();
        passwordField.setBounds(300, 300, 200, 40);
        passwordField.setHorizontalAlignment(JTextField.CENTER);

        // 3. 로그인 버튼
        signInButton = new JButton("sign in");
        signInButton.setBounds(300, 350, 200, 40);
        signInButton.setBackground(new Color(36, 41, 86));
        signInButton.setForeground(Color.WHITE); // 글자색

        // 4. 회원가입 하러가기 버튼
        goToSignUpButton = new JButton("sign up");
        goToSignUpButton.setBounds(300, 400, 200, 30);
        goToSignUpButton.setForeground(Color.LIGHT_GRAY);
        goToSignUpButton.setBorderPainted(false);
        goToSignUpButton.setContentAreaFilled(false);
        goToSignUpButton.setFocusPainted(false);

        // --- 패널에 UI 요소들 추가 ---
        add(emailField);
        add(passwordField);
        add(signInButton);
        add(goToSignUpButton);

        // "회원가입 하러가기" 버튼 클릭 시
        goToSignUpButton.addActionListener(e -> {
            game.changeState(Game.GameState.SIGN_UP);
        });

        // "로그인" 버튼 클릭 시
        signInButton.addActionListener(e -> {
            handleSignIn();
        });
    }

    private void handleSignIn() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이메일과 비밀번호를 모두 입력해주세요.");
            return;
        }

        FirebaseClientService clientService = new FirebaseClientService();

        // 1. Firebase Authentication으로 로그인 시도 (이제 uid를 바로 받아옵니다)
        String uid = clientService.signIn(email, password);

        if (uid != null) {
            // 2. 로그인 성공! Firestore에서 닉네임 정보를 가져옵니다.
            String nickname = clientService.getUsername(uid);

            if (nickname == null) {
                // 3. 닉네임이 없는 경우 (기존 사용자), 새로 입력받습니다.
                nickname = JOptionPane.showInputDialog(this, "최초 접속입니다. 사용할 닉네임을 입력해주세요.");

                if (nickname != null && !nickname.trim().isEmpty()) {
                    // 입력받은 닉네임을 Firestore에 저장합니다.
                    clientService.saveUsername(uid, nickname);
                } else {
                    JOptionPane.showMessageDialog(this, "닉네임 설정이 취소되어 로그인을 중단합니다.");
                    return; // 닉네임 설정 안하면 로그인 중단
                }
            }

            // 4. 최종적으로 얻은 uid와 nickname을 CurrentUserManager에 저장합니다.
            CurrentUserManager.getInstance().login(uid, nickname);

            // 5. PVP 메뉴 화면으로 전환합니다.
            JOptionPane.showMessageDialog(this, nickname + "님, 환영합니다!");
            game.changeState(Game.GameState.PVP_MENU);

        } else {
            // 클라이언트 로그인 실패 (아이디/비밀번호 틀림)
            JOptionPane.showMessageDialog(this, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    // paintComponent를 오버라이드하여 로고 등을 그릴 수 있습니다.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. 배경 이미지 그리기 (가장 먼저)
        if (backgroundSprite != null) {
            backgroundSprite.draw(g, 0, 0, getWidth(), getHeight());
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. 로고 이미지 그리기 (상단 중앙 정렬)
        if (titleLogoSprite != null) {
            int x = (getWidth() - titleLogoSprite.getWidth()) / 2;
            int y = -115; // 상단 여백
            titleLogoSprite.draw(g, x, y);
        }
    }
}