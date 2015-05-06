package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.MediaState;
import com.sloths.speedy.shortsounds.model.ReverbEffect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.IOException;
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
        viewHolder.setShortSoundTrack( mShortSound.getTracks().get(position) );
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mShortSound.getTracks().size();
    }

    /**
     * Stop all currently playing tracks in the pool.
     */
    public void stopAllTracks() {
        for( Map.Entry<Long, Pair<MediaPlayer,MediaState>> entry: mMediaPlayerPool.entrySet() ) {
            MediaPlayer player = entry.getValue().first;
            if ( player.isPlaying() ) {
                player.stop();
                player.prepareAsync();
                Log.d("DEBUG", "stop track["+entry.getKey()+"]");
            }
        }
    }

    /**
     * Play all the tracks that are in the audio pool.
     */
    public void playAllTracks() {
        // Loop through each track, make sure it is prepared, and then play.
        for( Map.Entry<Long, Pair<MediaPlayer,MediaState>> entry: mMediaPlayerPool.entrySet() ) {
            MediaPlayer player = entry.getValue().first;
            MediaState state = entry.getValue().second;
            if ( state.currentState == MediaState.STARTED ) {
                player.stop();
                state.currentState = MediaState.STOPPED;
            }
            if ( state.currentState != MediaState.PREPARED ) {
                try {
                    player.prepare();
                    state.currentState = MediaState.PREPARED;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Now that all the tracks are prepared, play them
        for( Map.Entry<Long, Pair<MediaPlayer,MediaState>> entry: mMediaPlayerPool.entrySet() ) {
            MediaPlayer player = entry.getValue().first;
            MediaState state = entry.getValue().second;
            player.start();
            state.currentState = MediaState.STARTED;
        }

    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vTitle;
        private final LinearLayout controller;
        private Button mPlayTrackButton;
        private MediaPlayer mMediaPlayer;
        private MediaState mMediaState;
        private ShortSoundTrack mShortSoundTrack;
        final Button eqButton;
        final Button reverbButton;
        final Button distButton;
        final Button bitButton;
        //        private final ListView effectsList;
        private boolean trackExpanded;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new TrackListener());

            vTitle = (TextView) v.findViewById(R.id.track_title);
            controller = (LinearLayout) v.findViewById(R.id.track_child);
            eqButton = ((Button) v.findViewById(R.id.eq_button));
            reverbButton = ((Button) v.findViewById(R.id.reverb_button));
            distButton = ((Button) v.findViewById(R.id.dist_button));
            bitButton = ((Button) v.findViewById(R.id.bit_button));
            mPlayTrackButton = (Button) v.findViewById(R.id.trackPlay);
            setUpButtons(new Button[] {eqButton, reverbButton, bitButton, distButton});

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
         * Set the event handler for the Play button on a given track.
         */
        private void setPlayClickHandler() {
            mPlayTrackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                if ( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                    mMediaState.currentState = MediaState.STOPPED;
                    mMediaPlayer.prepareAsync();
                    mPlayTrackButton.setBackground( context.getResources().getDrawable(R.drawable.ic_action_play) );
                    Log.d("DEBUG", "stop track[" + mShortSoundTrack.getId() + "]");
                } else if ( mMediaState.currentState == MediaState.PREPARED ) {
                    stopAllTracks();
                    mMediaPlayer.start();
                    mMediaState.currentState = MediaState.STARTED;
                    mPlayTrackButton.setBackground(context.getResources().getDrawable(R.drawable.ic_action_stop));
                    Log.d("DEBUG", "play track[" + mShortSoundTrack.getId() + "]");
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
            mMediaPlayer = new MediaPlayer();
            mMediaState = new MediaState();
            Context context = ShortSoundsApplication.getAppContext();
            String path = context.getFilesDir().getAbsolutePath();
            try {
                mMediaPlayer.setDataSource(path + "/" + mShortSoundTrack.getFile());
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaState.currentState = MediaState.PREPARED;
                        Log.d("DEBUG", "MediaPlayer prepared["+mShortSoundTrack.getId()+"]");
                    }
                });
                // Add this to the Audio Pool
                mMediaPlayerPool.put(track.getId(), new Pair(mMediaPlayer, mMediaState));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private List<Effect> getEffects() {
            List<Effect> retList = new ArrayList<Effect>();
            Effect effect1 = new EqEffect();
            retList.add(effect1);
            Effect effect2 = new ReverbEffect();
            retList.add(effect2);
            retList.add(effect2);
            return retList;
        }

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

        /* The click listener for ListView in the navigation drawer */
        private class TrackListener implements View.OnClickListener  {
            @Override
            public void onClick(View v) {

                //listener.onButtonClicked(v, getPosition());
                if (!trackExpanded) {
                    // Expand a track
                    controller.setVisibility(View.VISIBLE);
                    trackExpanded = true;
                    // Upon selecting a track we need to prepare the MediaPlayer (this only needs
                    // to happen once per track).
                    if ( mMediaState.currentState != MediaState.PREPARED ) {
                        mMediaPlayer.prepareAsync();
                    }
                } else {
                    // Close the current open track
                    controller.setVisibility(View.GONE);
                    trackExpanded = false;
                    // Stop the MediaPlayer if it was active
                    if ( mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.stop();
                        mMediaState.currentState = MediaState.STOPPED;
                        Log.d("DEBUG", "stop track["+mShortSoundTrack.getId()+"]");
                    }
                }
            }
        }
    }

    public interface RVListener {
        public void onButtonClicked(View v, int track, String name);
    }
}