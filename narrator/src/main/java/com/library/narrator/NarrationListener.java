package com.library.narrator;

public interface NarrationListener {

    void onNarrationStarted();

    void onNarrationPaused();

    void onNarrationStopped();

    void onNarrationCompleted();

}
