package com.example.mainword;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        categoryListView = findViewById(R.id.categoryListView);
        categoriesList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        categoriesRef = database.getReference("categories");

        populateCategoriesList();
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

