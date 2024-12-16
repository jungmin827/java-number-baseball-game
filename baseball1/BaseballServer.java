package baseball1;

import java.io.*;
import java.net.*;
import java.util.*;

public class BaseballServer {
    private ServerSocket serverSocket;
    private Map<String, GameRoom> gameRooms = new HashMap<>();
    private List<ClientHandler> clients = new ArrayList<>();
    private final int PORT = 5023;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("서버가 시작되었습니다.");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameRoom createGameRoom(String roomName) {
        GameRoom room = new GameRoom(roomName);
        gameRooms.put(roomName, room);
        broadcastRoomList();
        return room;
    }

    public void joinGameRoom(String roomName, ClientHandler client) {
        GameRoom room = gameRooms.get(roomName);
        if (room != null && room.getPlayerCount() < 2) {
            room.addPlayer(client);
            client.setCurrentRoom(room);  // 클라이언트의 현재 방 상태 업데이트
            client.sendMessage("JOIN_SUCCESS|" + roomName);  // 성공 메시지 전송
            
            if (room.getPlayerCount() == 2) {
                room.startGame();
            }
        } else {
            client.sendMessage("JOIN_FAILED|" + roomName + "|방이 가득 찼거나 존재하지 않습니다.");
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        // 게임룸에서도 제거
        gameRooms.values().forEach(room -> room.removePlayer(client));
        broadcastRoomList();
    }

    public void broadcastRoomList() {
        String roomList = "ROOMLIST|" + String.join(",", gameRooms.keySet());
        for (ClientHandler client : clients) {
            client.sendMessage(roomList);
        }
    }

    public static void main(String[] args) {
        new BaseballServer().start();
    }
}