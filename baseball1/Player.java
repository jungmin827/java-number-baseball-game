package baseball1;

public class Player {
    private String name;
    private int attempts;

    public Player(String name) {
        this.name = name;
        this.attempts = 0;
    }

    public String getName() {
        return name;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        attempts++;
    }

    public void resetAttempts() {
        attempts = 0;
    }
}