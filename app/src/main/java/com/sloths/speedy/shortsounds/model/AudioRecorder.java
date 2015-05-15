package com.sloths.speedy.shortsounds.model;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * The AudioRecorder class is a wrapper around Androids AudioRecord class.
 */
public class AudioRecorder {
    // global vars
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int SAMPLE_RATE_IN_HZ = 44100;
    public static final int CHANNEL_CONFIG =  AudioFormat.CHANNEL_IN_STEREO;
    public static final int BUFFER_ELEMENTS_TO_REC = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    public static final int BYTES_PER_ELEMENT = 2; // 2 bytes in 16bit format
    // instance vars
    private AudioRecord mTrackRecorder;
    private File mTempAudioFile;
    private String mTempFileName;
    private FileOutputStream mOutputStream;
    private boolean mIsRecording;
    private File mCacheDir;
    private Thread mRecordingThread = null;

    /**
     * Constructor for an AudioRecorder
     * @param cacheDir File to be used for audio
     */
    public AudioRecorder( File cacheDir ) {
        mCacheDir = cacheDir;
        this.setup();
    }

    /**
     * Performs setup for this AudioRecorder. Primarily sets up mTrackRecorder.
     */
    private void setup() {
        mTrackRecorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG,
                AUDIO_FORMAT, BUFFER_ELEMENTS_TO_REC * BYTES_PER_ELEMENT);
        int current_state = mTrackRecorder.getState();
        assert(current_state == AudioRecord.STATE_INITIALIZED);
        mIsRecording = false;
    }

    /**
     * Starts the recording process
     */
    public void start() {
        mTrackRecorder.startRecording();
        mIsRecording = true;
        mRecordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        mRecordingThread.start();
    }

    /**
     * writes the audio data to mTempAudioFile
     */
    private void writeAudioDataToFile() {
        // Write the output audio in byte
        short sData[] = new short[BUFFER_ELEMENTS_TO_REC];
        try {
            mTempFileName = "temp-recording-file-" + UUID.randomUUID().toString().substring(0, 4);
            mTempAudioFile = new File( mCacheDir, mTempFileName);
            mOutputStream = new FileOutputStream( mTempAudioFile );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording()) {
            // gets the voice output from microphone to byte format
            mTrackRecorder.read(sData, 0, BUFFER_ELEMENTS_TO_REC);
            System.out.println("Short writing to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                mOutputStream.write(bData, 0, BUFFER_ELEMENTS_TO_REC * BYTES_PER_ELEMENT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ends the recording process
     * @return File the file holding the audio
     */
    public File end() {
        if ( mTrackRecorder != null && mIsRecording ) {
            Log.d("DEBUG", "Attempt to stop recording!");
            // stops the recording activity
            mTrackRecorder.stop();
            mTrackRecorder.release();
            mTrackRecorder = null;
            mRecordingThread = null;
            mIsRecording = false;
        }
        return mTempAudioFile;
    }

    /**
     * returns True if this AudioRecorder is recording, false else
     * @return boolean true if this AudioRecorder is recording, false else
     */
    public boolean isRecording() {
        return this.mIsRecording;
    }

    /**
     * Converts a short to a byte
     * @param sData the short to be converted
     * @return byte array of converted bytes
     */
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    /**
     * Reset the AudioRecorder. This method needs to be called after each recording.
     */
    public void reset() {
        // TODO: cleanup resources from previous recording? temp file?
        setup();
    }
}
