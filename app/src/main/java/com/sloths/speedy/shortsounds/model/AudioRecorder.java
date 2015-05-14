package com.sloths.speedy.shortsounds.model;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * The AudioRecorder class is a wrapper around Androids MediaRecorder class.
 */
public class AudioRecorder {
    public static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.AAC_ADTS;
    public static final int AUDIO_ENCODING = MediaRecorder.AudioEncoder.AAC;

    private MediaRecorder mTrackRecorder;
    private File mTempAudioFile;
    private String mTempFileName;
    private FileOutputStream mOutputStream;
    private boolean mIsRecording;
    private File mCacheDir;

    public AudioRecorder( File cacheDir ) {
        mCacheDir = cacheDir;
        this.setup();
    }

    public void start() {
        try {
            mTrackRecorder.prepare();
            mTrackRecorder.start();
            mIsRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File end() {
        if ( mTrackRecorder != null && mIsRecording ) {
            Log.d("DEBUG", "Attempt to stop recording!");
            mTrackRecorder.stop();
            mTrackRecorder.reset();
            mTrackRecorder.release();
            mTrackRecorder = null;
            mIsRecording = false;
        }
        return mTempAudioFile;
    }

    /**
     * Reset the AudioRecorder. This method needs to be called after each recording.
     */
    public void reset() {
        // TODO: cleanup resources from previous recording? temp file?
        setup();
    }

    public boolean isRecording() {
        return this.mIsRecording;
    }

    private void setup() {
        try {
            mTempFileName = "temp-recording-file-" + UUID.randomUUID().toString().substring(0, 4);
            mTempAudioFile = new File( mCacheDir, mTempFileName);
            mOutputStream = new FileOutputStream( mTempAudioFile );
            mTrackRecorder = new MediaRecorder();
            mTrackRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mTrackRecorder.setOutputFormat(OUTPUT_FORMAT);
            mTrackRecorder.setOutputFile(mOutputStream.getFD());
            mTrackRecorder.setAudioEncoder(AUDIO_ENCODING);
            mIsRecording = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
