package com.nirv.proj1;

import com.google.firebase.auth.FirebaseUser;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;

    public User(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            email = firebaseUser.getEmail();

        }
    }

    // Getter methods
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

}
