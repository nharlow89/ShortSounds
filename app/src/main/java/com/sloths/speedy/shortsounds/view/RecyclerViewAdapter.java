package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ShortSound mShortSound;
    private Context mContext;
    private AudioPlayer mAudioPlayer;
//    private RVListener listener;
    private ArrayList<Color> mColorPallete;
    private List<ViewHolder> mViews;


    /**
     * Initialize the dataset of the Adapter.
     *
     */
    public RecyclerViewAdapter(ShortSound sound, Context context) {
        mShortSound = sound;
        this.mContext = context;
        mAudioPlayer = ((MainActivity) this.mContext).getActiveAudioPlayer();
        mViews = new ArrayList<ViewHolder>();
    }

    /**
     * Create new views (invoked by the layout manager)
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_view, viewGroup, false);
        // Define click mRVListener for the ViewHolder's View.
        ViewHolder vh = new ViewHolder(v);
        mViews.add(vh);
        return vh;
    }

    /**
     *  Replace the contents of a view (invoked by the layout manager)
     * @param viewHolder The ViewHolder to affect
     * @param position The position of the view
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.setTitleView(position);
        viewHolder.setShortSoundTrack(mShortSound.getTracks().get(position));
        dynamicallySetCardColor(viewHolder, position);
    }

    /**
     * Sets the background color of cards. Will pull from collection of
     * preselected colors.
     * @param viewHolder The view to be affected
     * @param position The position in the RecyclerView
     */
    private void dynamicallySetCardColor(ViewHolder viewHolder, int position) {
        // There are 6 different card colors
        // so position 0-5
        View currentView = viewHolder.getViewHoldersView();
        int num_colors = 6;
        switch(position % num_colors) {
            case 0:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.purple_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.purple_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 1:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.teal_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.teal_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 2:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.deep_orange_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.deep_orange_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 3:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.pink_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.pink_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 4:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.yellow_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.yellow_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 5:
                viewHolder.mPrimaryColor = this.mContext.getResources().getColor(R.color.indigo_500);
                viewHolder.mSecondaryColor = this.mContext.getResources().getColor(R.color.indigo_200);
                setColorOnView(viewHolder, currentView);
                break;
            default:
                // something went wrong
                break;
        }
    }

    /**
     * Sets the primary and secondary color for a viewHolder and view
     * @param viewHolder The Viewholder to set the colors
     * @param currentView The View to set the colors on
     */
    private void setColorOnView(ViewHolder viewHolder, View currentView) {
        View track_parent = currentView.findViewById(R.id.track_parent);
        track_parent.setBackgroundColor(viewHolder.mPrimaryColor);
        View track_child = currentView.findViewById(R.id.track_child);
        track_child.setBackgroundColor(viewHolder.mPrimaryColor);
    }

    /**
     * Return the size of the dataset (invoked by the layout manager)
     * @return size of dataset
     */
    @Override
    public int getItemCount() {
        return mShortSound.getTracks().size();
    }

    /**
     * This view holder holds the views for a track that will be part of a short sound
     * in the recycler view.  This includes holding the track's buttons, effects, etc.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vTitle;
        private final LinearLayout vTrackChild;
        private Button mPlayTrackButton;
        private ShortSoundTrack mShortSoundTrack;
        private View vView;
        private int mPrimaryColor;
        private int mSecondaryColor;
        private boolean trackExpanded;

        /**
         * Constructor for ViewHolder
         * @param v The view associated with the ViewHolder
         */
        public ViewHolder(View v) {
            super(v);
            // init instance variables
            vView = v;
            vTitle = (TextView) v.findViewById(R.id.track_title);
            vTitle.setOnClickListener(new TrackListener());
            vTrackChild = (LinearLayout) v.findViewById(R.id.track_child);
            mPlayTrackButton = (Button) v.findViewById(R.id.trackPlay);
            trackExpanded = false;

            // init buttons
            Button eqButton = ((Button) v.findViewById(R.id.eq_button));
            Button reverbButton = ((Button) v.findViewById(R.id.reverb_button));
            Button distButton = ((Button) v.findViewById(R.id.dist_button));
            Button bitButton = ((Button) v.findViewById(R.id.bit_button));
            Switch eqToggle = ((Switch) v.findViewById(R.id.eq_switch));
            Switch reverbToggle = ((Switch) v.findViewById(R.id.reverb_switch));
            Switch distToggle = ((Switch) v.findViewById(R.id.dist_switch));
            Switch bitToggle = ((Switch) v.findViewById(R.id.bit_switch));

            // perform setup
            setUpButtons(new Button[]{eqButton, reverbButton, bitButton, distButton});
            setUpToggle(new Switch[]{eqToggle, reverbToggle, distToggle, bitToggle});
            setPlayClickHandler(v);

        }

        /**
         * Returns the View associated with this ViewHolder.
         * @return View The View associated with this ViewHolder
         */
        public View getViewHoldersView() {
            return vView;
        }

        /**
         * Set the event handler for the Play button on a given track.
         */
        private void setPlayClickHandler(View v) {
            mPlayTrackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if ( mAudioPlayer.isPlayingTrack( mShortSoundTrack ) ) {
                        mAudioPlayer.pauseTrack( mShortSoundTrack );
                        mPlayTrackButton.setBackground(mContext.getResources().getDrawable(R.drawable.ic_action_play));
                    } else {
                        mAudioPlayer.playTrack( mShortSoundTrack );
                        mPlayTrackButton.setBackground(mContext.getResources().getDrawable(R.drawable.ic_action_pause));
                    }
                }
            });
        }

        /**
         * Dynamically set the text for each track title
         * @param position The position of each track as int
         */
        public void setTitleView(int position) {
            vTitle.setText(mShortSound.getTracks().get(position).getTitle());
        }

        /**
         * Set the ShortSoundTrack that this view corresponds to.
         * @param track
         */
        public void setShortSoundTrack(ShortSoundTrack track) {
            mShortSoundTrack = track;
            /*
            mShortSoundTrack.setOnPlayCompleteListener(new ShortSoundTrack.OnCompleteListener() {
                @Override
                public void onComplete() {
                    mPlayTrackButton.setBackground(mContext.getResources().getDrawable(R.drawable.ic_action_play));
                    mShortSound.notifyTrackStopped();
                }
            });
            */
        }

//        private List<Effect> getEffects() {
//            List<Effect> retList = new ArrayList<Effect>();
//            Effect effect1 = new EqEffect();
//            retList.add(effect1);
//            Effect effect2 = new ReverbEffect();
//            retList.add(effect2);
//            retList.add(effect2);
//            return retList;
//        }

        private void setUpButtons(Button[] bs) {
            for (int i = 0; i < 4; i++) {

                final String name = bs[i].getText().toString();
                bs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mContext).effectEditSelected(getPosition(), name);
                    }
                });
            }
        }
        
        // TODO implement effect toggle
        private void setUpToggle(Switch[] sws) {
            for (Switch sw : sws) {
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //effect on
                        } else {
                            //effect off
                        }
                    }
                });
            }
        }

        /**
         * The click mRVListener for individual tracks
         */
        private class TrackListener implements View.OnClickListener  {
            @Override
            public void onClick(View v) {
                if (!trackExpanded) {
                    // Expand a track
                    collapseAllOtherTracks();
                    expandTrackChildView(vTrackChild);
                    trackExpanded = true;
                } else {
                    // Close the current open track
                    collapseTrackChildView(vTrackChild);
                    trackExpanded = false;
                    // Stop the track (just in case it was playing)
                    mAudioPlayer.stopTrack( mShortSoundTrack );
                    mPlayTrackButton.setBackground(mContext.getResources().getDrawable(R.drawable.ic_action_play));
                }
            }
        }
    }

    /**
     * Collapse all other tracks.
     */
    public void collapseAllOtherTracks() {
        for(ViewHolder vh: mViews) {
            if ( vh.trackExpanded ) {
                collapseTrackChildView(vh.vTrackChild);
            }
        }
    }

    /**
     * Uses animation to expand the child view of a track
     * @param v The view to expand
     */
    public void expandTrackChildView(final View v) {
        v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RecyclerView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // .5 dp/ms
        a.setDuration((int) (targetHeight / (2 * v.getContext().getResources().getDisplayMetrics().density)));
        v.startAnimation(a);
    }

    /**
     * Uses animation to collapse the child view of a track
     * @param v The view to collapse
     */
    public void collapseTrackChildView(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // .5 dp/ms
        a.setDuration((int)(initialHeight / (2 * v.getContext().getResources().getDisplayMetrics().density)));
        v.startAnimation(a);
    }

}