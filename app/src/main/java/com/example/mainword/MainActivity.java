package com.example.mainword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<String> categories = new ArrayList<>(); // Add this line

    private TextView categoryTextView, currentWordTextView, pointsTextView, resultTextView, categoryHighestPointsTextView;
    private EditText guessEditText;
    private Button guessButton;
    private String category;
    private String selectedWord;
    private Set<Character> guessedLetters;
    private Set<Character> correctLetters;
    private int points;
    private DatabaseReference leaderboardRef;
    private int totalWordsPerCategory = 1;
    private int totalWords = 0;
    private int totalPoints = 0;
    private Map<String, Integer> categoryPointsMap = new HashMap<>();
    private Map<String, Integer> categoryHighestPointsMap = new HashMap<>();
    private Set<String> successfullyGuessedWords = new HashSet<>();
    private MediaPlayer winSound;
    private MediaPlayer backgroundMusic;
    private MediaPlayer correctGuessSound;
    private MediaPlayer incorrectGuessSound;
    private String playerName;
    private DatabaseReference categoriesRef;
    private int currentCategoryIndex = 0;
    private Category selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        playerName = preferences.getString("playerName", "DefaultPlayerName");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        leaderboardRef = database.getReference("leaderboard");

        // Initialize categoriesRef
        categoriesRef = database.getReference("categories");

        categoryTextView = findViewById(R.id.categoryTextView);
        currentWordTextView = findViewById(R.id.currentWordTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        resultTextView = findViewById(R.id.resultTextView);
        guessEditText = findViewById(R.id.guessEditText);
        guessButton = findViewById(R.id.guessButton);
        categoryHighestPointsTextView = findViewById(R.id.categoryHighestPointsTextView);

        winSound = MediaPlayer.create(this, R.raw.win_sound);
        correctGuessSound = MediaPlayer.create(this, R.raw.correctguess);
        incorrectGuessSound = MediaPlayer.create(this, R.raw.incorrectguess);

        backgroundMusic = MediaPlayer.create(this, R.raw.backmusic);
        backgroundMusic.setLooping(false);
        backgroundMusic.setOnCompletionListener(mp -> {
            backgroundMusic.seekTo(0);
            backgroundMusic.start();
        });
        backgroundMusic.start();

        // Commented out ArrayAdapter, as we will retrieve categories dynamically

        guessButton.setOnClickListener(v -> checkGuess());

        guessEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                guessButton.performClick();
                return true;
            }
            return false;
        });

        // Retrieve categories from the database
        retrieveCategories();
    }

    private void retrieveCategories() {
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear(); // Clear existing categories
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    categories.add(categoryName);
                }
                playNextCategory(); // Once categories are retrieved, play the next category
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources when the activity is destroyed
        if (backgroundMusic != null) {
            backgroundMusic.release();
        }
    }

    private void playNextCategory() {
        guessedLetters = new HashSet<>();
        correctLetters = new HashSet<>();
        points = 0;

        totalWords = 0;
        successfullyGuessedWords.clear();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedCategory")) {
            category = intent.getStringExtra("selectedCategory");

            if (category == null) {
                return;
            }
        } else {
            // All categories played, end the game or handle as needed
            resultTextView.setText("Game Over! All categories played.");
            return;
        }

        categoryTextView.setText("Category: " + category.toUpperCase() + " - Player: " + playerName);

        // Retrieve the list of words for the selected category
        getWordsForCategory(category);
    }
    private void getWordsForCategory(String category) {
        DatabaseReference categoryRef = categoriesRef.child(category).child("words");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> wordsList = new ArrayList<>();

                for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                    String word = wordSnapshot.getValue(String.class);
                    wordsList.add(word);
                }

                // Check if the wordsList is not empty
                if (!wordsList.isEmpty()) {
                    totalWordsPerCategory = wordsList.size();
                    playNextWord(); // Call playNextWord only when words are retrieved successfully
                } else {
                    // Handle the case when there are no words for the selected category
                    resultTextView.setText("No words available for the selected category: " + category);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Choose a new category or handle as needed
                            // For now, we'll just choose a new category
                            playNextCategory();
                        }
                    }, 2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }
    private void playNextWord() {
        if (totalWords < totalWordsPerCategory) {
            DatabaseReference wordsRef = FirebaseDatabase.getInstance().getReference("categories").child(category).child("words");

            wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> words = new ArrayList<>();

                    for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                        words.add(wordSnapshot.getValue(String.class));
                    }

                    // Shuffle the words and select one that hasn't been used before
                    Collections.shuffle(words);

                    for (String word : words) {
                        if (!successfullyGuessedWords.contains(word)) {
                            selectedWord = word;
                            break;
                        }
                    }

                    guessedLetters.clear();
                    correctLetters.clear();
                    points = selectedWord.length();
                    updateWordDisplay();

                    guessButton.setEnabled(true);
                    resultTextView.setText("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        } else {
            endCategory();
        }
    }

    private void updateWordDisplay() {
        StringBuilder displayWord = new StringBuilder();
        for (char letter : selectedWord.toCharArray()) {
            if (guessedLetters.contains(letter)) {
                displayWord.append(letter);
            } else {
                displayWord.append("_");
            }
        }
        currentWordTextView.setText("Current Word: " + displayWord.toString());
        pointsTextView.setText("Points: " + points);
    }

    private void checkGuess() {
        String guess = guessEditText.getText().toString().toLowerCase();

        if (guess.length() == 1) {
            char guessedLetter = guess.charAt(0);

            if (Character.isLetter(guessedLetter) && !guessedLetters.contains(guessedLetter)) {
                guessedLetters.add(guessedLetter);

                if (selectedWord.contains(String.valueOf(guessedLetter).toLowerCase())) {
                    correctLetters.add(guessedLetter);
                    correctGuessSound.start(); // Play correct guess sound
                } else {
                    points--;
                    resultTextView.setText("Incorrect! Keep guessing.");
                    incorrectGuessSound.start(); // Play incorrect guess sound
                }
            } else {
                resultTextView.setText("Invalid input. Please enter a single, new letter.");
                return;
            }
        } else if (guess.length() > 1) {
            if (guess.equalsIgnoreCase(selectedWord)) {
                correctGuess();
            } else {
                points--;
                resultTextView.setText("Incorrect! Keep guessing.");
                incorrectGuessSound.start(); // Play incorrect guess sound
            }
        } else {
            resultTextView.setText("Invalid input. Please enter a letter or the whole word.");
            return;
        }

        updateWordDisplay();

        if (correctLetters.size() == selectedWord.length()) {
            correctGuess();
        }

        if (successfullyGuessedWords.size() == totalWordsPerCategory) {
            showCongratulations();
        }

        if (points <= 0) {
            resultTextView.setText("Out of points! The correct word was: " + selectedWord);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    playNextWord();
                }
            }, 2000);
        }
    }


    private void correctGuess() {
        resultTextView.setText("Congratulations! You guessed the word: " + selectedWord);
        correctGuessSound.start();

        int categoryPoints = categoryPointsMap.getOrDefault(category, 0);
        categoryPointsMap.put(category, categoryPoints + points);

        resultTextView.append("\nPoints for this word: " + points);

        successfullyGuessedWords.add(selectedWord);
        totalWords++;

        totalPoints += points;

        guessButton.setEnabled(false);

        guessEditText.setText("");

        // Update category points in the leaderboard
        updateCategoryPointsInLeaderboard(category, categoryPoints + points);
        Log.d("MainActivity", "Category Points Map: " + categoryPointsMap);

        new Handler(Looper.getMainLooper()).postDelayed(() -> playNextWord(), 2000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                playNextWord();
            }
        }, 2000);
    }

    // Add this method to update category points in the leaderboard
    // Add this method to update category points in the leaderboard
    private void updateCategoryPointsInLeaderboard(String category, int points) {
        DatabaseReference playerRef = leaderboardRef.child(playerName).child("categoryPoints");

        // Format the category name consistently
        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();

        // Retrieve the existing total points and category points from the leaderboard
        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer existingCategoryPoints = dataSnapshot.child(formattedCategory + "Points").getValue(Integer.class);
                Integer existingTotalPoints = dataSnapshot.child("totalPoints").getValue(Integer.class);

                // Additional null checks
                existingCategoryPoints = (existingCategoryPoints != null) ? existingCategoryPoints : 0;
                existingTotalPoints = (existingTotalPoints != null) ? existingTotalPoints : 0;

                // Update category points in the leaderboard
                playerRef.child(formattedCategory + "Points").setValue(points);

                // Update the total points with the correct cumulative value
                int newTotalPoints = existingTotalPoints - existingCategoryPoints + points;
                playerRef.child("totalPoints").setValue(newTotalPoints);

                // Update the local totalPoints variable
                totalPoints = newTotalPoints;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void endCategory() {
        if (successfullyGuessedWords.size() == totalWordsPerCategory) {
            showCongratulations();
        } else {
            resultTextView.setText("Category Complete! Choose a new category.");

            if (totalWords >= totalWordsPerCategory) {
                resultTextView.append("\nCorrect words guessed in this category: " + totalWords + "/" + totalWordsPerCategory);
                resultTextView.append("\nCategory Points: " + categoryPointsMap.getOrDefault(category, 0));

                // Save category points under the player's name
                DatabaseReference playerCategoryPointsRef = leaderboardRef.child(playerName).child("CategoryPoints");
                playerCategoryPointsRef.child(category + "Points").setValue(categoryPointsMap.getOrDefault(category, 0));
            }

            playNextCategory();
        }
    }

    private void showCongratulations() {
        resultTextView.setText("Congratulations! You've completed this category!");
        playWinSound();

        Map<String, Integer> playerCategoryPoints = new HashMap<>();

        int categoryPoints = categoryPointsMap.getOrDefault(category, 0);
        resultTextView.append("\n" + category + ": " + categoryPoints + " points");
        totalPoints += getTotalCategoryPoints();
        resultTextView.append("\nTotal Points: " + totalPoints);


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                playNextCategory();
            }
        }, 5000);

    }

    private void savePlayerPoints(String playerName, int totalPoints, Map<String, Integer> categoryPoints) {
        LeaderboardEntry playerEntry = new LeaderboardEntry(playerName, totalPoints, categoryPoints);

        // Save category points under the player's name
        DatabaseReference playerRef = leaderboardRef.child(playerName);
        playerRef.setValue(playerEntry);
    }

    private void updateCategoryHighestPoints() {
        for (String category : categories) {
            int categoryHighestPoints = getCategoryHighestPoints(category);
            Log.d("MainActivity", "Retrieved highest points for " + category + ": " + categoryHighestPoints);
        }
    }

    private int getCategoryHighestPoints(String category) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
        return preferences.getInt(formattedCategory + "_highest_points", 0);
    }

    private int getTotalCategoryPoints() {
        int total = 0;
        total += categoryPointsMap.getOrDefault(category, 0);
        return total;
    }

    private void playWinSound() {
        winSound.start();
    }

    private void saveCategoryHighestPoints(String category, int highestPoints) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(category + "_highest_points", highestPoints);
        editor.apply();

        // Add log statement for debugging
        Log.d("MainActivity", "Saved highest points for " + category + ": " + highestPoints);
    }
}
