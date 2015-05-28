package com.sloths.speedy.shortsounds.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sloths.speedy.shortsounds.R;


/**
 * This fragment inflates a layout with two Floating Action Buttons and acts as a listener to
 * changes on them.
 */
public class FloatingActionButtonBasicFragment extends Fragment implements FloatingActionButton.OnCheckedChangeListener{

    private final static String TAG = "FloatingActionButtonBasicFragment";
    private FloatingActionButton mActionButton;
    private OnFragmentLoadedListener listener;

    /**
     * sets up FAB fragment view
     * @param inflater the LayoutInflator
     * @param container the ViewGroup
     * @param savedInstanceState the Bundle
     * @return The root view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fab_layout, container, false);

        // Make this {@link Fragment} listen for changes in both FABs.
        mActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        mActionButton.setOnCheckedChangeListener(this);
        Log.d("DEBUG", "Instantiated ActionButton " + mActionButton);

        // Notify the listener
        if ( listener != null )
            listener.didLoad();
        return rootView;
    }


    /**
     * Fetch the action button associated with this fragment.
     * @return FloatingActionButton
     */
    public FloatingActionButton getActionButton() {
        return this.mActionButton;
    }

    /**
     * action for when FAB is changed
     * @param fabView   The FAB view whose state has changed.
     * @param isChecked The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(FloatingActionButton fabView, boolean isChecked) {
        // When a FAB is toggled, log the action.
        switch (fabView.getId()){
            case R.id.fab_1:
                //Log.d(TAG, String.format("FAB 1 was %s.", isChecked ? "checked" : "unchecked"));
                break;
            default:
                break;
        }
    }

    /**
     * Set the listener for the Fragment loading.
     * @param listener the listener for the Fragment
     */
    public void setOnLoadListener( OnFragmentLoadedListener listener ) {
        this.listener = listener;
    }

    /**
     * Listener for returning the action button once the fragment has loaded.
     */
    public interface OnFragmentLoadedListener {
        void didLoad();
    }
}
