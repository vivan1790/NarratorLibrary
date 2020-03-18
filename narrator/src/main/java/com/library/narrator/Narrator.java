package com.library.narrator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class Narrator {

    public interface NarrationListener {
        void onNarrationStarted();
        void onNarrationPaused();
        void onNarrationStopped();
        void onNarrationCompleted();
    }

    private ViewGroup contentView;
    private TextToSpeech textToSpeech;
    private ArrayList<View> narrativeViews;
    private Handler handler;
    private int numberOfNarrativeViews = 0, indexOfCurrentNarrativeView = 0,
            indexOfLastNarrativeView = -1;

    private Set<NarrationListener> listeners = new HashSet<>();

    Narrator(final ViewGroup viewGroup, final TextToSpeech tts) {
        this.contentView = viewGroup;
        this.textToSpeech = tts;
        init();
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
                                contentView.getParent().requestChildFocus(contentView,
                                        narrativeViews.get(index));
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
                    for (NarrationListener listener : listeners) {
                        listener.onNarrationCompleted();
                    }
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
            return (view.getContentDescription() == null) ?
                    "" : view.getContentDescription().toString();
        }
    }

    void reset() {
        narrativeViews.clear();
        numberOfNarrativeViews = 0;
        indexOfCurrentNarrativeView = 0;
        indexOfLastNarrativeView = -1;
    }

    void startNarration() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (indexOfLastNarrativeView == -1) {
                    obtainNarrativeViews(contentView);
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
        for (NarrationListener listener : listeners) {
            listener.onNarrationStarted();
        }
    }

    void pauseNarration() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        for (NarrationListener listener : listeners) {
            listener.onNarrationPaused();
        }
    }

    void stopNarration() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        indexOfLastNarrativeView = -1;
        showAllViews(contentView);
        for (NarrationListener listener : listeners) {
            listener.onNarrationStopped();
        }
        reset();
    }

    boolean isNarrating() {
        return textToSpeech.isSpeaking();
    }

    private void obtainNarrativeViews(ViewGroup viewGroup) {
        int numOfChildren = viewGroup.getChildCount();
        for (int i = 0; i < numOfChildren; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getVisibility() == View.VISIBLE) {
                /*if ((view instanceof ImageView) || (view instanceof TextView)  || (view instanceof View)) {
                    narrativeViews.add(view);
                    animateView(view, 1);
                    numberOfNarrativeViews++;
                } else if (view instanceof ViewGroup) {
                    obtainNarrativeViews((ViewGroup) view);
                }*/
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

    private void animateView(View view, int type) {
        // 1 = fade away, 2 = show up
        AnimatorSet anim = new AnimatorSet();
        anim.setDuration(400);
        if (type == 1) {
            anim.playTogether(ObjectAnimator.ofFloat(view, "alpha", 1, 0));
        } else if (type == 2) {
            anim.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0, 1));
        }
        anim.start();
    }

    void registerNarrationListener(NarrationListener listener) {
        listeners.add(listener);
    }

    void unRegisterNarrationListener(NarrationListener listener) {
        listeners.remove(listener);
    }
}
