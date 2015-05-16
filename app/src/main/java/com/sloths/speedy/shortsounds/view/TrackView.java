package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * A TrackView represents the views associated with individual tracks.
 */
public class TrackView extends RecyclerView {
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerViewAdapter mAdapter;

    /**
     * Constructor for a TrackView.
     * @param context The Context associated with this TrackView.
     * @param attrs The AttributeSet associated with this TrackView.
     */
    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // create an mLayoutManager which is required for RecyclerViews
        mLayoutManager = new LinearLayoutManager(context);
        // set the LayoutManager for the RecyclerView
        setLayoutManager(mLayoutManager);
        // set the adapter for the RecyclerView, passing in the data
        mAdapter = new RecyclerViewAdapter(context);
        setAdapter(mAdapter);
    }

    /**
     * Notify the RecyclerViewAdapter that a track has been added to this
     * ShortSound.
     */
    public void notifyTrackAdded( int index ) {
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemInserted( index );
    }
}
