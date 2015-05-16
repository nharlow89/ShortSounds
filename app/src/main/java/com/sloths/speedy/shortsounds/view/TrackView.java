package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.sloths.speedy.shortsounds.model.ShortSound;

/**
 * A TrackView represents the views associated with individual tracks.
 */
public class TrackView extends RecyclerView {
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerViewAdapter mAdapter;
    private ShortSound mShortSound;
    private Context mContext;

    /**
     * Constructor for a TrackView.
     * @param context The Context associated with this TrackView.
     * @param attrs The AttributeSet associated with this TrackView.
     */
    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setLayoutManagerAndAdapter();
    }

    /**
     * Notify the RecyclerViewAdapter that a track has been added to this
     * ShortSound.
     */
    public void notifyTrackAdded( int index ) {
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemInserted( index );
    }

    /**
     * Sets the layout manager and adapter for the RecyclerView. A layout manager
     * is required for a RecyclerView, though we aren't currently using it.
     */
    public void setLayoutManagerAndAdapter() {
        mShortSound = ((MainActivity) mContext).getCurShortSound();
        if (mShortSound != null) {
            // create an mLayoutManager which is required for RecyclerViews
            mLayoutManager = new LinearLayoutManager(mContext);
            // set the LayoutManager for the RecyclerView
            setLayoutManager(mLayoutManager);
            // set the adapter for the RecyclerView, passing in the data
            mAdapter = new RecyclerViewAdapter(mShortSound, mContext);
            setAdapter(mAdapter);
        }
    }

    /**
     * Returns the RecyclerViewAdapter associated with this TrackView.
     * @return RecyclerViewAdapter associated with this TrackView
     */
    public RecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }
}
