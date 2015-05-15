package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.ShortSound;

import java.util.Random;


public class TrackView extends RecyclerView {

    public static final String ARG_SOUND_ID = "short_sound_id";
    private static final String TAG = "RecyclerViewFragment";
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerViewAdapter mAdapter;
    private ShortSound mShortSound;
    double mLastRandom = 2;
    private Context context;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }
    private LineGraphSeries<DataPoint> series;


    public TrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        notifyNewSS();
    }

    /**
     * Notify the Fragment adapter that a track has been added to this
     * ShortSound.
     */
    public void notifyTrackAdded( int index ) {
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemInserted( index );
    }

    public void notifyNewSS() {
        mShortSound = ((MainActivity) context).getCurShortSound();
        if (mShortSound != null) {
//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.track_view);
            // create an mLayoutManager which is required for RecyclerViews
            mLayoutManager = new LinearLayoutManager(context);
            // set the LayoutManager for the RecyclerView
            setLayoutManager(mLayoutManager);
            // set the adapter for the RecyclerView, passing in the data
            mAdapter = new RecyclerViewAdapter(mShortSound, context);
            setAdapter(mAdapter);
        }
    }

    public RecyclerViewAdapter getmAdapter() {
        return mAdapter;
    }

//    private void showToast(String text, int length) {
//        Toast toast = Toast.makeText(getActivity(), text, length);
//        LinearLayout layout =(LinearLayout)toast.getView();
//        TextView textView = ((TextView)layout.getChildAt(0));
//        textView.setTextSize(20);
//        textView.setGravity(Gravity.CENTER);
//        toast.show();
//    }
}
