package com.example.mainword;

public class LeaderboardEntry {
    private String playerName;
    private int points;

    public LeaderboardEntry() {
        // Default constructor required for Firebase
    }

    public LeaderboardEntry(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return playerName + ": " + points + " points";
    }
}
