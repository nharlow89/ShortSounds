package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.sloths.speedy.shortsounds.ModelControl;
import com.sloths.speedy.shortsounds.R;
import java.util.ArrayList;
import java.util.List;

/**
 * The RecyclerViewAdapter takes track data and uses it to populate the views associated
 * with the RecyclerView
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private ModelControl modelControl;
    private List<ViewHolder> mViews;


    /**
     * Constructor for the RecyclerViewAdapter.
     * @param context The Context associated with this RecyclerViewAdapter.
     */
    public RecyclerViewAdapter(Context context) {
        this.mContext = context;
        modelControl = ((MainActivity) context).getModelControl();
        mViews = new ArrayList<>();
    }

    /**
     * Create new views (invoked by the layout manager)
     * @param viewGroup The ViewGroup
     * @param viewType An int representation of the viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_view, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        mViews.add(vh);
        return vh;
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     * @param viewHolder The ViewHolder
     * @param position The position associated with a Track in the RecyclerView
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.setTitleView(position);
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
     * Sets a color on both the view and in the viewHolder
     * @param viewHolder The viewHolder to set the color on
     * @param currentView The overall current view
     */
    private void setColorOnView(ViewHolder viewHolder, View currentView) {
        View track_parent = currentView.findViewById(R.id.track_parent);
        track_parent.setBackgroundColor(viewHolder.mPrimaryColor);
        View track_child = currentView.findViewById(R.id.track_child);
        track_child.setBackgroundColor(viewHolder.mPrimaryColor);
    }

    /**
     *  Return the size of your dataset (invoked by the layout manager)
     * @return An int representing the number of Tracks
     */
    @Override
    public int getItemCount() {
        return ((MainActivity) mContext).getCurShortSoundNames().size();
    }

    /**
     * This view holder holds the views for a track that will be part of a short sound
     * in the recycler view.  This includes holding the track's buttons, effects, etc.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vTitle;
        private final LinearLayout vTrackChild;
        private Button mPlayTrackButton;
        private View vView;
        private int mPrimaryColor;
        private int mSecondaryColor;
        boolean trackIsSolo;
        int soloOff;
        private Switch eqToggle;
        private Switch reverbToggle;
        private Switch distToggle;
        private Switch bitToggle;
        private boolean mTrackExpanded;

        /**
         * Constructor for a ViewHolder
         * @param v The View associated with this ViewHolder
         */
        public ViewHolder(View v) {
            super(v);
            // init instance variables
            vView = v;
            vTitle = (TextView) v.findViewById(R.id.track_title);
            vTitle.setOnClickListener(new TrackListener());
            vTrackChild = (LinearLayout) v.findViewById(R.id.track_child);
            mPlayTrackButton = (Button) v.findViewById(R.id.trackSolo);//TODO name change
            trackIsSolo = false;// hacky.  will change to model value later
            soloOff = mContext.getResources().getColor(R.color.button_material_light);

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
            setPlayClickHandler();
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
                    if (!trackIsSolo)
                        mPlayTrackButton.setBackgroundColor(mSecondaryColor);
                    else
                        mPlayTrackButton.setBackgroundColor(soloOff);

                    trackIsSolo = !trackIsSolo;
                    // TODO solo button set volume
                }
            });
        }

        /**
         * Sets the titles for tracks in RecyclerView
         * @param position The position of a track
         */
        public void setTitleView(int position) {
            vTitle.setText(((MainActivity) mContext).getCurShortSoundNames().get(position));
        }


        /**
         * sets up the buttons inside a TrackView
         * @param bs a Button[] of buttons
         */
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

//        // This is kind of frustratingly complicated, but there's no readily apparent way for me
//        // to attach an Effect object to a Switch.  If you see a better way feel free to change
//        // this.  -Casey
//        private void setUpToggles() {
//            Log.d("effects", "setUpToggle called");
//            reverbToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Log.d("effects", "reverb switch clicked");
//                    if (isChecked) {
//                        mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.REVERB);
//                    } else {
//                        mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.REVERB);
//                    }
//                }
//            });
//            eqToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Log.d("effects", "eq switch clicked");
//                    if (isChecked) {
//                        mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.EQ);
//                    } else {
//                        mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.EQ);
//                    }
//                }
//            });
//            distToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Log.d("effects", "distortion switch clicked");
//                    if (isChecked) {
//                        mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.DISTORTION);
//                    } else {
//                        mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.DISTORTION);
//                    }
//                }
//            });
//            bitToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    Log.d("effects", "bitcrush switch clicked");
//                    if (isChecked) {
//                        mShortSoundTrack.addEffect(ShortSoundTrack.EFFECT.BITCRUSH);
//                    } else {
//                        mShortSoundTrack.removeEffect(ShortSoundTrack.EFFECT.BITCRUSH);
//                    }
//                }
//            });
//        }

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
         * The listener for when a track is clicked on
         */
        private class TrackListener implements View.OnClickListener  {
            @Override
            public void onClick(View v) {
                if (!modelControl.isRecording()) {
                    Log.i("Adapter", "vtrackchild == visible " + (vTrackChild.getVisibility() == View.VISIBLE));
                    Log.i("Adapter", "vtrackchild.visible " + vTrackChild.getVisibility());
                    Log.i("Adapter", "mtrackexpanded " + mTrackExpanded);
                    Log.i("Adapter", "");
                    if (vTrackChild.getVisibility() != View.VISIBLE) {
                        // Expand a track
                        collapseAllOtherTracks();
                        expandTrackChildView(vTrackChild);
                    } else {
                        // Close the current open track
                        collapseTrackChildView(vTrackChild);
                    }

                }
            }
        }
    }

    /**
     * Collapse all other tracks.
     */
    public void collapseAllOtherTracks() {
        for(ViewHolder vh: mViews) {
            if ( vh.mTrackExpanded) {
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

        // 1 dp/ms
        a.setDuration((int) (targetHeight / (v.getContext().getResources().getDisplayMetrics().density)));
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

        // 1 dp/ms
        a.setDuration((int)(initialHeight / (v.getContext().getResources().getDisplayMetrics().density)));
        v.startAnimation(a);
    }

    // The button clicking implementation is actually implemented in the RecyclerViewFragment
    // It holds the logic for populating an effect popup
    public interface ChooseEffectListener {
        void onButtonClicked(int track, String name);
    }

    public View getViewAtPos(int i) {
        return mViews.get(i).getViewHoldersView();
    }
}