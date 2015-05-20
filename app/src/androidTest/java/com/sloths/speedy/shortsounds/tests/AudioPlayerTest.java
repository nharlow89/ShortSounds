package com.sloths.speedy.shortsounds.tests;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.ShortSound;

import junit.framework.TestCase;

/**
 * Created by jbusc_000 on 5/15/2015.
 * Tests general Functionality of the Audio Player Class
 */
public class AudioPlayerTest extends TestCase {

    private String TAG = "AudioPlayerTest";

    /*
    Helper method, returns constructed player. Reduces redundent calls
     */
    private AudioPlayer constructPlayer() {
        ShortSound ss = new ShortSound();
        AudioPlayer player = null;
        return new AudioPlayer(ss);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructor Test
    // Dependencies: ShortSound class must be implemented.
    // Tests that Audio Player creates non-null Object when constructed, and
    // that the player is in the 'STOPPED_ALL' State after construction
    ///////////////////////////////////////////////////////////////////////////
    @SmallTest
    public void testConstructor() {
        AudioPlayer player = constructPlayer();
        assertNotNull("Constructor Failure, created null audio player", player);

        if(player != null) {
            assertEquals("Constructor Failure, Audio player in incorrect state after construction",
                    AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // playAllChangeState Test
    // Dependencies: testConstructor must pass, isPlayingAll() method working
    // Tests that AudioPlayer changes its state when playing starts
    ///////////////////////////////////////////////////////////////////////////
    public void testPlayAllChangesState() {

        // test that state is properly changed
        AudioPlayer player = constructPlayer();
        player.playAll(0);
        assertTrue("playAll Failure, not changing players state.",
                 player.isPlayingAll());

    }

    ///////////////////////////////////////////////////////////////////////////
    // stopAllChangeState Test
    // Dependencies: testConstructor must pass, playAll must start playing
    //Tests that AudioPlayer changes its state when stopAll is called
    ///////////////////////////////////////////////////////////////////////////
    public void testStopAllChangesState() {
        AudioPlayer player = constructPlayer();
        //Test when already stopped.
        player.stopAll();
        assertEquals("stopAll Failure, player state incorrect.",
                AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());

        //Test after playing has started
        player.playAll(0);
        player.stopAll();
        assertEquals("stopAll Failure, player state incorrect.",
                AudioPlayer.PlayerState.STOPPED_ALL, player.getPlayerState());
    }
}
