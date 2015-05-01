package com.sloths.speedy.shortsounds;

/**
 * Created by joel on 4/25/2015.
 */
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;


public class RecyclerViewFragment extends Fragment implements RecyclerViewAdapter.RVListener {

    public static final String ARG_SOUND_NUMBER = "sound_number";
    private static final String TAG = "RecyclerViewFragment";
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected RecyclerViewAdapter mAdapter;
    protected String[] trackNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // grab the root view of the layout for recycler view
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        // grab the RecyclerView component from the layout
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // create an mLayoutManager which is required for RecyclerViews
        mLayoutManager = new LinearLayoutManager(getActivity());
        // set the LayoutManager for the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        // set the adapter for the RecyclerView, passing in the data
        mAdapter = new RecyclerViewAdapter(trackNames, this);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;

    }


    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        int sound_num = getArguments().getInt(ARG_SOUND_NUMBER);
        String sound = getResources().getStringArray(R.array.shortsounds_array)[sound_num];
        getActivity().setTitle(sound);
        // Populate array of tracks
        List<String> trackTitles = Arrays.asList(getResources().getStringArray(R.array.track_array));
        trackNames = (String[]) trackTitles.toArray();
    }

    @Override
    public void onButtonClicked(View v, int track, String effect) {
        loadEffectDialog(track, effect);
    }

    private void loadEffectDialog(final int track, final String effect) {
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

    private void showToast(String text, int length) {
        Toast toast = Toast.makeText(getActivity(), text, length);
        LinearLayout layout =(LinearLayout)toast.getView();
        TextView textView = ((TextView)layout.getChildAt(0));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }

}
