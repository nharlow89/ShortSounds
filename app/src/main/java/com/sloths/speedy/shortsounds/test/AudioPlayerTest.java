package com.sloths.speedy.shortsounds.test;

import android.test.suitebuilder.annotation.SmallTest;

import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import junit.framework.TestCase;

/**
 * Created by jbusc_000 on 5/15/2015.
 * Tests general Functionality of the Audio Player Class
 */
public class AudioPlayerTest extends TestCase {

    private static final String TAG = "AudioPlayerTest";
    private static final String TEST_TITLE = "TestTrack";
    private static final String TEST_FILE_NAME = "test-file-modified";

    /**
     * Helper method, returns constructed player. Reduces redundent calls
     */
    private AudioPlayer constructEmptyPlayer() {
        ShortSound ss = new ShortSound();
        AudioPlayer player = null;
        return new AudioPlayer(ss);
    }

    /**
     * Constructs a player with tracks
     * @return a player with tracks
     */
    private AudioPlayer constructPlayerWithTracks() {
        ShortSound ss = new ShortSound();
        ShortSoundTrack sst = newSSTrack();
        ss.addTrack(sst);
        return new AudioPlayer(ss);
    }

    /**
     * cretaes a new short sound track
     * @return the new shortsound track
     */
    private ShortSoundTrack newSSTrack() {

        ShortSoundTrack sst = new ShortSoundTrack(ShortSoundTrackTest.makeTestValues());
        return sst;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Constructor Test
    // Dependencies: ShortSound class must be implemented.
    // Tests that Audio Player creates non-null Object when constructed, and
    // that the player is in the 'STOPPED_ALL' State after construction
    ///////////////////////////////////////////////////////////////////////////
    @SmallTest
    public void testConstructor() {
        AudioPlayer player = constructEmptyPlayer();
        assertNotNull("Constructor Failure, created null audio player", player);

        if(player != null) {
            assertEquals("Constructor Failure, Audio player in incorrect state after construction",
                    AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());
        }

        AudioPlayer player2 = constructPlayerWithTracks();
        assertEquals("Constructor Failure, Incorrect number of tracks after constructing with " +
                        "premade short sound",
                1, player2.getCurrentShortSound().getTracks().size());
    }

    ///////////////////////////////////////////////////////////////////////////
    // playAllChangeState Test
    // Dependencies: testConstructor must pass, isPlayingAll() method working
    // Tests that AudioPlayer changes its state when playing starts
    ///////////////////////////////////////////////////////////////////////////
    public void testPlayAllChangesState() {

        // test that state is properly changed
        AudioPlayer player = constructEmptyPlayer();
        player.preparePlayAll(0);
        player.startPlayAll();
        assertTrue("preparePlayAll Failure, State should remain stopped if no tracks contained.",
                !player.isPlayingAll());

    }

    ///////////////////////////////////////////////////////////////////////////
    // stopAllChangeState Test
    // Dependencies: testConstructor must pass, preparePlayAll must start playing
    //Tests that AudioPlayer changes its state when stopAll is called
    ///////////////////////////////////////////////////////////////////////////
    public void testStopAllChangesState() {
        AudioPlayer player = constructEmptyPlayer();
        //Test when already stopped.
        player.stopAll();
        assertEquals("stopAll Failure, player state incorrect.",
                AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());

        //Test after playing has started
        player.preparePlayAll(0);
        player.startPlayAll();
        player.stopAll();
        assertEquals("stopAll Failure, player state incorrect.",
                AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());
    }

    ///////////////////////////////////////////////////////////////////////////
    // trackFunctionality Test
    // Dependencies: testConstructor must pass, ShortSoundTracks must work
    // Tests that tracks can be added, played, paused, and stopped
    ///////////////////////////////////////////////////////////////////////////
    public void testTrackFunctionality() {
    }

}
