package org.newdawn.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class MyPagePanel extends JPanel {
    // [수정 1] 직렬화 제외를 위해 transient 추가
    private transient Game game;

    private JTextField nicknameField;
    private JButton modifyButton;
    private JButton saveButton;
    private JButton backButton;

    public MyPagePanel(Game game) {
        this.game = game;
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 42, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel titleLabel = new JLabel("마이페이지");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 32));
        add(titleLabel, gbc);

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        editPanel.setOpaque(false);

        nicknameField = new JTextField(20);
        nicknameField.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));

        // [수정 2] JTextField.CENTER -> SwingConstants.CENTER 로 변경
        // (상수가 정의된 인터페이스를 직접 참조하는 것이 권장됨)
        nicknameField.setHorizontalAlignment(SwingConstants.CENTER);

        editPanel.add(nicknameField);

        modifyButton = new JButton("수정");
        editPanel.add(modifyButton);

        saveButton = new JButton("저장");
        saveButton.setVisible(false);
        editPanel.add(saveButton);

        add(editPanel, gbc);

        backButton = new JButton("뒤로가기");
        add(backButton, gbc);

        modifyButton.addActionListener(e -> {
            nicknameField.setEditable(true);
            nicknameField.setBackground(Color.WHITE);
            nicknameField.setForeground(Color.BLACK);
            modifyButton.setVisible(false);
            saveButton.setVisible(true);
            nicknameField.requestFocusInWindow();
        });

        saveButton.addActionListener(e -> handleSave());

        backButton.addActionListener(e -> {
            game.changeState(Gamestate.PVP_MENU);
        });

        updateUser();
    }

    public void updateUser() {
        String nickname = CurrentUserManager.getInstance().getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            nicknameField.setText(nickname);
        } else {
            nicknameField.setText("(닉네임 없음)");
        }

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
            game.changeState(Gamestate.PVP_MENU);
        } else {
            JOptionPane.showMessageDialog(this, "닉네임 변경에 실패했습니다.");
        }
    }
}