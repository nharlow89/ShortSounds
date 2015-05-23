package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.AudioMixer;
import com.sloths.speedy.shortsounds.model.ShortSound;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for AudioMixer class, the class that mixes tracks together
 * into a single file.
 */
public class AudioMixerTest extends TestCase {

    /*
     * Tests the constructor to verify that it creates
     * a valid AudioMixer
     */
    public void testConstructor() {
        ShortSound ss = new ShortSound();
        AudioMixer am = new AudioMixer(ss);
        assertNotNull("AudioMixer Constructor fail", am);
        assertEquals("AudioMixer constructor made wrong" +
                "ShortSound", ss, am.getShortSound());
    }

    /*
     * Tests the generateAudioFile method from AudioMixer
     * to make sure it generates a file properly.
     */
    public void testGenerateAudioFile() {
        ShortSound ss = new ShortSound();
        AudioMixer am = new AudioMixer(ss);
        File collection;
        try {
            collection = am.generateAudioFile();
        } catch (IOException e) {
            collection = null;
        }
        assertNotNull("GenerateAudioFile fail", collection);
    }
}
