package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.*;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioRecorder;

import junit.framework.TestCase;

import java.io.File;

/**
 * Tests the AudioRecorder class
 * @author John Buscher
 */
public class AudioRecorderTest  extends TestCase {

    public void testConstructor() {
        AudioRecorder ar = new AudioRecorder(null);
        assertNotNull(ar);
    }
}
