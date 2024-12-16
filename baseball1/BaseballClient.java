package baseball1;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class BaseballClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private LobbyPanel lobbyPanel;
    private GamePanel gamePanel;
    private JFrame mainFrame;
    private String currentRoom;

    public BaseballClient(String serverAddress, int port) {
        try {
            // 플레이어 이름 입력 받기
            playerName = JOptionPane.showInputDialog(null, "플레이어 이름을 입력하세요:", "로그인", JOptionPane.QUESTION_MESSAGE);
            if (playerName == null || playerName.trim().isEmpty()) {
                System.exit(0);
            }
            playerName = playerName.trim();

            // 서버 연결
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // UI 초기화
            initializeUI();
            
            // 서버에 로그인 메시지 전송
            out.println("LOGIN|" + playerName);
            
            // 서버로부터 메시지 수신을 위한 스레드 시작
            new Thread(this::receiveMessages).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "서버 연결 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        mainFrame = new JFrame("숫자 야구 게임 - " + playerName);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(800, 600));
        
        // 로비 패널 초기화
        lobbyPanel = new LobbyPanel(this);
        mainFrame.getContentPane().add(lobbyPanel);
        
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        // 창 닫힐 때 서버 연결 종료
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnect();
            }
        });
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|");
        String command = parts[0];

        SwingUtilities.invokeLater(() -> {
            switch (command) {
                case "LOGIN_SUCCESS":
                    // 로그인 성공 처리
                    break;
                    
                case "ROOMLIST":
                    if (parts.length > 1) {
                        lobbyPanel.updateRoomList(parts[1].split(","));
                    }
                    break;
                    
                case "ROOM_CREATED":
                    if (parts.length > 1) {
                        currentRoom = parts[1];
                        joinRoom(currentRoom);
                    }
                    break;
                    
                case "GAME_START":
                    switchToGamePanel();
                    break;
                    
                case "GUESS_RESULT":
                    if (gamePanel != null) {
                        // 메시지 전체를 결과로 전달
                        gamePanel.appendResult(message.substring(12));
                    }
                    break;

                    
                case "CHAT":
                    if (gamePanel != null && parts.length > 1) {
                        gamePanel.appendChatMessage(parts[1]);
                    }
                    break;
                    
                case "GAME_END":
                    if (parts.length > 1) {
                        handleGameEnd(parts[1]);
                    }
                    break;
                    
                case "ERROR":
                    if (parts.length > 1) {
                        JOptionPane.showMessageDialog(mainFrame, parts[1], "에러", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                    
                case "UPDATE_SCORE":
                    if (gamePanel != null && parts.length > 4) {
                        gamePanel.updateScore(parts[1], Integer.parseInt(parts[2]), 
                                           parts[3], Integer.parseInt(parts[4]));
                    }
                    break;
                    
                case "UPDATE_ROUND":
                    if (gamePanel != null && parts.length > 1) {
                        gamePanel.updateRound(Integer.parseInt(parts[1]));
                    }
                    break;
                    
                case "JOIN_SUCCESS":
                    if (parts.length > 1) {
                        currentRoom = parts[1];
                        switchToGamePanel();  // 게임 패널로 전환
                    }
                    break;

                case "JOIN_FAILED":
                    if (parts.length > 2) {
                        JOptionPane.showMessageDialog(mainFrame, parts[2], "입장 실패", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                    
                case "TURN_UPDATE":
                    if (gamePanel != null && parts.length > 1) {
                        gamePanel.updateTurn(parts[1]);
                    }
                    break;
                    
                case "GUESS":
                    if (gamePanel != null) {
                        String fullMessage = message.substring(message.indexOf("|") + 1);
                        gamePanel.appendResult(fullMessage);
                    }
                    break;
            }
        });
    }

    public void createRoom(String roomName) {
        out.println("CREATE|" + roomName);
    }

    public void joinRoom(String roomName) {
        out.println("JOIN|" + roomName);
        currentRoom = roomName;
    }

    public void sendGameInput(String input) {
        out.println("GUESS|" + input);
    }

    public void sendChatMessage(String message) {
        out.println("CHAT|" + message);
    }

    private void switchToGamePanel() {
        mainFrame.getContentPane().removeAll();
        gamePanel = new GamePanel(this, currentRoom, playerName);
        mainFrame.getContentPane().add(gamePanel);
        mainFrame.pack();
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void handleGameEnd(String winner) {
        if (gamePanel != null) {
            gamePanel.gameOver(winner);
        }
        // 게임 종료 후 새 라운드 시작 또는 로비로 돌아가기 선택
        int choice = JOptionPane.showConfirmDialog(mainFrame, 
            "게임이 종료되었습니다. 새 라운드를 시작하시겠습니까?",
            "게임 종료",
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.NO_OPTION) {
            returnToLobby();
        }
    }

    private void returnToLobby() {
        currentRoom = null;
        out.println("LEAVE");
        mainFrame.getContentPane().removeAll();
        lobbyPanel = new LobbyPanel(this);
        mainFrame.getContentPane().add(lobbyPanel);
        mainFrame.pack();
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void handleConnectionError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame,
                "서버와의 연결이 끊어졌습니다: " + e.getMessage(),
                "연결 오류",
                JOptionPane.ERROR_MESSAGE);
            disconnect();
            System.exit(1);
        });
    }

    private void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
                out.close();
            }
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new BaseballClient("localhost", 5023);
        });
    }
}