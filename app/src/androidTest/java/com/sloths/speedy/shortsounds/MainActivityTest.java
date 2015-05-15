package com.sloths.speedy.shortsounds;

import android.app.Activity;
        import android.support.test.InstrumentationRegistry;
        import android.test.ActivityInstrumentationTestCase2;
        import android.support.test.runner.AndroidJUnit4;
        import android.support.test.runner.AndroidJUnitRunner;


        import com.sloths.speedy.shortsounds.view.MainActivity;
        import com.sloths.speedy.shortsounds.R;

        import android.app.Activity;

        import android.support.test.rule.ActivityTestRule;
        import android.support.test.runner.AndroidJUnit4;
        import android.test.ActivityInstrumentationTestCase2;
        import android.test.suitebuilder.annotation.LargeTest;
import android.widget.TextView;


import org.junit.After;
        import org.junit.Before;
        import org.junit.Test;
        import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mFirstTestActivity;
    private TextView mFirstTestText;

    public MainActivityTest () {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFirstTestActivity = this.getActivity();
        //mFirstTestText =
                //(TextView) mFirstTestActivity.findViewById(R.id.my_first_test_text_view);
    }


}