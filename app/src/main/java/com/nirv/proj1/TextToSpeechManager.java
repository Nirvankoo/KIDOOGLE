package com.nirv.proj1;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TextToSpeechManager {

    private TextToSpeech textToSpeech;
    private Context context;

    public TextToSpeechManager(Context context) {
        this.context = context;
        initializeTextToSpeech();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // Set language for Text-to-Speech engine (optional)
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    public void speak(String text) {
        if (textToSpeech != null) {
            // Speak the text
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
