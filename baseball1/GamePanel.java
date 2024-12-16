package baseball1;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private JTextArea gameResultArea;
    private JTextArea chatArea;
    private JTextField gameInputField;
    private JTextField chatInputField;
    private JLabel currentTurnLabel;
    private JLabel roundLabel;
    private JLabel scoreLabelPlayer1;
    private JLabel scoreLabelPlayer2;
    private JButton submitButton;
    private JButton chatButton;
    private BaseballClient client;
    private String myPlayerName;

    public GamePanel(BaseballClient client, String roomName, String playerName) {
        this.client = client;
        this.myPlayerName = playerName;
        setLayout(new BorderLayout());
        
        // 상단 정보 패널
        JPanel infoPanel = new JPanel(new GridLayout(2, 2));
        
        // 라운드와 현재 턴 표시
        roundLabel = new JLabel("Round: 1");
        currentTurnLabel = new JLabel("현재 턴: ");
        
        // 점수 표시
        scoreLabelPlayer1 = new JLabel("Player 1 점수: 0");
        scoreLabelPlayer2 = new JLabel("Player 2 점수: 0");
        
        infoPanel.add(roundLabel);
        infoPanel.add(currentTurnLabel);
        infoPanel.add(scoreLabelPlayer1);
        infoPanel.add(scoreLabelPlayer2);
        
        add(infoPanel, BorderLayout.NORTH);

        // 중앙 패널: 게임 결과와 채팅
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        
        // 게임 결과 영역
        gameResultArea = new JTextArea();
        gameResultArea.setEditable(false);
        JScrollPane gameScrollPane = new JScrollPane(gameResultArea);
        gameScrollPane.setBorder(BorderFactory.createTitledBorder("게임 진행"));
        
        // 채팅 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("채팅"));
        
        centerPanel.add(gameScrollPane);
        centerPanel.add(chatScrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // 하단 입력 패널
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        
        // 게임 입력
        JPanel gameInputPanel = new JPanel(new BorderLayout());
        gameInputField = new JTextField();
        submitButton = new JButton("제출");
        submitButton.addActionListener(e -> submitGameInput());
        gameInputPanel.add(new JLabel("숫자 입력: "), BorderLayout.WEST);
        gameInputPanel.add(gameInputField, BorderLayout.CENTER);
        gameInputPanel.add(submitButton, BorderLayout.EAST);
        
        // 채팅 입력
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputField = new JTextField();
        chatButton = new JButton("전송");
        chatButton.addActionListener(e -> sendChatMessage());
        chatInputPanel.add(new JLabel("채팅: "), BorderLayout.WEST);
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(chatButton, BorderLayout.EAST);
        
        inputPanel.add(gameInputPanel);
        inputPanel.add(chatInputPanel);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void appendResult(String message) {
        SwingUtilities.invokeLater(() -> {
            gameResultArea.append(message + "\n");
            gameResultArea.setCaretPosition(gameResultArea.getDocument().getLength());
        });
    }



    public void appendChatMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void updateTurn(String playerName) {
        SwingUtilities.invokeLater(() -> {
            currentTurnLabel.setText("현재 턴: " + playerName);
            boolean isMyTurn = playerName.equals(myPlayerName);
            gameInputField.setEnabled(isMyTurn);
            submitButton.setEnabled(isMyTurn);
            if (isMyTurn) {
                gameInputField.requestFocus();
            }
        });
    }

    public void updateScore(String player1Name, int score1, String player2Name, int score2) {
        SwingUtilities.invokeLater(() -> {
            scoreLabelPlayer1.setText(player1Name + " 점수: " + score1);
            scoreLabelPlayer2.setText(player2Name + " 점수: " + score2);
        });
    }

    public void updateRound(int round) {
        SwingUtilities.invokeLater(() -> {
            roundLabel.setText("Round: " + round);
        });
    }

    private void submitGameInput() {
        String input = gameInputField.getText().trim();
        if (!input.isEmpty()) {
            client.sendGameInput(input);
            gameInputField.setText("");
        }
    }

    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInputField.setText("");
        }
    }

    // 게임 종료 시 호출
    public void gameOver(String winner) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                winner + "가 승리했습니다!", 
                "게임 종료", 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
}