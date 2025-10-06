package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class SignInPanel extends JPanel {
    private Game game;

    private Sprite titleLogoSprite;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton signInButton;
    private JButton goToSignUpButton; // "sign up" 글자를 버튼으로 구현

    public SignInPanel(Game game) {
        this.game = game;
        setLayout(null);
        setBackground(Color.BLACK);

        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");

        // 1. 이메일 입력창 (JTextField)
        emailField = new JTextField("email");
        emailField.setBounds(300, 250, 200, 40); // (x, y, 너비, 높이)
        emailField.setHorizontalAlignment(JTextField.CENTER); // 텍스트 가운데 정렬
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(Color.GRAY);

        // 2. 비밀번호 입력창 (JPasswordField) - 입력 시 ●으로 표시됩니다.
        passwordField = new JPasswordField();
        passwordField.setBounds(300, 300, 200, 40);
        passwordField.setHorizontalAlignment(JTextField.CENTER);

        // 3. 로그인 버튼 (JButton)
        signInButton = new JButton("sign in");
        signInButton.setBounds(300, 350, 200, 40);
        signInButton.setBackground(new Color(36, 41, 86)); // Figma 버튼색과 비슷하게
        signInButton.setForeground(Color.WHITE); // 글자색

        // 4. 회원가입 하러가기 버튼 (JButton) - 버튼처럼 보이지 않게 스타일링
        goToSignUpButton = new JButton("sign up");
        goToSignUpButton.setBounds(300, 400, 200, 30);
        goToSignUpButton.setForeground(Color.LIGHT_GRAY);
        // 버튼의 배경과 테두리를 없애서 글자처럼 보이게 만듭니다.
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
            game.changeState(Game.GameState.SIGN_UP); // 회원가IP 상태로 전환
        });

        // "로그인" 버튼 클릭 시
        signInButton.addActionListener(e -> {
            // 3단계에서 구현할 Firebase 로그인 로직 호출
            handleSignIn();
        });
    }

    private void handleSignIn() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        FirebaseClientService clientService = new FirebaseClientService();
        String idToken = clientService.signIn(email, password);

        if (idToken != null) {
            AuthService authService = new AuthService();
            String uid = authService.verifyIdToken(idToken);

            if (uid != null) {
                // 최종 검증 성공!
                JOptionPane.showMessageDialog(this, "로그인 성공! 게임을 시작합니다.");
                game.changeState(Game.GameState.PLAYING);
            } else {
                // 서버 검증 실패 (보안 문제 등)
                JOptionPane.showMessageDialog(this, "서버 인증에 실패했습니다.");
            }
        } else {
            // 클라이언트 로그인 실패 (아이디/비밀번호 틀림)
            JOptionPane.showMessageDialog(this, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // paintComponent를 오버라이드하여 로고 등을 그릴 수 있습니다.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (titleLogoSprite != null) {
            titleLogoSprite.draw(g, 130, 30);
        }    }
}