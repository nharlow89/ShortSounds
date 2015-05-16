package com.sloths.speedy.shortsounds.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ViewAnimator;

import com.sloths.speedy.shortsounds.*;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.view.MainActivity;

/**
 * Created by jbusc_000 on 5/15/2015.
 * System test for the main activity and system interaction with other ui elements.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    MainActivity mMainActivity;
    View mAnimator;
    public MainActivityTest() {
        super(MainActivity.class);
    }

    /**
     * Set up required for activity tests
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivity = getActivity();
        mAnimator = mMainActivity.findViewById(R.id.view_animator);
    }

    /**
     * Tests that the main activity is not null (all other tests would fail as well).
     */
    @SmallTest
    public void testMainActivityNotNull() {
        assertNotNull("Main Activity is Null", mMainActivity);
    }

    /**
     * Test to insure that the views within the main activity are non-null.
     */
    @SmallTest
    public void testMainActivityHasContent() {
        assertNotNull("Animator is non-null", mAnimator);
    }
}
