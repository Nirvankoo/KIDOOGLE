package com.nirv.proj1;


import android.os.Handler;
import android.os.Looper;

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

    // Constructor to retrieve API key from Firebase Realtime Database
    public GPTRequestHandler() {
        retrieveApiKeyFromFirebase();
    }

    // Method to retrieve API key from Firebase
    private void retrieveApiKeyFromFirebase() {
        // Your implementation to retrieve API key from Firebase
    }

    // Method to send GPT request using the retrieved API key
    public static void sendGPTRequest(String speech, final GPTResponseListener listener) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", speech);
        String jsonText = gson.toJson(jsonObject);
        API_KEY = "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";

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");

        JSONArray messagesArray = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
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

                // Parse the JSON response
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseBody);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Extract the message from the JSON object
                JSONArray choices = null;
                try {
                    choices = jsonObject.getJSONArray("choices");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                JSONObject choice = null; // Assuming there's only one choice
                try {
                    choice = choices.getJSONObject(0);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                JSONObject message = null;
                try {
                    message = choice.getJSONObject("message");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String responseMessage = null;
                try {
                    responseMessage = message.getString("content");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Post the response message to the main thread
                String finalResponseMessage = responseMessage;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResponse(finalResponseMessage);
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
