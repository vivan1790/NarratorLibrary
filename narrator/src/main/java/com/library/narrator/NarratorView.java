package com.library.narrator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NarratorView extends FrameLayout implements Narrator.NarrationListener {

    final int ORIENTATION_HORIZONTAL = 0;
    final int ORIENTATION_VERTICAL = 1;

    private LinearLayout containerLayout;
    private FloatingActionButton mFAB_Speak_Stop = null, mFAB_Play_Pause = null;
    private ViewGroup contentView;
    private Narrator narrator;
    private TextToSpeech textToSpeech;

    public NarratorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_narrator_options, this, true);
        initViews();
        initAttributes(context, attrs);
    }

    public NarratorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_narrator_options, this, true);
        initViews();
        initAttributes(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (narrator == null && contentView != null) {
                narrator = new Narrator(contentView, textToSpeech);
            }
        } else {
            if (narrator != null && narrator.isNarrating()) {
                narrator.pauseNarration();
                mFAB_Play_Pause.setImageResource(R.drawable.icon_play);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (narrator == null && contentView != null) {
            narrator = new Narrator(contentView, textToSpeech);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (narrator != null && narrator.isNarrating()) {
            narrator.stopNarration();
            mFAB_Speak_Stop.setImageResource(R.drawable.icon_speaker_white);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    /*ObjectAnimator.ofFloat(mFAB_Play_Pause, "translationX",
                            (-140), 0),*/
                    ObjectAnimator.ofFloat(mFAB_Play_Pause, "alpha", 0.8f, 0)
            );
            animatorSet.setDuration(50);
            animatorSet.start();
            narrator.unRegisterNarrationListener(this);
            narrator = null;
        }
    }

    public void attachContentViewAndDisplay(ViewGroup contentView, TextToSpeech textToSpeech) {
        this.contentView = contentView;
        this.textToSpeech = textToSpeech;
        narrator = new Narrator(contentView, textToSpeech);
        narrator.registerNarrationListener(this);
    }

    public boolean isReadyForNarration() {
        return (contentView != null && narrator != null);
    }

    private void initViews() {
        containerLayout = findViewById(R.id.container_layout);
        mFAB_Speak_Stop = findViewById(R.id.fab_speak_stop);
        mFAB_Speak_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!narrator.isNarrating() && mFAB_Play_Pause.getAlpha() == 0.8f) {
                    narrator.stopNarration();
                    ((FloatingActionButton)view)
                            .setImageResource(R.drawable.icon_speaker_white);
                    mFAB_Play_Pause.setImageResource(R.drawable.icon_pause);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(
                            /*ObjectAnimator.ofFloat(mFAB_Play_Pause, "translationX"
                                    , (-70), 0),*/
                            ObjectAnimator.ofFloat(mFAB_Play_Pause, "alpha"
                                    , 0.8f, 0)
                    );
                    animatorSet.setDuration(300);
                    animatorSet.start();
                    return;
                }
                if (narrator.isNarrating()) {
                    narrator.stopNarration();
                    ((FloatingActionButton)view)
                            .setImageResource(R.drawable.icon_speaker_white);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(
                            /*ObjectAnimator.ofFloat(mFAB_Play_Pause, "translationX"
                                    , (-70), 0),*/
                            ObjectAnimator.ofFloat(mFAB_Play_Pause, "alpha"
                                    , 0.8f, 0)
                    );
                    animatorSet.setDuration(300);
                    animatorSet.start();
                } else {
                    narrator.startNarration();
                    ((FloatingActionButton)view).setImageResource(R.drawable.icon_stop);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(
                            /*ObjectAnimator.ofFloat(mFAB_Play_Pause, "translationX"
                                    , 0, (-70)),*/
                            ObjectAnimator.ofFloat(mFAB_Play_Pause, "alpha"
                                    , 0, 0.8f)
                    );
                    animatorSet.setDuration(300);
                    animatorSet.start();
                }
            }
        });
        mFAB_Play_Pause = findViewById(R.id.fab_play_pause);
        mFAB_Play_Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (narrator.isNarrating()) {
                    narrator.pauseNarration();
                    ((FloatingActionButton)view).setImageResource(R.drawable.icon_play);
                } else {
                    narrator.startNarration();
                    ((FloatingActionButton)view).setImageResource(R.drawable.icon_pause);
                }
            }
        });
    }

    @SuppressLint("ResourceType")
    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.NarratorView);
        int orientation = values.getInt(
                R.styleable.NarratorView_orientation, ORIENTATION_HORIZONTAL);
        if (orientation == ORIENTATION_VERTICAL) {
            containerLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            containerLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
        mFAB_Play_Pause.setBackgroundTintList(ColorStateList.valueOf(
                values.getColor(R.styleable.NarratorView_color_play_pause_button,
                context.getColor(R.color.button_play_pause))));
        mFAB_Speak_Stop.setBackgroundTintList(ColorStateList.valueOf(
                values.getColor(R.styleable.NarratorView_color_speak_stop_button,
                        context.getColor(R.color.button_speak_stop))));
        /*mFAB_Play_Pause.setBackgroundColor(
                values.getColor(R.styleable.NarratorView_color_play_pause_button,
                        context.getColor(R.color.button_play_pause)));
        mFAB_Speak_Stop.setBackgroundColor(
                values.getColor(R.styleable.NarratorView_color_speak_stop_button,
                        context.getColor(R.color.button_speak_stop)));*/

        /*mFAB_Speak_Stop.setBackgroundTintList(ColorStateList.valueOf(Color
                .parseColor("#33691E")));*/
        values.recycle();
    }

    @Override
    public void onNarrationStarted() {
    }

    @Override
    public void onNarrationPaused() {
    }

    @Override
    public void onNarrationStopped() {
    }

    @Override
    public void onNarrationCompleted() {
        narrator.reset();
        mFAB_Speak_Stop.setImageResource(R.drawable.icon_speaker_white);
        mFAB_Play_Pause.setImageResource(R.drawable.icon_play);
        mFAB_Play_Pause.setAlpha(0f);
    }
}
