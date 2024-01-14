package com.example.mainword;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class FrontPage extends AppCompatActivity {

    private Button startButton;
    private Button leaderButton;
    private Button userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_front_page);

        startButton = findViewById(R.id.startButton);
        leaderButton = findViewById(R.id.LeaderButton);
        userButton = findViewById(R.id.userButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FrontPage.this, CategorySelectionActivity.class));
            }
        });

        leaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FrontPage.this, LeaderboardActivity.class));
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayerName();
            }
        });
    }

    private void changePlayerName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Player Name");

        // Add an EditText to the dialog to get the new player name
        final EditText input = new EditText(this);
        input.setHint("Enter your name");  // Add a hint

        // Set the background color of the EditText
        input.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_dark_background));

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerName = input.getText().toString().trim();

                // Save the new player name to SharedPreferences
                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("playerName", playerName);
                editor.apply();

                // Notify the user about the successful name change
                Toast.makeText(FrontPage.this, "Player Name changed to: " + playerName, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();

        // Set the background color of the entire AlertDialog
        alertDialog.getWindow().setBackgroundDrawableResource(androidx.cardview.R.color.cardview_dark_background);

        alertDialog.show();
    }

}
