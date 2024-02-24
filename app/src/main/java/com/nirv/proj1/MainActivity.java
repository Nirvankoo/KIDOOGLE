package com.nirv.proj1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int DELAY_MILLISECONDS = 3000; // 3 seconds
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private StringBuilder conversationBuilder = new StringBuilder(); // to store conversation history
    private TextView confirm_question;
    private Button conf_button_yes;
    private Button conf_button_no;
    private TextView answer;
    private TextToSpeechManager textToSpeechManager;
    private AlertDialog loadingDialog;



    //menu
    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.main_menu_log_out) {
            logout();
            return true; // Return true to indicate that the action has been handled
        }

        return super.onOptionsItemSelected(item); // If it's not the logout action, delegate to superclass
    }


    private void logout() {
        // Perform logout actions here
        // For example, if you're using Firebase authentication, you can sign out the current user:
        FirebaseAuth.getInstance().signOut();

        // After logging out, you may want to redirect the user to the login screen
        // Start LoginActivity and finish MainActivity so the user can't navigate back to it with the back button
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        answer = findViewById(R.id.answer_txt);

        // Step 1: Show greeting message
        final TextView greetingTextView = findViewById(R.id.welcome_text);
        greetingTextView.setText("Welcome to KIDOOGLE");
        //do test for API
        try {
            sendSpeechToGPT("test");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        confirm_question = findViewById(R.id.confirm_question);
        conf_button_yes = findViewById(R.id.conf_button_yes);
        conf_button_no = findViewById(R.id.confirm_button_no);

        // Initially hide the confirmation components
        confirm_question.setVisibility(View.INVISIBLE);
        conf_button_yes.setVisibility(View.INVISIBLE);
        conf_button_no.setVisibility(View.INVISIBLE);

        // Initialize TextToSpeechManager
        textToSpeechManager = new TextToSpeechManager(this);
        //textToSpeechManager.speak(text);

        // Step 3: Display button with voice API functionality
        Button voiceButton = findViewById(R.id.circularButton);
        voiceButton.setVisibility(View.VISIBLE); // Make button visible
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request microphone permission
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
                    startAnimation();
                } else {
                    // Permission already granted, proceed with voice API invocation
                    invokeVoiceAPI();
                }
            }

            private void startAnimation() {
                ImageView imageView = findViewById(R.id.imageView);

                // Create a ScaleAnimation
                Animation animation = new ScaleAnimation(
                        1f, // fromXScale (start scale X)
                        1.5f, // toXScale (end scale X)
                        1f, // fromYScale (start scale Y)
                        1.5f, // toYScale (end scale Y)
                        Animation.RELATIVE_TO_SELF, 0.5f, // pivotXType, pivotXValue
                        Animation.RELATIVE_TO_SELF, 0.5f); // pivotYType, pivotYValue
                animation.setDuration(5000);

                // Set animation properties (optional)
                animation.setFillAfter(true);
                imageView.startAnimation(animation);
            }

            ;

        });


        conf_button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Yes" button click
                sendRequestToGPT();
            }
        });

        conf_button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "No" button click by invoking voice recognition again
                invokeVoiceAPI();
            }
        });

    }


    private void showPromptDialog(String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.read_answer_dialog, null);


        Button yesButton = dialogView.findViewById(R.id.yes_button_dialog);
        Button noButton = dialogView.findViewById(R.id.no_button_dialog);


        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the response using the TextToSpeechManager
                TextView stop_button = findViewById(R.id.stop_button_dialog);
                stop_button.setVisibility(View.VISIBLE);
                textToSpeechManager.speak(response);
                dialog.dismiss();

            }
        });


        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do not play the response
                dialog.dismiss();
                Button voiceButton;
                voiceButton = findViewById(R.id.circularButton);
                voiceButton.setVisibility(View.VISIBLE);
            }
        });

        Button stopSpeachButton;
        stopSpeachButton = findViewById(R.id.stop_button_dialog);

        stopSpeachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeechManager.stop();
                dialog.dismiss();
                answer.setText("");
                stopSpeachButton.setVisibility(View.INVISIBLE);

                showPromptDialogNextQuestion();
                //TODO


            }

            private void showPromptDialogNextQuestion() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.next_question_prompt, null);

                Button yesButton = dialogView.findViewById(R.id.yes_button_next_qestion_prompt);
                Button noButton = dialogView.findViewById(R.id.no_button_next_qestion_prompt);

                builder.setView(dialogView);

                AlertDialog dialog = builder.create();
                dialog.show();

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        invokeVoiceAPI();
                    }
                });

                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Button voiceButton = findViewById(R.id.circularButton);
                        voiceButton.setVisibility(View.VISIBLE);
                    }
                });
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
        Button voiceButton;
        voiceButton = findViewById(R.id.circularButton);
        voiceButton.setVisibility(View.INVISIBLE);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String recognizedSpeech = matches.get(0);
                TextView greetingTextView = findViewById(R.id.welcome_text);

                // Display recognized speech

                greetingTextView.setVisibility(View.VISIBLE);
                greetingTextView.setText(recognizedSpeech + "?");

                // Show confirmation components
                TextView answer;
                answer = findViewById(R.id.answer_txt);
                answer.setVisibility(View.INVISIBLE);
                confirm_question.setVisibility(View.VISIBLE);
                conf_button_yes.setVisibility(View.VISIBLE);
                conf_button_no.setVisibility(View.VISIBLE);
            } else {
                // If no speech is recognized, dismiss confirmation components and reset UI
                resetUI();
            }
        } else {
            // If requestCode is not 100 or resultCode is not RESULT_OK, dismiss confirmation components and reset UI
            resetUI();
        }
    }

    private void resetUI() {
        // Hide confirmation components
        confirm_question.setVisibility(View.INVISIBLE);
        conf_button_yes.setVisibility(View.INVISIBLE);
        conf_button_no.setVisibility(View.INVISIBLE);

        // Reset text in welcome_text TextView
        TextView greetingTextView = findViewById(R.id.welcome_text);
        greetingTextView.setText("");

        // Show voice button
        Button voiceButton = findViewById(R.id.circularButton);
        voiceButton.setVisibility(View.VISIBLE);
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

    private void sendRequestToGPT() {
        // Get the recognized speech
        showLoadingAnimation();
        TextView greetingTextView = findViewById(R.id.welcome_text);
        String recognizedSpeech = greetingTextView.getText().toString() + "?";

        // Send the recognized speech to the GPT API
        try {
            sendSpeechToGPT(recognizedSpeech);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred while sending request to GPT", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSpeechToGPT(String speech) throws JSONException {
        // Create an instance of GPTRequestHandler
        GPTRequestHandler gptRequestHandler = new GPTRequestHandler();

        // Send a request to the GPT API with the recognized speech
        gptRequestHandler.sendGPTRequest(speech, new GPTRequestHandler.GPTResponseListener() {
            @Override
            public void onResponse(String response) {
                hideLoadingAnimation();
                // Handle the GPT API response here
                // Append the response below the conversation
                TextView greetingTextView;
                greetingTextView = findViewById(R.id.welcome_text);
                greetingTextView.setVisibility(View.INVISIBLE);

                TextView answer = findViewById(R.id.answer_txt);
                answer.setVisibility(View.VISIBLE);
                answer.setText(response);

                Button voiceButton = findViewById(R.id.circularButton);
                voiceButton.setVisibility(View.VISIBLE);

                showPromptDialog(response);

                //conversationBuilder.append(response).append("\n");
                //TextView greetingTextView = findViewById(R.id.welcome_text);


                //greetingTextView.setText(conversationBuilder.toString());

                // Hide confirmation components after response is received
                confirm_question.setVisibility(View.INVISIBLE);
                conf_button_yes.setVisibility(View.INVISIBLE);
                conf_button_no.setVisibility(View.INVISIBLE);
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

    //w8 animation
    private void showLoadingAnimation() {
        ImageView loading = findViewById(R.id.loading_layout);
        loading.setVisibility(View.VISIBLE);
    }

    private void hideLoadingAnimation() {
        ImageView loading = findViewById(R.id.loading_layout);
        loading.setVisibility(View.INVISIBLE);
    }
}
