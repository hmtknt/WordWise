package com.example.mainword;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardEntry {
    private String playerName;
    private Map<String, Integer> categoryPoints;

    public LeaderboardEntry(String playerName, int totalPoints, Map<String, Integer> categoryPoints) {
        // Default constructor required for Firebase
        this.categoryPoints = new HashMap<>();
    }

    public LeaderboardEntry(String playerName, Map<String, Integer> categoryPoints) {
        this.playerName = playerName;
        this.categoryPoints = categoryPoints;
    }

    public String getPlayerName() {
        return playerName;
    }
    public int getTotalPoints() {
        // Calculate and return the total points as the sum of category points
        return categoryPoints.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<String, Integer> getCategoryPoints() {
        return categoryPoints;
    }
}



