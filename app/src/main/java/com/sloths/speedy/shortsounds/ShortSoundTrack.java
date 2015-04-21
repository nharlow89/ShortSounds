package com.sloths.speedy.shortsounds;

/**
 * Created by neilharlow on 4/17/15.
 */
public class ShortSoundTrack {

    public static final String AUDIO_FORMAT = "";  // TODO: format/encoding?
    public static final int TRACK_LENGTH = 30;  // Track length in seconds
    public static final int BUFFER_SIZE = 2000;  // TODO: make buffer size with respect to TRACK_LENGTH
    public static final String DEFAULT_TITLE = "Untitled Track";

    private String title;
    private String fileName = null;
    private byte[] audioBuffer = new byte[ BUFFER_SIZE ];

    ShortSoundTrack( byte[] audioBuffer ) {
        this.audioBuffer = audioBuffer;
        this.title = DEFAULT_TITLE;
        // TODO
    }

    public void addEffect() {
        // TODO
    }

    public void removeEffect() {
        // TODO
    }

    public String getTitle() {
        return title;
    }
}


/*
Record a sound.
Create ShortSoundTrack.
Add ShortSoundTrack to the ShortSound.
    Save ShortSoundTrack to disk
    Update DB to associate the ShortSoundTrack with the ShortSound
 */