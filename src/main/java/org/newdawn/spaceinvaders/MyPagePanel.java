package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class MyPagePanel extends JPanel {
    private Game game;
    private JTextField nicknameField;
    private JButton modifyButton;
    private JButton saveButton;
    private JButton backButton;

    public MyPagePanel(Game game) {
        this.game = game;
        // GridBagLayout으로 변경하여 중앙 정렬을 쉽게 합니다.
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 42, 45));

        // GridBagConstraints를 사용하여 컴포넌트 위치를 정밀하게 제어합니다.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 컴포넌트 간의 여백
        gbc.gridwidth = GridBagConstraints.REMAINDER; // 이 컴포넌트가 행의 마지막임을 의미

        // --- UI 요소 생성 ---
        JLabel titleLabel = new JLabel("마이페이지");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 32));
        add(titleLabel, gbc);

        // 닉네임과 수정/저장 버튼을 담을 패널 생성
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        editPanel.setOpaque(false); // 부모 패널의 배경색을 사용하도록 투명하게 설정

        nicknameField = new JTextField(20); // 크기를 20글자 기준으로 설정
        nicknameField.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        nicknameField.setHorizontalAlignment(JTextField.CENTER);
        editPanel.add(nicknameField);

        modifyButton = new JButton("수정");
        editPanel.add(modifyButton);

        saveButton = new JButton("저장");
        saveButton.setVisible(false);
        editPanel.add(saveButton);

        add(editPanel, gbc);

        backButton = new JButton("뒤로가기");
        add(backButton, gbc);

        // --- 버튼 리스너 ---
        modifyButton.addActionListener(e -> {
            nicknameField.setEditable(true);
            nicknameField.setBackground(Color.WHITE);
            nicknameField.setForeground(Color.BLACK);
            modifyButton.setVisible(false);
            saveButton.setVisible(true);
            // '수정' 버튼 클릭 시 텍스트 필드에 바로 포커스를 줍니다.
            nicknameField.requestFocusInWindow();
        });

        saveButton.addActionListener(e -> handleSave());

        backButton.addActionListener(e -> {
            game.changeState(Game.GameState.PVP_MENU); // 메뉴 화면으로 돌아가기
        });

        // 화면이 보일 때마다 최신 정보로 업데이트
        updateUser();
    }

    public void updateUser() {
        String nickname = CurrentUserManager.getInstance().getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            nicknameField.setText(nickname);
        } else {
            nicknameField.setText("(닉네임 없음)");
        }

        // UI 상태를 초기 '보기' 모드로 리셋
        nicknameField.setEditable(false);
        nicknameField.setBackground(Color.GRAY);
        nicknameField.setForeground(Color.WHITE);
        modifyButton.setVisible(true);
        saveButton.setVisible(false);
    }

    private void handleSave() {
        String newNickname = nicknameField.getText().trim();
        if (newNickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "닉네임은 비워둘 수 없습니다.");
            return;
        }

        String uid = CurrentUserManager.getInstance().getUid();
        FirebaseClientService clientService = new FirebaseClientService();
        boolean success = clientService.saveUsername(uid, newNickname);

        if (success) {
            CurrentUserManager.getInstance().setNickname(newNickname);
            JOptionPane.showMessageDialog(this, "닉네임이 성공적으로 변경되었습니다.");
            game.changeState(Game.GameState.PVP_MENU); // 저장 성공 후 메뉴로 돌아감
        } else {
            JOptionPane.showMessageDialog(this, "닉네임 변경에 실패했습니다.");
        }
    }
}