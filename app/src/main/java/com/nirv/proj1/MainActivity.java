package com.nirv.proj1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int DELAY_MILLISECONDS = 3000; // 3 seconds
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private StringBuilder conversationBuilder = new StringBuilder(); // to store conversation history

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Step 1: Show greeting message
        final TextView greetingTextView = findViewById(R.id.welcome_text);
        greetingTextView.setText("Welcome to KIDOOGLE");

        // Step 2: Hide greeting message after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                greetingTextView.setText("Ask your question please!"); // Hide greeting message
            }
        }, DELAY_MILLISECONDS);

        // Step 3: Display button with voice API functionality
        Button voiceButton = findViewById(R.id.circularButton);
        voiceButton.setVisibility(View.VISIBLE); // Make button visible
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request microphone permission
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
                } else {
                    // Permission already granted, proceed with voice API invocation
                    invokeVoiceAPI();
                }
            }
        });
    }

    private void invokeVoiceAPI() {
        // Create an instance of Intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String recognizedSpeech = matches.get(0);
                TextView greetingTextView = findViewById(R.id.welcome_text);

                // Append the recognized speech to the conversation
                conversationBuilder.append(recognizedSpeech).append("\n");
                greetingTextView.setText(conversationBuilder.toString());

                // Send the recognized speech to the GPT API
                try {
                    sendSpeechToGPT(recognizedSpeech);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with voice API invocation
                invokeVoiceAPI();
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable microphone functionality)
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSpeechToGPT(String speech) throws JSONException {
        // Create an instance of GPTRequestHandler
        GPTRequestHandler gptRequestHandler = new GPTRequestHandler();

        // Send a request to the GPT API with the recognized speech
        gptRequestHandler.sendGPTRequest(speech, new GPTRequestHandler.GPTResponseListener() {
            @Override
            public void onResponse(String response) {
                // Handle the GPT API response here
                // Append the response below the conversation
                conversationBuilder.append(response).append("\n");
                TextView greetingTextView = findViewById(R.id.welcome_text);
                greetingTextView.setText(conversationBuilder.toString());
            }

            @Override
            public void onError(final String error) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Display the error message as a Toast
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}