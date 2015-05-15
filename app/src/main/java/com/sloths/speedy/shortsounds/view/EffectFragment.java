package com.sloths.speedy.shortsounds.view;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
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

public class EffectFragment extends Fragment {
    public static final String TAG = "EffectFragment";
    public static final String ARG_SOUND_NUMBER = "track_number";
    private ShortSoundTrack track;
    private String effect = null;


    // Creates the view to put in to the card View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.i(TAG, "called onCreateView");
        View view = inflater.inflate(R.layout.eq_canvas, container, false);
        if (effect != null)
            ((TextView) view.findViewById(R.id.effectNameTitle)).setText(effect);
        return view;
    }

    public void setName(String name) {
        Log.i(TAG, "called setName");
        effect = name;
        if (getView() != null)
            ((TextView) getView().findViewById(R.id.effectNameTitle)).setText(effect);
    }

}
