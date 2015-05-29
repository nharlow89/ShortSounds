package com.sloths.speedy.shortsounds.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ViewAnimator;

import com.sloths.speedy.shortsounds.*;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.view.MainActivity;

import java.util.List;

/**
 * Created by jbusc_000 on 5/15/2015.
 * System test for the main activity and system interaction with other ui elements.
 */
public class MainActivityContentTest { //extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private View mAnimator;
    private static final String TAG = "MAIN ACTIVITy TEST";

    public MainActivityContentTest() {
//     super(MainActivity.class);
    }

    /**
     * Set up required for activity tests
     * @throws Exception
     *
    @Override
    protected void setUp() throws Exception {
        Log.d(TAG, "Set-Up");
        super.setUp();
        setActivityInitialTouchMode(true);
        mMainActivity = getActivity();
        mAnimator = mMainActivity.findViewById(R.id.view_animator);
    }

    /**
     * Tests that the main activity is not null (all other tests would fail as well).
     *
    @SmallTest
    public void testMainActivityNotNull() {
        assertNotNull("Main Activity is Null", mMainActivity);
    }

    /**
     * Test to ensure that the views within the main activity are non-null.
     *
    @SmallTest
    public void testMainActivityHasContent() {

        assertNotNull("Animator null", mAnimator);
        assertNotNull("SeekBar is null", mMainActivity.findViewById(R.id.seekBar));
        assertNotNull("Record button is null", mMainActivity.findViewById(R.id.imageButtonPlay));
        assertNotNull("DrawerLayout is null", mMainActivity.findViewById(R.id.drawer_layout));
    }
*/
}
