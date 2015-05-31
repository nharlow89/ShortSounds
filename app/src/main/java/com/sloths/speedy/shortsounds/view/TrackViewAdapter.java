package com.sloths.speedy.shortsounds.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.controller.ModelControl;
import com.sloths.speedy.shortsounds.model.Effect;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecyclerViewAdapter takes track data and uses it to populate the views associated
 * with the RecyclerView
 *
 * @author Joel Sigo
 */
public class TrackViewAdapter extends RecyclerView.Adapter<TrackViewAdapter.ViewHolder> {
    public static final String TAG = "RecyclerViewAdapter";
    MainActivity main;
    private Context mContext;
    private ModelControl modelControl;
    private List<ViewHolder> mViews;
    public static final int MAX_VOLUME = 100;
    private final int screenHeight;
    private ColorWheel colorWheel;


    /**
     * Constructor for the RecyclerViewAdapter.
     * @param context The Context associated with this RecyclerViewAdapter.
     */
    public TrackViewAdapter(Context context) {
        this.mContext = context;
        main = (MainActivity) context;
        colorWheel = ColorWheel.instance();
        colorWheel.buildWheelIfNeeded(mContext);
        modelControl = ModelControl.instance();
        mViews = new ArrayList<>();
        Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        screenHeight = p.y;
    }

    /**
     * Create new views (invoked by the layout manager)
     * @param viewGroup The ViewGroup
     * @param viewType An int representation of the viewType
     * @return the viewholder of the new view
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
        viewHolder.setUpColorsIfNeeded(position);
        setColorOnView(viewHolder);
        viewHolder.setUpVolume(position);
        viewHolder.setInitToggleState(position);
    }


    /**
     * Sets a color on both the view and in the viewHolder
     * @param viewHolder The viewHolder to set the color on
     */
    private void setColorOnView(ViewHolder viewHolder) {
        View currentView = viewHolder.getViewHoldersView();
        currentView.findViewById(R.id.track_parent).setBackgroundColor(viewHolder.mPrimaryColor);
        currentView.findViewById(R.id.track_child).setBackgroundColor(viewHolder.mPrimaryColor);
    }

    /**
     *  Return the size of your dataset (invoked by the layout manager)
     * @return An int representing the number of Tracks
     */
    @Override
    public int getItemCount() {
        return main.getCurrentShortSoundSize();
    }


    /**
     * This view holder holds the views for a track that will be part of a short sound
     * in the recycler view.  This includes holding the track's buttons, effects, etc.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public static final int EQ_INDEX = 0;
        public static final int REVERB_INDEX = 1;
        private final TextView vTitle;
        private final LinearLayout vTrackChild;
        private final ImageButton vToggle;
        private View vView;
        private Integer mPrimaryColor = null;
        private Integer mSecondaryColor = null;
        int soloOff;
        // Switches
        private ToggleButton[] effectToggles;
        private Button[] effectButtons;
        private Effect.Type[] effectTypes;
        private TrackAnimator trackAnimator;


        /**
         * Constructor for a ViewHolder
         * @param v The View associated with this ViewHolder
         */
        public ViewHolder(View v)  {
            super(v);
            // init instance variables
            vView = v;
            vToggle = (ImageButton) v.findViewById(R.id.toggleTrackView);
            trackAnimator = new TrackAnimator();
            vToggle.setOnClickListener(trackAnimator);
            vTitle = (TextView) v.findViewById(R.id.track_title);
            vTitle.setOnClickListener(trackAnimator);
            vTrackChild = (LinearLayout) v.findViewById(R.id.track_child);
            vTrackChild.setVisibility(View.GONE);
            effectTypes = new Effect.Type[]{Effect.Type.EQ, Effect.Type.REVERB};
            effectButtons = new Button[]{
                                        ((Button) v.findViewById(R.id.eq_button)),
                                        ((Button) v.findViewById(R.id.reverb_button))
                                    };
            effectToggles = new ToggleButton[]{
                                        ((ToggleButton) v.findViewById(R.id.eq_switch)),
                                        ((ToggleButton) v.findViewById(R.id.reverb_switch))
                                    };
            v.findViewById(R.id.track_list_parent);
            // perform setup
            setUpSolo();
            setUpButtons();
            setUpToggle();
            setUpSwipeListener();
        }

        /**
         * Sets up the "swipe to delete" and click handler for the track title.
         */
        private void setUpSwipeListener() {
            vTitle.setOnClickListener(null);
            vTitle.setOnTouchListener(new TrackSwipeListener(vView, new SwipeToDeleteListener() {
                @Override
                public void onTrackDelete() {
                    trackAnimator.deleteTrackView();
                }

                @Override
                public void onEditTrackTitle() {
                    main.renameTrack(getPosition());
                }
            }));
        }

        /**
         * initializes the state of this track's volume
         *
         * @param track, the position of the track in the list
         */
        private void setUpVolume(int track) {
            SeekBar volumeSlider = (SeekBar) vView.findViewById(R.id.volumeSlider);
            volumeSlider.setMax(MAX_VOLUME);

            float lvl = 0.8f;
            if (getPosition() >= 0)
                lvl = main.getShortSoundVolume(track);
            volumeSlider.setProgress((int) (MAX_VOLUME * lvl));
            volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser)
                        modelControl.volumeChanged(getPosition(), 1.0f * progress / MAX_VOLUME);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    main.saveShortSoundTrack(getPosition());
                }
            });
        }

        /**
         * Returns the View associated with this ViewHolder.
         * @return View The View associated with this ViewHolder
         */
        public View getViewHoldersView() {
            return vView;
        }

        /**
         * Set the event handler for the Solo button on a given track.
         */
        private void setUpSolo() {
            soloOff = mContext.getResources().getColor(R.color.button_material_light);
            final ToggleButton mSoloSwitch = (ToggleButton) vView.findViewById(R.id.trackSolo);
            mSoloSwitch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!modelControl.isTrackSolo(getPosition())) {
//                        mSoloButton.set( mContext.getResources().getColor(R.color.button_material_dark) );
                    } else {
//                        mSoloButton.setBackgroundColor( mContext.getResources().getColor(R.color.button_material_light) );
                    }
                    modelControl.soloTrack(getPosition());
                }
            });
        }

        /**
         * Sets the titles for tracks in RecyclerView
         * @param position The position of a track
         */
        public void setTitleView(int position) {
            vTitle.setText(main.getCurrentTrackNameAt(position));
        }

        /**
         * sets up the buttons inside a TrackView
         */
        private void setUpButtons() {
            for (int i = 0; i < effectButtons.length; i++) {
                final String name = effectButtons[i].getText().toString();
                effectButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        main.effectEditSelected(getPosition(), name);
                    }
                });
            }
        }

        /**
         * sets up the effect toggle switch
         */
        private void setUpToggle() {
            for (int i = 0; i < effectToggles.length; i++) {
                final int i_ = i;
                effectToggles[i].setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if ( isChecked ) {
                            modelControl.turnOnEffect(effectTypes[i_], getPosition());
                            effectButtons[i_].setTextColor(main.getResources().getColor(R.color.accent_material_dark));
                        } else {
                            modelControl.muteEffect(effectTypes[i_], getPosition());
                            effectButtons[i_].setTextColor(main.getResources().getColor(R.color.white));
                        }
                    }
                });
            }
        }

        /**
         * Sets up the initial toggle values and effect button state
         */
        public void setInitToggleState(int position) {
            for (int i = 0; i < effectToggles.length; i++) {
                boolean checked = main.isEffectOn(effectTypes[i], position);
                effectToggles[i].setChecked(checked);
            }
        }

        /**
         * assigns a color to a ViewHolder if one has not been previously assigned
         */
        private void setUpColorsIfNeeded(int position) {
            int colorNum = main.getTrackColor(position) % ColorWheel.NUM_COLORS;
            if (colorNum == -1)
                colorNum = (main.getNextColorNum() + main.getCurrentShortSoundId()) % ColorWheel.NUM_COLORS;

            if (mPrimaryColor == null || mSecondaryColor == null) {
                mPrimaryColor = colorWheel.getPrimaryColor(colorNum);
                mSecondaryColor = colorWheel.getSecondaryColor(colorNum);
                main.setTrackColor(position, colorNum);
            }
        }


        /**
         * The listener for when a track is clicked on
         */
        private class TrackAnimator implements View.OnClickListener  {
            @Override
            public void onClick(View v) {
                if (!modelControl.isRecording()) {
                    if (vTrackChild.getVisibility() == View.GONE) {
                        // Expand a track
                        collapseAllOtherTracks();
                        expandTrackChildView(vTrackChild);
                    } else {
                        // Close the current open track
                        collapseTrackChildView(vTrackChild, vToggle);
                    }
                }
            }
            /**
             * Collapse all other tracks.
             */
            public void collapseAllOtherTracks() {
                for(ViewHolder vh: mViews) {
                    if ( vh.vTrackChild.getVisibility() == View.VISIBLE) {
                        collapseTrackChildView(vh.vTrackChild, vh.vToggle);
                    }
                }
            }


            /**
             * Uses animation to expand the child view of a track
             * @param v The view to expand
             */
            public void expandTrackChildView(final View v) {
                v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
                final int targetHeight = screenHeight * 2 / 5;

                v.getLayoutParams().height = 0;
                v.setVisibility(View.VISIBLE);
                Animation a = new Animation()
                {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        if (interpolatedTime == 1) {
                            v.getLayoutParams().height = targetHeight;//RecyclerView.LayoutParams.MATCH_PARENT;
                        } else {
                            v.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
                        }
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
                vToggle.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_collapse));
            }

            /**
             * Uses animation to collapse the child view of a track
             * @param v The view to collapse
             */
            public void collapseTrackChildView(final View v, final ImageButton toggle) {
                Animation a = getCollapseAnimation(v);
                v.startAnimation(a);
                toggle.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_expand));
            }

            /**
             * Deletes a track from the view and notifies the appropriate model classes
             */
            public void deleteTrackView() {
                final int position = getPosition();
                main.removeShortSoundTrack(position);
                // Note: this was the only way I could get the recycler view to play with the
                // "swipe to delete". Just had to delete view so recycler wouldn't reuse it again =(
                // This kinda defeats the purpose of the RecyclerView but we probably did not need it
                // to begin with.
                ((RecyclerView)vView.getParent()).removeView( vView );
                notifyItemRemoved(position);
            }

            /**
             * Initializes a collapse track animation
             * @param v, the view to collapse
             * @return the animation that will take place on the given view
             */
            private Animation getCollapseAnimation(final View v) {
                final int initialHeight = v.getMeasuredHeight();
                Animation a = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        if(interpolatedTime == 1){
                            v.setVisibility(View.GONE);
                        } else {
                            v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        }
                        v.requestLayout();
                    }
                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };
                // 1 dp/ms
                a.setDuration((int)(initialHeight / (v.getContext().getResources().getDisplayMetrics().density)));
                return a;
            }
        }
    }
}