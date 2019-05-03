// ClipServerAIDL.aidl
package com.example.clipserver;

// Declare any non-default types here with import statements

interface ClipServerAIDL {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void playClip(int clip_pos);

    void stopClip();

    void pauseClip();
}
