package org.newdawn.spaceinvaders;

// 필요한 Swing 라이브러리들을 import 합니다.
import javax.swing.*;
import java.awt.*;

public class SignUpPanel extends JPanel {
    // [수정 1] 직렬화 제외를 위해 transient 키워드 추가
    private transient Game game;
    private transient Sprite titleLogoSprite;
    private transient Sprite backgroundSprite;

    // UI 요소들을 클래스 변수로 선언
    private JTextField emailField; // Figma의 'id'
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField; // Figma의 'checkout'
    private JTextField usernameField;
    private JButton signUpButton;
    private JButton goToSignInButton;

    public SignUpPanel(Game game) {
        this.game = game;
        setLayout(null);

        // 로고 이미지 로드
        titleLogoSprite = SpriteStore.get().getSprite("sprites/title-logo.png");
        backgroundSprite = SpriteStore.get().getSprite("sprites/main_background.jpg");

        // --- UI 요소 생성 및 설정 ---
        emailField = new JTextField("email");
        emailField.setBounds(300, 220, 200, 40);
        // [수정 2] JTextField.CENTER -> SwingConstants.CENTER 로 변경
        emailField.setHorizontalAlignment(SwingConstants.CENTER);

        passwordField = new JPasswordField();
        passwordField.setBounds(300, 270, 200, 40);
        // [수정 3] JTextField.CENTER -> SwingConstants.CENTER 로 변경
        passwordField.setHorizontalAlignment(SwingConstants.CENTER);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(300, 320, 200, 40);
        // [수정 4] JTextField.CENTER -> SwingConstants.CENTER 로 변경
        confirmPasswordField.setHorizontalAlignment(SwingConstants.CENTER);

        usernameField = new JTextField("username");
        usernameField.setBounds(300, 370, 200, 40);
        // [수정 5] JTextField.CENTER -> SwingConstants.CENTER 로 변경
        usernameField.setHorizontalAlignment(SwingConstants.CENTER);

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
        // String username = usernameField.getText(); // (현재 사용 안 함)

        // 1. 비밀번호와 비밀번호 확인이 일치하는지 검사
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            return; // 일치하지 않으면 여기서 중단
        }

        FirebaseClientService clientService = new FirebaseClientService();
        // 변수 이름을 uid로 변경하여 무엇을 받아오는지 명확하게 합니다.
        String uid = clientService.signUp(email, password);

        // 조건문을 "uid가 null이 아닌 경우" (즉, 성공한 경우)로 변경합니다.
        if (uid != null) {
            // 회원가입 성공 로직 (이제 여기서 닉네임 저장 로직을 추가하면 됩니다)
            JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 화면으로 돌아갑니다.");
            game.changeState(Game.GameState.SIGN_IN);
        } else {
            // 회원가입 실패 로직
            JOptionPane.showMessageDialog(this, "회원가입 실패. (이미 존재하는 이메일이거나 비밀번호가 너무 짧습니다.)");
        }

        System.out.println("회원가입 시도: " + email);
    }

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