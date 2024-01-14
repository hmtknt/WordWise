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
import androidx.appcompat.app.AppCompatActivity;

public class CategorySelectionActivity extends AppCompatActivity {

    private String[] categories = {"Fruits", "Animals"};
    private int[] categoryLogos = {R.drawable.fruits_logo, R.drawable.animals_logo}; // Add more for additional categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        ListView categoryListView = findViewById(R.id.categoryListView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_category, R.id.categoryNameTextView, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_category, null);
                }

                ImageView logoImageView = view.findViewById(R.id.categoryLogoImageView);
                TextView nameTextView = view.findViewById(R.id.categoryNameTextView);

                // Set logo and category name
                logoImageView.setImageResource(categoryLogos[position]);
                nameTextView.setText(categories[position]);

                return view;
            }
        };

        categoryListView.setAdapter(adapter);

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position].toString().toLowerCase(); // Convert to lowercase

                // Pass the selected category to MainActivity
                Intent intent = new Intent(CategorySelectionActivity.this, MainActivity.class);
                intent.putExtra("selectedCategory", selectedCategory);
                startActivity(intent);
            }
        });
    }
}
