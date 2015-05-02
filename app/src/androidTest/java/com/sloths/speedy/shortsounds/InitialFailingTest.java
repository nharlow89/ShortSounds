package com.sloths.speedy.shortsounds;

/**
 * Created by joel on 4/29/2015.
 */

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class InitialFailingTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Creating a failing test in order to satisfy the zero-feature requirement
     * that we have a continuous integration builder server set up that can send
     * emails to team members when a build fails.
     */
    @Test
    public void initialFailingTestForJenkins() {
        assertTrue(false);
    }

}
