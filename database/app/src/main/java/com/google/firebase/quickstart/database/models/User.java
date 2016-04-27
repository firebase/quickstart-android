package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;

    public User() {

    }

    public User(String username) {
        this.username = username;
    }

}
