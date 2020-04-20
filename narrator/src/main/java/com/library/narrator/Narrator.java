package com.library.narrator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class Narrator {

    private ViewGroup readableContentView;
    private NarratorView narratorView;
    private TextToSpeech textToSpeech;
    private ArrayList<View> narrativeViews;
    private Handler handler;
    private int numberOfNarrativeViews = 0, indexOfCurrentNarrativeView = 0,
            indexOfLastNarrativeView = -1;
    private NarrationListener narrationListener;
    private boolean isNarrationPaused = false;
    private String imageDescription;

    public Narrator(@NonNull final ViewGroup readableContentView,
                    @NonNull final ViewGroup narrationControlsParent) {
        this.readableContentView = readableContentView;
        initTextToSpeech(narrationControlsParent.getContext());
        narratorView = new NarratorView(narrationControlsParent.getContext(), this);
        narrationControlsParent.addView(narratorView);
        init();
    }

    public void finish() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    private void initTextToSpeech(Context context) {
        int pitchValue = 100, speechValue = 100;
        try {
            pitchValue = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.TTS_DEFAULT_PITCH);
            speechValue = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.TTS_DEFAULT_RATE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        final float pitchRate = pitchValue / 100f;
        final float speechRate = speechValue / 100f;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (textToSpeech != null && status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setPitch(pitchRate);
                    textToSpeech.setSpeechRate(speechRate);
                }
            }
        });
    }

    private void init() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utterance_id) {
                if (utterance_id.charAt(0) == 'S' || utterance_id.charAt(0) == 'U') {
                    indexOfLastNarrativeView = Integer.parseInt(utterance_id.substring(1));
                    if (utterance_id.charAt(0) == 'U') {
                        final int index = Integer.parseInt(utterance_id.substring(1));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                readableContentView.getParent().requestChildFocus(
                                        readableContentView, narrativeViews.get(index));
                            }
                        });
                    }
                }
            }

            @Override
            public void onDone(String utterance_id) {
                if (utterance_id.charAt(0) == 'S') {
                    int index = Integer.parseInt(utterance_id.substring(1)) + 1;
                    //animateView(narrativeViews.get(index), 2);
                    if (index  < numberOfNarrativeViews) {
                        narrativeViews.get(index).setAlpha(1);
                        //animateView(narrativeViews.get(index), 2);
                    }
                }
                if (indexOfLastNarrativeView == numberOfNarrativeViews - 1 && !isNarrating()) {
                    if (narrationListener != null) {
                        narrationListener.onNarrationCompleted();
                    }
                    reset();
                }
            }

            @Override
            public void onError(String utterance_id) {

            }
        });
        narrativeViews = new ArrayList<>();
        handler = new Handler();
    }

    private String getNarrationText(View view) {
        if (view instanceof TextView) {
            return ((TextView) view).getText().toString();
        } else {
            if (imageDescription != null) {
                return imageDescription;
            }
            return (view.getContentDescription() == null) ?
                    "" : view.getContentDescription().toString();
        }
    }

    private void reset() {
        narrativeViews.clear();
        numberOfNarrativeViews = 0;
        indexOfCurrentNarrativeView = 0;
        indexOfLastNarrativeView = -1;
        narratorView.reset();
    }

    public void startNarration() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (indexOfLastNarrativeView == -1) {
                    obtainNarrativeViews(readableContentView);
                    if (numberOfNarrativeViews > 0) {
                        animateView(narrativeViews.get(0), 2);
                    }
                    indexOfCurrentNarrativeView = 0;
                } else {
                    indexOfCurrentNarrativeView = indexOfLastNarrativeView;
                }
                if (!textToSpeech.isSpeaking()) {
                    for (; indexOfCurrentNarrativeView < numberOfNarrativeViews;
                         indexOfCurrentNarrativeView++) {
                        textToSpeech.speak(
                                getNarrationText(narrativeViews.get(indexOfCurrentNarrativeView)),
                                TextToSpeech.QUEUE_ADD, null,
                                "U" + indexOfCurrentNarrativeView);
                        textToSpeech.playSilentUtterance(600, TextToSpeech.QUEUE_ADD,
                                "S" + indexOfCurrentNarrativeView);
                    }
                }
            }
        }, 500);
        isNarrationPaused = false;
        if (narrationListener != null) {
            narrationListener.onNarrationStarted();
        }
    }

    public void pauseNarration() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        isNarrationPaused = true;
        if (narrationListener != null) {
            narrationListener.onNarrationPaused();
        }
    }

    public void stopNarration() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        indexOfLastNarrativeView = -1;
        showAllViews(readableContentView);
        isNarrationPaused = false;
        if (narrationListener != null) {
            narrationListener.onNarrationStopped();
        }
        reset();
    }

    public boolean isNarrating() {
        return textToSpeech.isSpeaking();
    }

    boolean isNarrationPaused() {
        return isNarrationPaused;
    }

    private void obtainNarrativeViews(ViewGroup viewGroup) {
        int numOfChildren = viewGroup.getChildCount();
        for (int i = 0; i < numOfChildren; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getVisibility() == View.VISIBLE) {
                if (view instanceof ViewGroup) {
                    obtainNarrativeViews((ViewGroup) view);
                } else {
                    narrativeViews.add(view);
                    animateView(view, 1);
                    numberOfNarrativeViews++;
                }
            }
        }
    }

    private void showAllViews(ViewGroup viewGroup) {
        int numOfChildren = viewGroup.getChildCount();
        for (int i = 0; i < numOfChildren; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                showAllViews((ViewGroup) view);
            } else {
                if (view.getAlpha() == 0) {
                    animateView(view, 2);
                }
            }
        }
    }

    private void animateView(View view, int type) {   // 1 = fade away, 2 = show up
        AnimatorSet anim = new AnimatorSet();
        anim.setDuration(400);
        if (type == 1) {
            anim.playTogether(ObjectAnimator.ofFloat(view, "alpha", 1, 0));
        } else if (type == 2) {
            anim.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0, 1));
        }
        anim.start();
    }

    public void setNarrationListener(NarrationListener narrationListener) {
        this.narrationListener = narrationListener;
    }
}