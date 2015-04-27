package com.sloths.speedy.shortsounds;

/**
 * Created by joel on 4/25/2015.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;


public class RecyclerViewFragment extends Fragment {

    public static final String ARG_SOUND_NUMBER = "sound_number";
    private static final String TAG = "RecyclerViewFragment";
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected RecyclerViewAdapter mAdapter;
    protected String[] trackNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        initDataset();
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecyclerViewAdapter(trackNames);
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
}
