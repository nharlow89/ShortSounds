package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.*;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioRecorder;

import junit.framework.TestCase;

import java.io.File;

/**
 * Created by jbusc_000 on 5/15/2015.
 */
public class AudioRecorderTest  extends TestCase {

    public void testConstructor() {
        AudioRecorder ar = new AudioRecorder(null);
        assertNotNull(ar);
    }
}
