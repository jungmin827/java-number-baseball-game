package baseball1;

import javax.swing.*;
import java.awt.*;

public class LobbyPanel extends JPanel {
    private DefaultListModel<String> roomListModel;
    private JList<String> roomList;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private BaseballClient client;

    public LobbyPanel(BaseballClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        
        // 방 목록
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        add(new JScrollPane(roomList), BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        createRoomButton = new JButton("방 만들기");
        joinRoomButton = new JButton("입장하기");
        
        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        joinRoomButton.addActionListener(e -> joinSelectedRoom());
        
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showCreateRoomDialog() {
        String roomName = JOptionPane.showInputDialog(this, "방 이름을 입력하세요:");
        if (roomName != null && !roomName.trim().isEmpty()) {
            client.createRoom(roomName.trim());
        }
    }

    private void joinSelectedRoom() {
        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null) {
            client.joinRoom(selectedRoom);
        }
    }

    public void updateRoomList(String[] rooms) {
        roomListModel.clear();
        for (String room : rooms) {
            if (!room.trim().isEmpty()) {
                roomListModel.addElement(room);
            }
        }
    }
}