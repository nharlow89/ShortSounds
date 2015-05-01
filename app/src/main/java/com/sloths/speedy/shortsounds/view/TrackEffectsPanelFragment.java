package com.sloths.speedy.shortsounds.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Created by shampson on 4/27/15.
 *
 * This fragment represents the logic/view for the effects panel after
 * clicking on a ShortSoundTrack. The population of effects occurs in the Adapter.
 *
 * ================================== NOTE ==================================
 * This class is not used.  Instead we are using setVisibility() to show the
 * track information (effects, play button, & volume control).  We are keeping
 * it here if we need to populate this area in the future with a fragment instead
 * ================================== NOTE ==================================
 */
public class TrackEffectsPanelFragment extends Fragment {
    public static final String ARG_SOUND_NUMBER = "track_number";
    private ShortSoundTrack track;

    public TrackEffectsPanelFragment() {
        // Default constructor (required)
    }

    // Creates the view to put in to the card View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }
}
