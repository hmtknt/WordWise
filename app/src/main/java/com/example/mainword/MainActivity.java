package com.example.mainword;

import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String[] categories = {"FRUITS", "ANIMALS"};

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        playerName = preferences.getString("playerName", "DefaultPlayerName");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        leaderboardRef = database.getReference("leaderboard");

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
        backgroundMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                backgroundMusic.seekTo(0);
                backgroundMusic.start();
            }
        });
        backgroundMusic.start();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGuess();
            }
        });

        guessEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                guessButton.performClick(); // Trigger the onClick for guessButton
                return true;
            }
            return false;
        });

        playNextCategory();
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
            String selectedCategory = intent.getStringExtra("selectedCategory");

            for (Categories categoryOption : Categories.values()) {
                if (categoryOption.toString().equalsIgnoreCase(selectedCategory)) {
                    category = categoryOption.toString();
                    break;
                }
            }

            if (category == null) {
                return;
            }
        } else {
            return;
        }

        categoryTextView.setText("Category: " + category.toUpperCase() + " - Player: " + playerName);

        playNextWord();
    }


    private void playNextWord() {
        if (totalWords < totalWordsPerCategory) {
            do {
                selectedWord = selectWord(category);
            } while (successfullyGuessedWords.contains(selectedWord));

            guessedLetters.clear();
            correctLetters.clear();
            points = selectedWord.length();
            updateWordDisplay();

            guessButton.setEnabled(true);
            resultTextView.setText("");
        } else {
            endCategory();
        }
    }

    private String selectWord(String category) {
        String[] words;
        if ("fruits".equals(category)) {
            words = getResources().getStringArray(R.array.Fruits);
        } else {
            words = getResources().getStringArray(R.array.Animals);
        }
        return words[new Random().nextInt(words.length)];
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

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                playNextWord();
            }
        }, 2000);
    }

    private void endCategory() {
        if (successfullyGuessedWords.size() == totalWordsPerCategory) {
            showCongratulations();
        } else {
            resultTextView.setText("Category Complete! Choose a new category.");

            if (totalWords >= totalWordsPerCategory) {
                resultTextView.append("\nCorrect words guessed in this category: " + totalWords + "/" + totalWordsPerCategory);
                resultTextView.append("\nCategory Points: " + categoryPointsMap.getOrDefault(category, 0));
            }

            playNextCategory();
        }
    }

    private void showCongratulations() {
        resultTextView.setText("Congratulations! You've completed this category!");
        playWinSound();

        for (String category : categories) {
            int categoryPoints = categoryPointsMap.getOrDefault(category, 0);
            resultTextView.append("\n" + category + " Points: " + categoryPoints);

            int categoryHighestPoints = getCategoryHighestPoints(category);
            categoryHighestPointsMap.put(category, Math.max(categoryPoints, categoryHighestPoints));
            saveCategoryHighestPoints(category, Math.max(categoryPoints, categoryHighestPoints));
        }

        totalPoints += getTotalCategoryPoints();
        resultTextView.append("\nTotal Points: " + totalPoints);

        LeaderboardEntry playerEntry = new LeaderboardEntry(playerName, totalPoints);
        String leaderboardKey = leaderboardRef.push().getKey();
        leaderboardRef.child(leaderboardKey).setValue(playerEntry);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                playNextCategory();
            }
        }, 5000);
        updateCategoryHighestPoints();
    }
    private void changePlayerName() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Change Player Name");

        final EditText input = new EditText(this);
        alertDialog.setView(input);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();

                playerName = newName;
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("playerName", playerName);
                editor.apply();

                LeaderboardEntry playerEntry = new LeaderboardEntry(playerName, totalPoints);
                String leaderboardKey = leaderboardRef.push().getKey();
                leaderboardRef.child(leaderboardKey).setValue(playerEntry);

                Toast.makeText(MainActivity.this, "Player name updated to " + newName, Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }
    private void updateCategoryHighestPoints() {
        String currentCategory = category.toUpperCase();
        int categoryHighestPoints = getCategoryHighestPoints(currentCategory);
        categoryHighestPointsTextView.setText("Highest Point: " + categoryHighestPoints);
    }

    private int getCategoryHighestPoints(String category) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        return preferences.getInt(category + "_highest_points", 0);
    }

    private int getTotalCategoryPoints() {
        int total = 0;
        for (String category : categories) {
            total += categoryPointsMap.getOrDefault(category, 0);
        }
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
