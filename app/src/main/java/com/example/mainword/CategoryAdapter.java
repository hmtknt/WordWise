package com.example.mainword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {

    public CategoryAdapter(Context context, int resource, List<Category> categories) {
        super(context, resource, categories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_category, parent, false);
        }

        Category category = getItem(position);

        // Set the category name and logo using Picasso for image loading
        TextView categoryNameTextView = convertView.findViewById(R.id.categoryNameTextView);
        categoryNameTextView.setText(category.getName());

        ImageView categoryLogoImageView = convertView.findViewById(R.id.categoryLogoImageView);
        Picasso.get().load(category.getLogoUrl()).into(categoryLogoImageView);

        return convertView;
    }
}
