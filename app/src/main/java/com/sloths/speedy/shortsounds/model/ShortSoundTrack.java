package com.sloths.speedy.shortsounds.model;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.sloths.speedy.shortsounds.view.ShortSoundsApplication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * This class represents the model of a ShortSoundTrack. A ShortSoundTrack essentially
 * keeps track of an audio file along with any effects that may have been applied to that
 * file. A ShortSoundTrack should belong to a single ShortSound at any given time.
 */
public class ShortSoundTrack {
    public static final String DEBUG_TAG = "SHORT_SOUNDS";
    // AudioTrack Params
    public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    public static final int SAMPLE_RATE = 44100;  // NOTE: also used for buffer size
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MODE = AudioTrack.MODE_STREAM;
    public static int BUFFER_SIZE = 44100; // Default


    public static final String DEFAULT_TITLE = "Untitled Track";
    private static final String TAG = "Track";
    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();
    private static final Context context = ShortSoundsApplication.getAppContext();
    private static final String STORAGE_PATH = context.getFilesDir().getAbsolutePath();
    private final String originalFile;
    private final String file;
    private InputStream audioInputStream;
    private long id;
    private String title;
    private final long parentId;
    private AudioTrack audioTrack;
    private Thread audioThread;
    private byte[] audioFrame;
    private TrackState trackState;
    private enum TrackState{ PLAYING, PAUSED, STOPPED };
    private OnCompleteListener onCompleteListener;


    /**
     * Create a ShortSoundTrack provided an existing audio file.
     * @param audioFile The recorded audio file.
     * @param shortSoundId The id of the ShortSound that this track belongs to.
     * @postcondition This ShortSoundTrack will be stored in the database and a
     *      copy of the file referenced by filename will be made.
     */
    public ShortSoundTrack( File audioFile, long shortSoundId ) {
        this.title = DEFAULT_TITLE;
        this.parentId = shortSoundId;
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
        this.originalFile = "ss" + shortSoundId + "-track" + id;
        this.file = originalFile + "-modified";
        this.sqlHelper.updateShortSoundTrack(this);  // Had to update with filenames =(
        initFiles(audioFile);
        setupAudioTrack();
    }

    /**
     * Construct a ShortSoundTrack from data stored in the DB.
     * @param map Key-value pairs corresponding to the fields that make up a ShortSoundTrack.
     * @throws AssertionError if an expected key is not found.
     */
    public ShortSoundTrack( HashMap<String, String> map ) {
        if ( !map.containsKey( sqlHelper.KEY_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_ID + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TRACK_FILENAME_ORIGINAL ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TRACK_FILENAME_ORIGINAL + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TRACK_FILENAME_MODIFIED ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TRACK_FILENAME_MODIFIED + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TITLE ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TITLE + " field.");
        if ( !map.containsKey( sqlHelper.KEY_SHORT_SOUND_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_SHORT_SOUND_ID + " field.");
        this.id = Long.parseLong(map.get(sqlHelper.KEY_ID));
        this.file = map.get( sqlHelper.KEY_TRACK_FILENAME_MODIFIED );
        this.originalFile = map.get(sqlHelper.KEY_TRACK_FILENAME_ORIGINAL);
        this.title = map.get( sqlHelper.KEY_TITLE );
        this.parentId = Long.parseLong( map.get( sqlHelper.KEY_SHORT_SOUND_ID ) );
        setupAudioTrack();
    }

    /**
     * Sets up the AudioTrack for this ShortSoundTrack. This method needs to be called prior to
     * any audio interaction (stop, play, etc..).
     */
    public void setupAudioTrack() {
        BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE, MODE);
        audioFrame = new byte[BUFFER_SIZE * 2];
        trackState = TrackState.STOPPED;
    }

    /**
     * Play the audio track associated with this ShortSound.
     */
    public void play() {
        // If the track is Stopped then we need to reset the input stream so the AudioTrack starts
        // from the beginning again.
        if ( trackState == TrackState.STOPPED ) {
            setInputStream();
        }
        if ( audioThread != null ) {
            audioThread.interrupt();
        }
        trackState = TrackState.PLAYING;
        AudioTask task = new AudioTask();
        audioThread = new Thread(task);
        audioThread.start();
        Log.d(DEBUG_TAG, "Play track ["+id+"] from new thread.");
    }

    private class AudioTask implements Runnable {
        @Override
        public void run() {
            try {
                Log.d(DEBUG_TAG, "Running thread to play ShortSoundTrack["+id+"]");
                audioTrack.play();
                int bytesRead;
                while( (bytesRead = audioInputStream.read( audioFrame ) ) != -1 && trackState == TrackState.PLAYING ) {
                    // NOTE: this is blocking, so the next frame will not be loaded until ready.
                    // Look at AudioTrack docs for more info.
                    Log.d(DEBUG_TAG, "Writing ["+bytesRead+"] bytes to audioTrack. PlaybackPosition["+audioTrack.getPlaybackHeadPosition()+"]");
                    audioTrack.write( audioFrame, 0, bytesRead );
                }
                if ( trackState == TrackState.PLAYING ) {
                    // We reached the end of the track
                    Log.d(DEBUG_TAG, "Reached end of track["+id+"]");
                    audioTrack.flush();  // Clear the playback buffer and set Playback position to 0
                    trackState = TrackState.STOPPED;
                    audioInputStream.close();
                    onCompleteListener.onComplete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setInputStream() {
        if ( audioInputStream != null ) {
            try {
                audioInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream( new File(STORAGE_PATH, file) );
            audioInputStream = new DataInputStream( fileInputStream );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop playing this track and reset its position to the beginning of the audio file.
     */
    public void stop() {
        // TODO
    }

    /**
     * Pause this track.
     */
    public void pause() {
        if ( trackState == TrackState.PLAYING ) {
            Log.d(DEBUG_TAG, "Pause track [" + id + "]");
            audioTrack.pause();
            trackState = TrackState.PAUSED;
        } else {
            Log.e(DEBUG_TAG, "Tried to pause track ["+id+"] in invalid state ["+trackState+"]");
        }
    }

    /**
     * Set the onCompletionListener for this ShortSoundTrack.
     * @param listener
     */
    public void setOnPlayCompleteListener( OnCompleteListener listener ) {
        this.onCompleteListener = listener;
    }

    public void addEffect() {
        // TODO
    }

    public void removeEffect() {
        // TODO
    }

    /**
     * Remove the ShortSoundTrack from the database and delete the corresponding
     * files.
     * @postcondition This track will no longer exist in the database, and its audio files
     *      will be removed.
     */
    public void delete() {
        // TODO: cleanup any resources (audio players and stuff)
        sqlHelper.removeShortSoundTrack( this );
        deleteFiles();
    }

    /**
     * Initialize the files for a new ShortSoundTrack. This makes a copy of the original audio file
     * into the proper location.
     * @param audioFile
     */
    private void initFiles( File audioFile ) {
        File originalFile = new File( STORAGE_PATH, this.originalFile );
        File file = new File( STORAGE_PATH , this.file );
        try {
            copyFile( audioFile, originalFile );
            copyFile( audioFile, file );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove this ShortSoundTrack's files from memory (both the original and
     * any modified)
     */
    private void deleteFiles() {
        File originalFile = new File( this.originalFile );
        if( originalFile.exists() ) {
            originalFile.delete();
        }
        File file = new File( this.file );
        if( file.exists() ) {
            file.delete();
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        Log.d("DEBUG", "Copy file [" + src.getPath() + "] to ["+ dst.getPath() +"]");
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Get the title of this ShortSoundTrack.
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the title of this ShortSoundTrack
     * @return String
     */
    @Override
    public String toString() {
        return this.title;
    }

    /**
     * Get the parentId (ShortSound id) that this track is associated with.
     * @return long
     */
    public long getParentId() {
        return this.parentId;
    }

    /**
     * Get the filename associated with this track.
     * @return filename
     */
    public String getFile() { return this.file; }

    /**
     * Get this tracks id.
     * @return id
     */
    public long getId() { return this.id; }

    public interface OnCompleteListener {
        public void onComplete();
    }

    /**
     * This is the representation invarient of the ShortSoundTrack model.
     * The main thing here is that a ShortSoundTrack becomes invalid if the files
     * associated with the tracks are non-existent.
     */
    private void repInvariant() {
        if ( this.title == null || !(this.title instanceof String) ) throw new AssertionError("Invalid title");
        if ( this.file == null || !(this.file instanceof String) ) throw new AssertionError("Invalid filename");
        if ( this.originalFile == null || !(this.originalFile instanceof String) ) throw new AssertionError("Invalid filename");
        if ( this.id < 1 ) throw new AssertionError("Invalid id: " + this.id);
        // Check that the files are on disk
        File originalFile = new File( this.originalFile );
        if ( !originalFile.exists() ) throw new AssertionError("File does not exist: " + originalFile);
        File file = new File( this.file );
        if ( !file.exists() ) throw new AssertionError("File does not exist: " + file);
    }

    public String getOriginalFile() {
        return originalFile;
    }
}
