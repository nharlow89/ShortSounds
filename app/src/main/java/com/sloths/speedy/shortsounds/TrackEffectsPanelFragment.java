package com.sloths.speedy.shortsounds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shampson on 4/27/15.
 *
 * This fragment represents the logic/view for the effects panel after
 * clicking on a ShortSoundTrack. The population of effects occurs in the Adapter.
 */
public class TrackEffectsPanelFragment extends Fragment {
    public static final String ARG_SOUND_NUMBER = "track_number";
    private ShortSoundTrack track;

    public TrackEffectsPanelFragment() {
        // Default constructore (required)
    }

//    public TrackEffectsPanelFragment(ShortSoundTrack track) {
//        this.track = track;
//    }

    // Creates the view to put in to the card View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.track_control_view, container, false);
        // Set title
        getActivity().setTitle(track.getTitle());
        // Populate array of effects
        // TODO: This will be updated based upon track, but for now it is static
        List<String> effects = Arrays.asList(getResources().getStringArray(R.array.effects_array));

        // TODO: Figure out how to modify list of effects view (inside rootView)
        // TODO: Add adapter that contains effects to ^ list
        // TODO: Set listener for a click on the list (this will display a specfici effect)
        return rootView;
    }
}
