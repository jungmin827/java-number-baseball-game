package baseball1;

import java.util.*;
import java.util.stream.Collectors;

public class GameLogic {
    private Map<String, Player> players = new HashMap<>();
    private int[] targetNumbers = new int[3];
    private String currentTurn;
    private String winner;
    private int roundNumber = 1;
    private Map<String, Integer> scores = new HashMap<>();

    public GameLogic() {
        generateTargetNumbers();
    }

    public void addPlayer(String playerName) {
        players.put(playerName, new Player(playerName));
        scores.put(playerName, 0);
        if (currentTurn == null) {
            currentTurn = playerName;
        }
    }

    public String processGuess(String playerName, String guess) {
        // 1. 현재 턴 확인
        if (!playerName.equals(currentTurn)) {
            return "NOT_YOUR_TURN|다른 플레이어의 턴입니다.";
        }

        // 2. 플레이어 확인
        Player player = players.get(playerName);
        if (player == null) {
            return "ERROR|플레이어가 존재하지 않습니다.";
        }

        // 3. 입력값 파싱
        int[] guessNumbers = parseGuess(guess);
        if (guessNumbers == null) {
            return "ERROR|잘못된 입력입니다. 숫자 3개를 입력하세요.";
        }

        // 4. 스트라이크/볼 계산
        int strikes = 0, balls = 0;
        boolean[] targetUsed = new boolean[3];
        boolean[] guessUsed = new boolean[3];

        // 스트라이크 계산
        for (int i = 0; i < 3; i++) {
            if (targetNumbers[i] == guessNumbers[i]) {
                strikes++;
                targetUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // 볼 계산
        for (int i = 0; i < 3; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < 3; j++) {
                    if (!targetUsed[j] && targetNumbers[j] == guessNumbers[i]) {
                        balls++;
                        targetUsed[j] = true;
                        guessUsed[i] = true;
                        break;
                    }
                }
            }
        }

        // 5. 시도 횟수 증가
        player.incrementAttempts();

        if (strikes == 3) {
            winner = playerName;
            scores.put(playerName, scores.get(playerName) + 1);
            return "GUESS|" + String.format("%s가 입력한 숫자: %s | 결과: 3 스트라이크! 홈런!", 
                playerName, guess);
        } else if (strikes == 0 && balls == 0) {
            switchTurn();
            return "GUESS|" + String.format("%s가 입력한 숫자: %s | 결과: 아웃! 숫자가 전혀 일치하지 않습니다.", 
                playerName, guess);
        } else {
            switchTurn();
            return "GUESS|" + String.format("%s가 입력한 숫자: %s | 결과: %d 스트라이크, %d 볼", 
                playerName, guess, strikes, balls);
        }
    }


    private void switchTurn() {
        List<String> playerNames = new ArrayList<>(players.keySet());
        int currentIndex = playerNames.indexOf(currentTurn);
        currentTurn = playerNames.get((currentIndex + 1) % playerNames.size());
    }

    public boolean isGameOver() {
        return winner != null;
    }

    public String getWinner() {
        return winner;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

    public void resetGame() {
        generateTargetNumbers();
        winner = null;
        roundNumber++;
        for (Player player : players.values()) {
            player.resetAttempts();
        }
        // 이전 라운드 패자가 먼저 시작
        if (players.size() == 2) {
            List<String> playerNames = new ArrayList<>(players.keySet());
            currentTurn = playerNames.get(0);
        }
    }

    private void generateTargetNumbers() {
        Random random = new Random();
        Set<Integer> uniqueNumbers = new HashSet<>();
        while (uniqueNumbers.size() < 3) {
            uniqueNumbers.add(random.nextInt(9) + 1);
        }
        int index = 0;
        for (int number : uniqueNumbers) {
            targetNumbers[index++] = number;
        }
        
        // 콘솔에 난수 출력 추가
        System.out.println("이번 라운드 정답 숫자: " + 
            Arrays.stream(targetNumbers)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining()));
    }

    private int[] parseGuess(String guess) {
        if (guess.length() != 3) return null;
        int[] numbers = new int[3];
        for (int i = 0; i < 3; i++) {
            if (!Character.isDigit(guess.charAt(i))) return null;
            numbers[i] = Character.getNumericValue(guess.charAt(i));
        }
        return numbers;
    }

    @SuppressWarnings("unused")
	private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) return true;
        }
        return false;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
}