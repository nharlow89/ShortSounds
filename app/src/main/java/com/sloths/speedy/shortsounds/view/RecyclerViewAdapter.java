package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */

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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.controller.ModelControl;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.Effect;

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
    public static final int MAX_VOLUME = 100;
    private final int screenWidth;
    private final int screenHeight;


    /**
     * Constructor for the RecyclerViewAdapter.
     * @param context The Context associated with this RecyclerViewAdapter.
     */
    public RecyclerViewAdapter(Context context) {
        this.mContext = context;
        modelControl = ModelControl.instance();
//        modelControl = ((MainActivity) context).getModelControl();
        mViews = new ArrayList<>();
        Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        screenHeight = p.y;
        screenWidth = p.x;
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
        viewHolder.setUpVolume(viewHolder.vView, position);
        viewHolder.setInitToggleState(position);
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
        private View vView;
        private int mPrimaryColor;
        private int mSecondaryColor;
        int soloOff;
        // Switches
        private CrossView[] xViews;
        private Effect.Type[] effectTypes;

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
            vTrackChild.setVisibility(View.GONE);
            xViews = new CrossView[]{((CrossView) v.findViewById(R.id.eq_switch)),
                        ((CrossView) v.findViewById(R.id.reverb_switch))};
            effectTypes = new Effect.Type[]{Effect.Type.EQ, Effect.Type.REVERB};

            // set up solo button
            setUpSolo();

            // init buttons
            setUpButtons(new Button[]{((Button) v.findViewById(R.id.eq_button)),
                    ((Button) v.findViewById(R.id.reverb_button))});
            // perform setup
            setUpToggle();
        }



        private void setUpVolume(final View v, final int track) {
            SeekBar volumeSlider = (SeekBar) v.findViewById(R.id.volumeSlider);
            volumeSlider.setMax(MAX_VOLUME);

            float lvl = 0.8f;
            if (getPosition() >= 0)
                lvl = ((MainActivity) mContext).getShortSoundVolume(getPosition());
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
                    ((MainActivity) mContext).saveShortSoundTrack(track);
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
         * Set the event handler for the Play button on a given track.
         */
        private void setUpSolo() {
            soloOff = mContext.getResources().getColor(R.color.button_material_light);
            final Button mSoloButton = (Button) vView.findViewById(R.id.trackSolo);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSoloButton.getLayoutParams();
            int size = Math.max (params.width, params.height);
            mSoloButton.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            mSoloButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!modelControl.isTrackSolo(getPosition()))
                        mSoloButton.setBackgroundColor(mSecondaryColor);
                    else
                        mSoloButton.setBackgroundColor(soloOff);
                    modelControl.soloTrack(getPosition());
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
            for (int i = 0; i < bs.length; i++) {
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
        private void setUpToggle() {
            for (int i = 0; i < xViews.length; i++) {
                final int i_ = i;
                xViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((MainActivity)mContext).getEffectChecked(effectTypes[i_], getPosition())) {
                            xViews[i_].cross();
                            modelControl.muteEffect(effectTypes[i_], getPosition());
                        } else {
                            xViews[i_].plus();
                            modelControl.turnOnEffect(effectTypes[i_], getPosition());
                        }
                    }
                });
            }
        }

        /**
         * Sets up the initial toggle values, pulled from the backend model
         * @param track
         */
        public void setInitToggleState(int track) {
            MainActivity main = (MainActivity) mContext;
            for (int i = 0; i < xViews.length; i++) {
                boolean checked = main.getEffectChecked(effectTypes[i], getPosition());
                xViews[i].setInitialState(checked);
            }
        }

        /**
         * The listener for when a track is clicked on
         */
        private class TrackListener implements View.OnClickListener  {
            @Override
            public void onClick(View v) {
                if (!modelControl.isRecording()) {
                    if (vTrackChild.getVisibility() == View.GONE) {
                        // Expand a track
                        collapseAllOtherTracks();
                        expandTrackChildView(vTrackChild);
                    } else {
                        // Close the current open track
                        collapseTrackChildView(vTrackChild);
                    }
                }
            }
            /**
             * Collapse all other tracks.
             */
            public void collapseAllOtherTracks() {
                for(ViewHolder vh: mViews) {
                    if ( vh.vTrackChild.getVisibility() == View.VISIBLE) {
                        collapseTrackChildView(vh.vTrackChild);
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
                        v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
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
                        } else {
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
        }
    }
}