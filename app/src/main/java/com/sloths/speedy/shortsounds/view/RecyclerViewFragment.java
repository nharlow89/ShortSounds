package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.ShortSound;

import java.util.Random;


public class RecyclerViewFragment extends Fragment implements RecyclerViewAdapter.RVListener {

    public static final String ARG_SOUND_ID = "short_sound_id";
    private static final String TAG = "RecyclerViewFragment";
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected RecyclerViewAdapter mAdapter;
    private ShortSound mShortSound;
    private ImageButton mGlobalPlayButton;
    private LinearLayout mParentLayout;
    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }
    private LineGraphSeries<DataPoint> series;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long sound_id = getArguments().getLong(ARG_SOUND_ID);
        mShortSound = ShortSound.getById( sound_id );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save the parent container for later reference (targeting the global play button)
        mParentLayout = (LinearLayout) container.getParent();
        // grab the root view of the layout for recycler view
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        // grab the RecyclerView component from the layout
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // create an mLayoutManager which is required for RecyclerViews
        mLayoutManager = new LinearLayoutManager(getActivity());
        // set the LayoutManager for the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        // set the adapter for the RecyclerView, passing in the data
        mAdapter = new RecyclerViewAdapter(mShortSound, this);
        mRecyclerView.setAdapter(mAdapter);
        setGlobalPlayButtonClickHandler();
//
//        mRecyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
//            @Override
//            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
//
//            }
//        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // We need to cleanup the audio stuff from this ShortSound
        mShortSound.stopAllTracks();
        mShortSound.releaseAllTracks();
    }

    /**
     * Now that we have a selected ShortSound in focus we need to update the Global Play
     * button's click handler to play all tracks associated with this ShortSound.
     */
    private void setGlobalPlayButtonClickHandler() {
        mGlobalPlayButton = (ImageButton)mParentLayout.findViewById(R.id.imageButtonPlay);
        mGlobalPlayButton.setEnabled(true);
        Log.d("DEBUG", "Found the global play button! " + mGlobalPlayButton);
        mGlobalPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: we need to handle the case when the ShortSound finishes playing!
                if ( mShortSound.isPlaying() ) {
                    // The ShortSound is already playing, stop it.
                    mShortSound.pauseAllTracks();
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                } else {
                    if ( mShortSound.isPaused() ) {
                        // The ShortSound was previously playing, unpause it.
                        mShortSound.unPauseAllTracks();
                    } else {
                        // The ShortSound is not playing yet, play it.
                        mShortSound.playAllTracks();
                    }
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                }

            }
        });
    }

    // This is used for loading the popup when clicking a specific effect
    @Override
    public void onButtonClicked(View v, int track, String effect) {
        if (effect.equals("EQ")) {
            loadEQDialog(track, effect);
        } else if (effect.equals("Reverb")) {
            loadReverbEffectDialog(track, effect);
        } else {
            loadGeneralEffectDialog(track, effect);
        }
    }

    // Helper method for loading a reverb effect popup
    private void loadReverbEffectDialog(final int track, final String effect) {
        final AlertDialog.Builder imageDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = getEffectCanvas(effect);
        imageDialog.setView(layout);
        final AlertDialog dialog = imageDialog.create();

        // TODO: Set reverb values in here
        ReverbCanvas reverbCanvas = (ReverbCanvas) layout.findViewById(R.id.effect_canvas);

        // effectCanvas.setReverbVals(initEQValues);

        // Shows a text popup that the effect was saved or cleared
        layout.findViewById(R.id.saveEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Grab values from Reverb Canvas to save here
                // TODO: Implement backend for saving current Reverb effect
                // eqVals = reverbCanvas.getReverbVals();
                // saveReverb(reverbVals);
                // grab values from ReverbCanvas here, to save
                showToast(effect + " saved", Toast.LENGTH_SHORT);
            }
        });
        layout.findViewById(R.id.cancelEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(effect + "  cleared", Toast.LENGTH_SHORT);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Helper method for loading a general effect popup (holder until we get functionality
    // for new effects)
    private void loadGeneralEffectDialog(final int track, final String effect) {
        final AlertDialog.Builder imageDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.effect_view,
                (ViewGroup) getActivity().findViewById(R.id.parentEffectPanel));

        ((TextView) layout.findViewById(R.id.effectNameTitle)).setText(effect);
        imageDialog.setView(layout);

        final AlertDialog dialog = imageDialog.create();

        layout.findViewById(R.id.saveEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                showToast("Effects saved", Toast.LENGTH_SHORT);
            }
        });
        layout.findViewById(R.id.cancelEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Effect cleared", Toast.LENGTH_SHORT);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Helper method for loading a specific EQ popup
    private void loadEQDialog(final int track, final String effect) {
        Activity activity = getActivity();
        final AlertDialog.Builder imageDialog = new AlertDialog.Builder(activity);

        View layout = getEffectCanvas(effect);
        imageDialog.setView(layout);
        final AlertDialog dialog = imageDialog.create();

        // TODO: Here we can populate initial effect values from backend
        EQCanvas2 effectCanvas = (EQCanvas2) layout.findViewById(R.id.effect_canvas);
        LinearLayout ll = (LinearLayout) layout.findViewById(R.id.effect_content);


        // Shows a text popup that the effect was saved or cleared
        layout.findViewById(R.id.saveEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Grab values from EQ Canvas to save here
                // TODO: Implement backend for saving current EQ effect
                // eqVals = effectCanvas.getEQVals();
                // saveEQ(eqVals);
                dialog.dismiss();
                showToast(effect + " saved", Toast.LENGTH_SHORT);
            }
        });
        layout.findViewById(R.id.cancelEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(effect + "  cleared", Toast.LENGTH_SHORT);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    // Currently returns EQ canvas or reverb canvas view
    private View getEffectCanvas(String effect) {
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = null;
        if (effect.equals("EQ")) {
            layout = inflater.inflate(R.layout.eq_canvas,
                    (ViewGroup) activity.findViewById(R.id.eqCanvasParent), false);
        } else {
            layout = inflater.inflate(R.layout.reverb_canvas,
                    (ViewGroup) activity.findViewById(R.id.eqCanvasParent), false);
        }
        // Here we can set the specific effect values
        return layout;
    }

    private void showToast(String text, int length) {
        Toast toast = Toast.makeText(getActivity(), text, length);
        LinearLayout layout =(LinearLayout)toast.getView();
        TextView textView = ((TextView)layout.getChildAt(0));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }
}
