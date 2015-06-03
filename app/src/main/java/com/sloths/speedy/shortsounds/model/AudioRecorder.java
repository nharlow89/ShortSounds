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
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int CHANNEL_CONFIG =  AudioFormat.CHANNEL_IN_STEREO;
    public static final int BUFFER_ELEMENTS_TO_REC = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    public static final int GAIN_LEVEL = 4;
    public static int BUFFER_SIZE;
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
        repInvariant();
    }

    /**
     * Performs setup for this AudioRecorder. Primarily sets up mTrackRecorder.
     */
    private void setup() {
        BUFFER_SIZE = AudioRecord.getMinBufferSize(ShortSoundTrack.SAMPLE_RATE, CHANNEL_CONFIG, ShortSoundTrack.AUDIO_FORMAT);
        mTrackRecorder = new AudioRecord(AUDIO_SOURCE, ShortSoundTrack.SAMPLE_RATE, CHANNEL_CONFIG,
                ShortSoundTrack.AUDIO_FORMAT, BUFFER_SIZE);
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
        byte frameBuffer[] = new byte[BUFFER_ELEMENTS_TO_REC];
        try {
            mTempFileName = "temp-recording-file-" + UUID.randomUUID().toString().substring(0, 4);
            mTempAudioFile = new File( mCacheDir, mTempFileName);
            mOutputStream = new FileOutputStream( mTempAudioFile );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording()) {
            // gets the voice output from microphone to byte format
            int bytesRead = mTrackRecorder.read(frameBuffer, 0, BUFFER_ELEMENTS_TO_REC);
//            System.out.println("Short writing to file" + frameBuffer.toString());
            try {
                // Put de gains
                int i = 0;
                while ( i < bytesRead ) {
                    float sample = (float)( frameBuffer[ i ] & 0xFF
                            | frameBuffer[ i + 1 ] << 8 );
                    // THIS is the point were the work is done:
                    // Increase level by about 6dB:
                    // sample *= 2;
                    // Or increase level by 20dB:
                    sample *= GAIN_LEVEL;
                    // Or if you prefer any dB value, then calculate the gain factor outside the loop
                    // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
                    // sample *= gainFactor;

                    // Avoid 16-bit-integer overflow when writing back the manipulated data:
                    if ( sample >= 32767f ) {
                        frameBuffer[ i ] = (byte)0xFF;
                        frameBuffer[ i + 1 ] = 0x7F;
                    } else if ( sample <= -32768f ) {
                        frameBuffer[ i ] = 0x00;
                        frameBuffer[ i + 1 ] = (byte)0x80;
                    } else {
                        int s = (int)( 0.5f + sample );  // Here, dithering would be more appropriate
                        frameBuffer[ i ] = (byte)(s & 0xFF);
                        frameBuffer[ i + 1 ] = (byte)(s >> 8 & 0xFF);
                    }
                    i += 2;
                }
                mOutputStream.write(frameBuffer, 0, BUFFER_ELEMENTS_TO_REC);
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
     * Reset the AudioRecorder. This method needs to be called after each recording.
     */
    public void reset() {
        setup();
    }

    /**
     * Represenation invaraiant for AudioRecorder
     */
    private void repInvariant() {
        int current_state = mTrackRecorder.getState();
        assert(current_state == AudioRecord.STATE_INITIALIZED);
        assert(BUFFER_SIZE >= AudioRecord.getMinBufferSize(ShortSoundTrack.SAMPLE_RATE,
                                                           CHANNEL_CONFIG,
                                                           ShortSoundTrack.AUDIO_FORMAT));
    }
}
