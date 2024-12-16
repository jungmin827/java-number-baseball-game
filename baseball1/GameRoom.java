package baseball1;

import java.util.ArrayList;
import java.util.List;

class GameRoom {
    private String roomName;
    private List<ClientHandler> players = new ArrayList<>();
    private GameLogic gameLogic = new GameLogic();
    private boolean isGameStarted = false;

    public GameRoom(String roomName) {
        this.roomName = roomName;
    }

    public synchronized void addPlayer(ClientHandler player) {
        if (players.size() < 2) {
            players.add(player);
            gameLogic.addPlayer(player.getPlayerName());  // GameLogic에 플레이어 추가
            broadcastMessage("PLAYER_JOINED|" + player.getPlayerName());
            
            // 플레이어가 2명이 되면 게임 시작
            if (players.size() == 2) {
                startGame();
            }
        }
    }

    public synchronized void removePlayer(ClientHandler player) {
        players.remove(player);
        if (isGameStarted) {
            endGame();
        }
    }

    public void startGame() {
        isGameStarted = true;
        // 기존 게임로직을 유지하고 플레이어들을 다시 등록
        for (ClientHandler player : players) {
            gameLogic.addPlayer(player.getPlayerName());
        }
        broadcastMessage("GAME_START");
        broadcastMessage("TURN_UPDATE|" + gameLogic.getCurrentTurn());
    }
    
    
    public void processGuess(ClientHandler player, String guess) {
    	
        String result = gameLogic.processGuess(player.getPlayerName(), guess);
        
        broadcastMessage(result); // 그대로 전달
        // 게임 종료 체크
        if (gameLogic.isGameOver()) {
            endGame();
        } else {
            broadcastMessage("TURN_UPDATE|" + gameLogic.getCurrentTurn());
        }
    }

    
    public void broadcastChat(String message) {
        broadcastMessage("CHAT|" + message);
    }

    private void broadcastMessage(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    private void endGame() {
        isGameStarted = false;
        broadcastMessage("GAME_END|" + gameLogic.getWinner());
    }

    public int getPlayerCount() {
        return players.size();
    }
}