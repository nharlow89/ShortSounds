package com.sloths.speedy.shortsounds;

import android.media.AudioRecord;
import android.test.AndroidTestCase;

import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Created by Justin Yoon on 5/15/2015.
 */
public class AudioRecorderTest extends AndroidTestCase {

    private AudioRecorder testObject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testObject = new AudioRecorder(null);
    }
/*
    @Override
    public void tearDown() {

    }*/

/*
 * Constructor
 */

    public void testConstructorMakesSufficientBuffer() {
        assert(testObject.BUFFER_SIZE >= AudioRecord.getMinBufferSize(ShortSoundTrack.SAMPLE_RATE,
                                                                      testObject.CHANNEL_CONFIG,
                                                                      ShortSoundTrack.AUDIO_FORMAT));
    }

/*
 * start
 */

    public void testStart() {

    }
}
