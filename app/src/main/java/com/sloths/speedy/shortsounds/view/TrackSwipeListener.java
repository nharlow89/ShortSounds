package com.sloths.speedy.shortsounds.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.controller.ModelControl;

/**
 * TrackSwipeListener is a helper class that takes care of the "swipe to delete" feature as well
 * as the tap to edit title. These events are based back to the controller view a SwipeToDeleteListener.
 */
public class TrackSwipeListener implements View.OnTouchListener {
    private static final String TAG = "SWIPE";
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private final int mSlop;
    private VelocityTracker mVelocityTracker;
    private View mView;
    private int mWidth;
    private float mDownX;
    private boolean mSwiping;
    private long mAnimationTime;
    private int mSwipingSlop;
    private boolean mDeleted;
    private SwipeToDeleteListener mListener;

    public TrackSwipeListener( View v, SwipeToDeleteListener listener ) {
        mView = v;
        mListener = listener;
        ViewConfiguration vc = ViewConfiguration.get(v.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 4;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mDeleted = false;
        mWidth = 1;
        mAnimationTime = 500;
    }

    /**
     * The onTouch is where we handle the click and swipe events for a track.
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( mDeleted )
            return true;
        if ( mWidth == 1 )
            mWidth = v.getWidth(); // Just grab the width once

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // The user has pressed somewhere on the track, begin the tracking.
                mDownX = event.getRawX();
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement( event );
                return false;
            case MotionEvent.ACTION_UP:  // Release of the view
                // This occurs when the user lifts their finger up.
                // 1. If we were not swiping, then it was a press and need to edit title.
                if ( !mSwiping )
                    mListener.onEditTrackTitle();
                // 2. End of a drag. Check if the track was dragged far enough (or fast enough)
                // to be an actual DELETE action.
                if ( mVelocityTracker == null )
                    break;
                float deltaX = event.getRawX() - mDownX;
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                boolean dismiss = false;
                boolean dismissRight = false;
                if ( Math.abs(deltaX) > mWidth / 1.65f && mSwiping ) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = velocityX > 0;
                }
                if ( dismiss ) {
                    // dismiss
                    mView.animate()
                            .translationX(dismissRight ? mWidth : -mWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mListener.onTrackDelete();
                                }
                            });
                    mDeleted = true;
                    return true;
                } else {
                    // cancel
                    mView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mDownX = 0;
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mSwiping = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                // The cancel event occurs when the user stops dragging a track. Here we need
                // to animate the track back to its original position.
                if ( mVelocityTracker == null )
                    break;
                if (mView != null && mSwiping) {
                    // cancel
                    mView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mDownX = 0;
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mSwiping = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // This is the event that occurs when a user is dragging the track.
                // We need to animate the track according to the users dragging.
                if ( mVelocityTracker == null )
                    break;
                mVelocityTracker.addMovement(event);
                float newDeltaX = event.getRawX() - mDownX;
                if ( Math.abs(newDeltaX) > mSlop ) {
                    mSwiping = true;
                    mSwipingSlop = (newDeltaX > 0 ? mSlop : -mSlop);
                }
                if (mSwiping) {
                    mView.setTranslationX(newDeltaX - mSwipingSlop);
                    mView.setAlpha(Math.max(0.25f, Math.min(1f,
                            1f - 2f * Math.abs(newDeltaX) / mWidth)));
                    return true;
                }

                break;
        }
        return false;
    }
//    private void confirmDelete() {
//        if (!ModelControl.instance().isPlaying() && !ModelControl.instance().isRecording()) {
//            final String name = ((TextView)mView.findViewById(R.id.track_title)).getText().toString();
//            new AlertDialog.Builder(mView.getContext())
//                    .setTitle("\tDelete track?")
//                    .setIcon(R.drawable.ic_action_mic)
//                    .setMessage("Are you sure you want to delete track" + name + "?")
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            mListener.onTrackDelete();
//                            ((ShortSoundsApplication) mView.getContext().getApplicationContext())
//                                    .showToast(name + " deleted");
//                            dialog.dismiss();
//                        }
//                    }).create().show();
//        }
//    }
}