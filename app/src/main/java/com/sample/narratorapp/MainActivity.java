package com.sample.narratorapp;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.library.narrator.Narrator;

public class MainActivity extends AppCompatActivity {

    private ViewGroup contentView;
    private FrameLayout narratorContainer;
    private Narrator narrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = findViewById(R.id.content_layout);
        narratorContainer = findViewById(R.id.narrator_container);
        narrator = new Narrator(contentView, narratorContainer);
    }
}
