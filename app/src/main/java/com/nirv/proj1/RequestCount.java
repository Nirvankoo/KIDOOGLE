package com.nirv.proj1;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class RequestCount {

    public void incrementRequestCount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        System.out.println("!!!!!!!!!!!!!!!" +user);


        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            String displayName = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();

            System.out.println("User ID: " + uid);
            System.out.println("Email: " + email);
            System.out.println("Display Name: " + displayName);
            System.out.println("Photo URL: " + photoUrl);
        } else {
            System.out.println("User is not signed in.");
        }


        if (user != null) {
            String uid = user.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://kidoogle-default-rtdb.firebaseio.com/").getReference();
            DatabaseReference usersRef = databaseRef.child("users").child(uid).child("numReq");

            // Run transaction to increment numReq by 1
            usersRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Integer currentValue = mutableData.getValue(Integer.class);
                    if (currentValue == null) {
                        // Set initial value if it doesn't exist
                        mutableData.setValue(1);
                    } else {
                        // Increment value by 1
                        mutableData.setValue(currentValue + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        // Transaction failed
                        System.out.println("Transaction failed: " + databaseError.getMessage());
                    } else {
                        // Transaction completed successfully
                        System.out.println("Transaction completed successfully");
                    }
                }
            });
        } else {
            System.out.println("User not authenticated");
        }
    }
}
