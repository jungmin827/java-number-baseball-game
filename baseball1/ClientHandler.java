package baseball1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket socket;
    private BaseballServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private GameRoom currentRoom;

    public ClientHandler(Socket socket, BaseballServer server) {
        this.socket = socket;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            server.removeClient(this);
        } finally {
            closeConnection();
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\|");
        String command = parts[0];

        switch (command) {
            case "LOGIN":
                setPlayerName(parts[1]);
                break;
            case "CREATE":
                currentRoom = server.createGameRoom(parts[1]);
                break;
            case "JOIN":
                server.joinGameRoom(parts[1], this);
                break;
            case "GUESS":
                if (currentRoom != null) {
                    currentRoom.processGuess(this, parts[1]);
                }
                break;
            case "CHAT":
                if (currentRoom != null) {
                    currentRoom.broadcastChat(playerName + ": " + parts[1]);
                }
                break;
            case "LEAVE":
                if (currentRoom != null) {
                    currentRoom.removePlayer(this);
                    currentRoom = null;
                }
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    private void closeConnection() {
        try {
            if (currentRoom != null) {
                currentRoom.removePlayer(this);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void finalize() throws Throwable {
        closeConnection();
        super.finalize();
    }
}
