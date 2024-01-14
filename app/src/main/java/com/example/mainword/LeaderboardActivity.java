package com.example.mainword;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private DatabaseReference leaderboardRef;
    private ListView leaderboardListView;
    private List<LeaderboardEntry> leaderboardEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboardListView);
        leaderboardEntries = new ArrayList<>();

        // Initialize Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        leaderboardRef = database.getReference("leaderboard");

        // Load and display leaderboard data
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        leaderboardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leaderboardEntries.clear();

                for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    LeaderboardEntry entry = entrySnapshot.getValue(LeaderboardEntry.class);
                    leaderboardEntries.add(entry);
                }

                // Sort leaderboardEntries based on points (descending order)
                Collections.sort(leaderboardEntries, (entry1, entry2) -> entry2.getPoints() - entry1.getPoints());

                // Display the leaderboard
                displayLeaderboard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void displayLeaderboard() {
        ArrayAdapter<LeaderboardEntry> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardEntries);
        leaderboardListView.setAdapter(adapter);
    }
}
