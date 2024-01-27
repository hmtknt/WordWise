package com.example.mainword;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardEntry {
    private String playerName;
    private int totalPoints;
    private Map<String, Integer> categoryPoints;

    public LeaderboardEntry() {
        // Default constructor required for Firebase
        categoryPoints = new HashMap<>();
    }

    public LeaderboardEntry(String playerName, int totalPoints, Map<String, Integer> categoryPoints) {
        this.playerName = playerName;
        this.totalPoints = totalPoints;
        this.categoryPoints = categoryPoints;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public Map<String, Integer> getCategoryPoints() {
        return categoryPoints;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(playerName + ": " + totalPoints + " points");

        if (categoryPoints != null && !categoryPoints.isEmpty()) {
            for (Map.Entry<String, Integer> entry : categoryPoints.entrySet()) {
                result.append("\n").append(entry.getKey()).append(": ").append(entry.getValue()).append(" points");
            }
        }

        return result.toString();
    }
}



