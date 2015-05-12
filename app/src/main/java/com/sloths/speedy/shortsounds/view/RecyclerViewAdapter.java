package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.MediaState;
import com.sloths.speedy.shortsounds.model.ReverbEffect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ShortSound mShortSound;
    // This is a pool of all the MediaPlayers for each track. The mapping is from ShortSoundTrack id
    // to a pair containg the MediaPlayer and a boolean that describes if the MediaPlayer is currently
    // prepared or not.
    public Map<Long, Pair<MediaPlayer, MediaState>> mMediaPlayerPool;
    private Context context;
    private RVListener listener;
    private ArrayList<Color> mColorPallete;


    /**
     * Initialize the dataset of the Adapter.
     *
     */
    public RecyclerViewAdapter(ShortSound sound, RecyclerViewFragment rvf) {
        mShortSound = sound;
        mMediaPlayerPool = new HashMap<>();
        this.context = rvf.getActivity();
        listener = rvf;


    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_view, viewGroup, false);
        // Define click listener for the ViewHolder's View.
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
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
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.purple_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.purple_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 1:
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.teal_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.teal_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 2:
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.deep_orange_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.deep_orange_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 3:
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.pink_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.pink_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 4:
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.yellow_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.yellow_200);
                setColorOnView(viewHolder, currentView);
                break;
            case 5:
                viewHolder.mPrimaryColor = this.context.getResources().getColor(R.color.indigo_500);
                viewHolder.mSecondaryColor = this.context.getResources().getColor(R.color.indigo_200);
                setColorOnView(viewHolder, currentView);
                break;
            default:
                // something went wrong
                break;
        }
    }

    private void setColorOnView(ViewHolder viewHolder, View currentView) {
        View track_parent = currentView.findViewById(R.id.track_parent);
        track_parent.setBackgroundColor(viewHolder.mPrimaryColor);
        View track_child = currentView.findViewById(R.id.track_child);
        track_child.setBackgroundColor(viewHolder.mPrimaryColor);
    }

    // Return the size of your dataset (invoked by the layout manager)
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
        private final LinearLayout controller;
        private Button mPlayTrackButton;
        private ShortSoundTrack mShortSoundTrack;
        private View vView;
        private int mPrimaryColor;
        private int mSecondaryColor;
        //        private final ListView effectsList;
        private boolean trackExpanded;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new TrackListener());
            vTitle = (TextView) v.findViewById(R.id.track_title);
            vView = v;
            controller = (LinearLayout) v.findViewById(R.id.track_child);
            Button eqButton = ((Button) v.findViewById(R.id.eq_button));
            Button reverbButton = ((Button) v.findViewById(R.id.reverb_button));
            Button distButton = ((Button) v.findViewById(R.id.dist_button));
            Button bitButton = ((Button) v.findViewById(R.id.bit_button));
            Switch eqToggle = ((Switch) v.findViewById(R.id.eq_switch));
            Switch reverbToggle = ((Switch) v.findViewById(R.id.reverb_switch));
            Switch distToggle = ((Switch) v.findViewById(R.id.dist_switch));
            Switch bitToggle = ((Switch) v.findViewById(R.id.bit_switch));
            mPlayTrackButton = (Button) v.findViewById(R.id.trackPlay);
            setUpButtons(new Button[] {eqButton, reverbButton, bitButton, distButton});
            setUpToggle(new Switch[] {eqToggle, reverbToggle, distToggle, bitToggle});
            setPlayClickHandler();
            controller.setVisibility(View.GONE);

            // Populate effects in the effects list the track keeps
            // TODO: Link the effects to the real ones in the database
            // Currently populating a fake effects list
//            List<Effect> effects = getEffects();
//
//            effectsList = (ListView) v.findViewById(R.id.effects_list_b);
//            EffectsListAdapter effectsAdapter = new EffectsListAdapter(context, effects);
//            effectsList.setAdapter(effectsAdapter);
            trackExpanded = false;
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
        private void setPlayClickHandler() {
            mPlayTrackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if ( mShortSoundTrack.isPlaying() ) {
                        // If the track was playing then stop it.
                        mShortSoundTrack.stop();
                        mShortSoundTrack.prepareAsync();
                        mPlayTrackButton.setBackground( context.getResources().getDrawable(R.drawable.ic_action_play) );
                    } else {
                        // The track was not playing, stop any other tracks and play this one.
                        mShortSound.stopAllTracks();
                        mShortSoundTrack.play();
                        mPlayTrackButton.setBackground(context.getResources().getDrawable(R.drawable.ic_action_stop));
                    }
                }
            });
        }

        public void setTitleView(int position) {
            vTitle.setText(mShortSound.getTracks().get(position).getTitle());
        }

        /**
         * Set the ShortSoundTrack that this view corresponds to. We also setup the
         * MediaPlayer for this particular track.
         * @param track
         */
        public void setShortSoundTrack( ShortSoundTrack track ) {
            mShortSoundTrack = track;
            mShortSoundTrack.setOnPlayCompleteListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mShortSoundTrack.stop();  // Make sure the state is updated with ShortSoundTrack
                    mPlayTrackButton.setBackground(context.getResources().getDrawable(R.drawable.ic_action_play));
                }
            });
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
                        listener.onButtonClicked(v, getPosition(), name);
                    }
                });
            }
        }

        // This is kind of frustratingly complicated, but there's no readily apparent way for me
        // to attach an Effect object to a Switch.  If you see a better way feel free to change
        // this.  -Casey
        private void setUpToggle(Switch[] sws) {
            for (Switch sw : sws) {
                if (sw == vView.findViewById(R.id.reverb_switch)) {
                    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.REVERB);
                            } else {
                                mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.REVERB);
                            }
                        }
                    });
                } else if (sw == vView.findViewById(R.id.eq_switch)) {
                    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.EQ);
                            } else {
                                mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.EQ);
                            }
                        }
                    });
                } else {
                    throw new UnsupportedOperationException("only reverb and eq effects are currently implemented");
                }
            }
        }

        /* The click listener for ListView in the navigation drawer */
        private class TrackListener implements View.OnClickListener  {
            @Override
            public void onClick(View v) {

                //listener.onButtonClicked(v, getPosition());
                if (!trackExpanded) {
                    // Expand a track
                    controller.setVisibility(View.VISIBLE);
                    trackExpanded = true;
                    // Upon selecting a track we need to prepare the track for playing.
                    mShortSoundTrack.prepareAsync();
                } else {
                    // Close the current open track
                    controller.setVisibility(View.GONE);
                    trackExpanded = false;
                    // Stop the track (just in case it was playing)
                    mShortSoundTrack.stop();
                }
            }
        }
    }

    // The button clicking implementation is actually implemented in the RecyclerViewFragment
    // It holds the logic for populating an effect popup
    public interface RVListener {
        void onButtonClicked(View v, int track, String name);
    }
}