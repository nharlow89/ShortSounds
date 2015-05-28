package com.sloths.speedy.shortsounds.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;

/**
 * A TrackView represents the views associated with individual tracks.
 */
public class TrackList extends RecyclerView {
    private RecyclerView.LayoutManager mLayoutManager;
    private TrackViewAdapter mAdapter;
    private TextView recordSound;

    /**
     * Constructor for a TrackView.
     * @param context The Context associated with this TrackView.
     * @param attrs The AttributeSet associated with this TrackView.
     */
    public TrackList(Context context, AttributeSet attrs) {
        super(context, attrs);
        // create an mLayoutManager which is required for RecyclerViews
        mLayoutManager = new LinearLayoutManager(context);
        // set the LayoutManager for the RecyclerView
        setLayoutManager(mLayoutManager);
        // set the adapter for the RecyclerView, passing in the data
        mAdapter = new TrackViewAdapter(context);
        setAdapter(mAdapter);

        recordSound = (TextView) findViewById(R.id.recordSoundText);
        assert recordSound != null;
    }

    public void notifyTrackNameChanged() {
        mAdapter.notifyDataSetChanged();
    }


    /**
     * Notify the RecyclerViewAdapter that a track has been added to this
     * ShortSound.
     */
    public void notifyTrackAdded( int index ) {
        mAdapter.notifyItemInserted(index);
    }
}
