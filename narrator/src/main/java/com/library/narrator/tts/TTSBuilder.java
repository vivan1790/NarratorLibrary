package com.library.narrator.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.Locale;

public class TTSBuilder {
    private Context context;
    private TextToSpeech textToSpeech;
    private Locale locale = Locale.getDefault();
    private float speechRate = 1.0f;
    private float pitchRate = 1.0f;
    private String voice;

    public TTSBuilder(Context context) {
        this.context = context;
    }

    public TTSBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public TTSBuilder withSpeechRate(float speechRate) {
        this.speechRate = speechRate;
        return this;
    }

    public TTSBuilder withPitchRate(float pitchRate) {
        this.pitchRate = pitchRate;
        return this;
    }

    public TTSBuilder withVoice(String voice) {
        this.voice = voice;
        return this;
    }

    public TextToSpeech build() {
       textToSpeech = new TextToSpeech(context,
                new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (textToSpeech != null && status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(locale);
                    textToSpeech.setSpeechRate(speechRate);
                    textToSpeech.setPitch(pitchRate);
                    try {
                        if (voice != null && !voice.isEmpty()) {
                            for (Voice tmpVoice : textToSpeech.getVoices()) {
                                if (tmpVoice.getName().equalsIgnoreCase(voice)) {
                                    textToSpeech.setVoice(tmpVoice);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return textToSpeech;
    }
}
