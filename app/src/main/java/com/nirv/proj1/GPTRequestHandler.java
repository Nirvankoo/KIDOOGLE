package com.nirv.proj1;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GPTRequestHandler {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static String API_KEY = null;

    private static RequestCount requestCount = new RequestCount();

    // Constructor to retrieve API key from Firebase Realtime Database
    public GPTRequestHandler() {
        retrieveApiKeyFromFirebase();
    }

    // Method to retrieve API key from Firebase
    private void retrieveApiKeyFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("apiKeys/openai/apiKey");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                API_KEY = dataSnapshot.getValue(String.class);
                Log.d("API_KEY", "API Key: " + API_KEY);
                // Once API key is retrieved, send GPT request
                if (API_KEY != null) {
                    try {
                        sendGPTRequest("Your speech here", new GPTResponseListener() {
                            @Override
                            public void onResponse(String response) {
                                // Handle response
                            }

                            @Override
                            public void onError(String error) {
                                // Handle error
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle case where API key is null
                    Log.e("API_KEY", "API key is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors in retrieving data from Firebase
                Log.e("Firebase", "Error retrieving API key: " + databaseError.getMessage());
            }
        });
    }

    // Method to send GPT request using the retrieved API key
    public static void sendGPTRequest(String speech, final GPTResponseListener listener) throws JSONException {

        requestCount.incrementRequestCount();


        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", speech);
        String jsonText = gson.toJson(jsonObject);

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");

        JSONArray messagesArray = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You explaining to kids");
        messagesArray.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", speech); // Set the content to the recognized speech
        messagesArray.put(userMessage);

        requestBody.put("messages", messagesArray);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0);

        String jsonBody = requestBody.toString();

        RequestBody body = RequestBody.create(mediaType, jsonBody);
        Request request = new Request.Builder()
                .url(GPT_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                listener.onError("Request failed: " + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                //Log.d("Request Body", String.valueOf(request));// Print the request body to Logcat
                //Log.d("JSON Response", responseBody);

                // Post UI updates to the main thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Parse the JSON response and handle accordingly
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);
                            JSONArray choices = jsonObject.getJSONArray("choices");
                            if (choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject message = choice.getJSONObject("message");
                                if (message.has("content")) {
                                    String responseMessage = message.getString("content");
                                    listener.onResponse(responseMessage);
                                } else {
                                    listener.onError("Missing 'content' field in JSON response");
                                }
                            } else {
                                listener.onError("No 'choices' array found in JSON response");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onError("Error parsing JSON response: " + e.getMessage());
                        }
                    }
                });
            }

        });


    }





    // Interface for response callbacks
    public interface GPTResponseListener {
        void onResponse(String response);
        void onError(String error);
    }
}
