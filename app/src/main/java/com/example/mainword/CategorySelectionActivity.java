package com.example.mainword;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectionActivity extends AppCompatActivity {

    private DatabaseReference categoriesRef;
    private ListView categoryListView;
    private List<Category> categoriesList;
    private Button requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        categoryListView = findViewById(R.id.categoryListView);
        requestButton = findViewById(R.id.requestButton);
        categoriesList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        categoriesRef = database.getReference("categories");

        populateCategoriesList();

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRequestDialog();
            }
        });
    }

    private void populateCategoriesList() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoriesList.clear();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = Category.fromSnapshot(categorySnapshot);
                    categoriesList.add(category);
                }

                displayCategories();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }
    private void showRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request A Category");

        // Set up the input
        final EditText input = new EditText(this);
        input.setBackgroundColor(getResources().getColor(androidx.cardview.R.color.cardview_light_background));
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryRequest = input.getText().toString().trim();
                if (!categoryRequest.isEmpty()) {
                    saveCategoryRequest(categoryRequest);
                } else {
                    Toast.makeText(CategorySelectionActivity.this, "Please enter a category request", Toast.LENGTH_SHORT).show();
                }
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
        alertDialog.getWindow().setBackgroundDrawableResource(androidx.cardview.R.color.cardview_shadow_end_color);

        alertDialog.show();
    }


    private void saveCategoryRequest(String categoryRequest) {
        // Save the category request to the Firebase database
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("category_requests");
        String requestId = requestsRef.push().getKey();
        requestsRef.child(requestId).setValue(categoryRequest);

        Toast.makeText(this, "Category request submitted", Toast.LENGTH_SHORT).show();
    }

    private void displayCategories() {
        CategoryAdapter adapter = new CategoryAdapter(this, R.layout.list_item_category, categoriesList);
        categoryListView.setAdapter(adapter);

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = categoriesList.get(position);
                String categoryName = selectedCategory.getName();

                // Pass the selected category name to MainActivity
                Intent intent = new Intent(CategorySelectionActivity.this, MainActivity.class);
                intent.putExtra("selectedCategory", categoryName);
                startActivity(intent);
            }
        });
    }
}

