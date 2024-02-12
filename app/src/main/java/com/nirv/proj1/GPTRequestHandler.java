package com.nirv.proj1;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GPTRequestHandler {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static String API_KEY = null;

    // Constructor to retrieve API key from Firebase Realtime Database
    public GPTRequestHandler() {
        retrieveApiKeyFromFirebase();
    }

    // Method to retrieve API key from Firebase
    private void retrieveApiKeyFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference apiKeyRef = database.getReference("apiKeys/openai/apiKey");

        apiKeyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                API_KEY = dataSnapshot.getValue(String.class);
                // API key retrieved from Firebase
                if (API_KEY != null) {
                    System.out.println("API Key retrieved successfully: " + API_KEY);
                } else {
                    System.err.println("Failed to retrieve API Key from Firebase.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                System.err.println("Firebase database error: " + databaseError.getMessage());
            }
        });
    }

    // Method to send GPT request using the retrieved API key
    public static void sendGPTRequest(String text, final GPTResponseListener listener) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        String jsonText = gson.toJson(jsonObject);
        API_KEY = "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";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonText);
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                listener.onError("Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    listener.onError("Unexpected response code: " + response.code());
                    return;
                }
                String responseBody = response.body().string();
                listener.onResponse(responseBody);
            }
        });
    }

    // Interface for response callbacks
    public interface GPTResponseListener {
        void onResponse(String response);
        void onError(String error);
    }
}