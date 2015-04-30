package com.sloths.speedy.shortsounds;

/**
 * Created by joel on 4/25/2015.
 */
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private String[] mDataSet;
    private Context context;


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param trackNames String[] containing the data to populate views to be used by RecyclerView.
     */
    public RecyclerViewAdapter(String[] trackNames, Context context) {
        mDataSet = trackNames;
        this.context = context;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_view, viewGroup, false);
        // Define click listener for the ViewHolder's View.

        return new ViewHolder(v);
    }

    public String[] getTrackNames() {
        return mDataSet;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTitle().setText(mDataSet[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vTitle;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new TrackItemClickListener());
            vTitle = (TextView) v.findViewById(R.id.track_title);

        }

        public TextView getTitle() {
            return vTitle;
        }

        /* The click listener for ListView in the navigation drawer */
        private class TrackItemClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                selectTrack(getPosition());
            }
        }

        /**
         * Swaps card view holder w/ our track content
         */
        private void selectTrack(int position) {
            // Grabs the ShortSound and populates the screen with it

            // Create fragment for tracks
            String currTrack = getTrackNames()[position];
            Fragment trackFragment = new TrackEffectsPanelFragment();

            // ================================================================
            // TODO: Figure out how to recycle card view to show full track info
            // ================================================================
            // Replaces the main content screen w/ track
            FragmentActivity activity = (FragmentActivity) context;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, trackFragment).commit();
        }

    }
}