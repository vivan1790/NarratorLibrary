package com.library.narrator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

@SuppressLint("ViewConstructor")
class NarratorView extends FrameLayout {

    private Narrator narrator;
    private FloatingActionButton buttonStartStop = null, buttonPlayPause = null;

    public NarratorView(@NonNull Context context, @NonNull Narrator narrator) {
        super(context);
        this.narrator = narrator;
        LayoutInflater.from(getContext()).inflate(R.layout.layout_narrator_options, this, true);
        initViews();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            if (narrator != null && narrator.isNarrating()) {
                narrator.pauseNarration();
                buttonPlayPause.setImageResource(R.drawable.icon_play);
            }
        }
    }

    public void reset() {
        buttonStartStop.setImageResource(R.drawable.icon_speaker_white);
        buttonPlayPause.setVisibility(View.GONE);
        buttonPlayPause.setImageResource(R.drawable.icon_pause);
    }

    private void initViews() {
        buttonStartStop = findViewById(R.id.button_start_stop);
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (narrator.isNarrating() || narrator.isNarrationPaused()) {
                    narrator.stopNarration();
                    buttonStartStop.setImageResource(R.drawable.icon_speaker_white);
                    buttonPlayPause.setVisibility(View.GONE);
                } else {
                    buttonPlayPause.setVisibility(View.VISIBLE);
                    narrator.startNarration();
                    buttonStartStop.setImageResource(R.drawable.icon_stop);
                }
            }
        });
        buttonPlayPause = findViewById(R.id.button_play_pause);
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (narrator.isNarrating()) {
                    narrator.pauseNarration();
                    buttonPlayPause.setImageResource(R.drawable.icon_play);
                } else {
                    narrator.startNarration();
                    buttonPlayPause.setImageResource(R.drawable.icon_pause);
                }
            }
        });
    }
}