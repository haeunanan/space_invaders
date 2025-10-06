package org.newdawn.spaceinvaders;

// 필요한 Swing 라이브러리들을 import 합니다.
import javax.swing.*;
import java.awt.*;

public class SignUpPanel extends JPanel {
    private Game game;

    // UI 요소들을 클래스 변수로 선언
    private JTextField emailField; // Figma의 'id'
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField; // Figma의 'checkout'
    private JTextField usernameField;
    private JButton signUpButton;
    private JButton goToSignInButton;
    private Sprite titleLogoSprite;

    public SignUpPanel(Game game) {
        this.game = game;
        setLayout(null);
        setBackground(Color.BLACK);

        // 로고 이미지 로드
        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");

        // --- UI 요소 생성 및 설정 ---
        emailField = new JTextField("email");
        emailField.setBounds(300, 220, 200, 40);
        emailField.setHorizontalAlignment(JTextField.CENTER);

        passwordField = new JPasswordField();
        passwordField.setBounds(300, 270, 200, 40);
        passwordField.setHorizontalAlignment(JTextField.CENTER);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(300, 320, 200, 40);
        confirmPasswordField.setHorizontalAlignment(JTextField.CENTER);

        usernameField = new JTextField("username");
        usernameField.setBounds(300, 370, 200, 40);
        usernameField.setHorizontalAlignment(JTextField.CENTER);

        signUpButton = new JButton("sign up"); // Figma의 'log in' 버튼을 'sign up'으로 수정
        signUpButton.setBounds(300, 420, 200, 40);
        signUpButton.setBackground(new Color(36, 41, 86));
        signUpButton.setForeground(Color.WHITE);

        goToSignInButton = new JButton("back to sign in");
        goToSignInButton.setBounds(300, 470, 200, 30);
        goToSignInButton.setForeground(Color.LIGHT_GRAY);
        goToSignInButton.setBorderPainted(false);
        goToSignInButton.setContentAreaFilled(false);
        goToSignInButton.setFocusPainted(false);

        // --- 패널에 UI 요소들 추가 ---
        add(emailField);
        add(passwordField);
        add(confirmPasswordField);
        add(usernameField);
        add(signUpButton);
        add(goToSignInButton);

        // --- 버튼 클릭 이벤트 리스너 추가 ---
        signUpButton.addActionListener(e -> {
            handleSignUp(); // 회원가입 처리 메소드 호출
        });

        goToSignInButton.addActionListener(e -> {
            game.changeState(Game.GameState.SIGN_IN); // 로그인 화면으로 전환
        });
    }

    private void handleSignUp() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String username = usernameField.getText(); // username은 지금 당장 쓰진 않지만 받아둡니다.

        // 1. 비밀번호와 비밀번호 확인이 일치하는지 검사
        if (!password.equals(confirmPassword)) {
            // JOptionPane을 사용하려면 javax.swing.JOptionPane을 import 해야 합니다.
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            return; // 일치하지 않으면 여기서 중단
        }

        FirebaseClientService clientService = new FirebaseClientService();
        boolean success = clientService.signUp(email, password);

        if (success) {
            JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 화면으로 돌아갑니다.");
            game.changeState(Game.GameState.SIGN_IN);
        } else {
            JOptionPane.showMessageDialog(this, "회원가입 실패. (이미 존재하는 이메일이거나 비밀번호가 너무 짧습니다.)");
        }

        System.out.println("회원가입 시도: " + email);

        // 2. TODO: Firebase REST API를 이용한 사용자 생성 요청 로직 구현
        // 성공 시: JOptionPane.showMessageDialog(this, "회원가입 성공!");
        //         game.changeState(Game.GameState.SIGN_IN);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (titleLogoSprite != null) {
            titleLogoSprite.draw(g, 130, 30);
        }
    }
}