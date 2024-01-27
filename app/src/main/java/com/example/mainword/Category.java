package com.example.mainword;

import com.google.firebase.database.DataSnapshot;

public class Category {
    private String name;
    private String logoUrl;
    public static Category fromSnapshot(DataSnapshot snapshot) {
        String name = snapshot.child("name").getValue(String.class);
        String logoUrl = snapshot.child("logoUrl").getValue(String.class);
        return new Category(name, logoUrl);
    }
    public Category() {
        // Default constructor required for Firebase
    }

    public Category(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}
