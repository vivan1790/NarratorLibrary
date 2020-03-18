package com.sample.narratorapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.library.narrator.NarratorView;
import com.library.narrator.tts.TTSBuilder;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewGroup contentView;
    private NarratorView narratorView;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = findViewById(R.id.content_layout);
        narratorView = findViewById(R.id.narrator_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!narratorView.isReadyForNarration()) {
            textToSpeech = new TTSBuilder(this)
                    .withLocale(Locale.getDefault())
                    .build();
            narratorView.attachContentViewAndDisplay(contentView, textToSpeech);
        }
    }
}
