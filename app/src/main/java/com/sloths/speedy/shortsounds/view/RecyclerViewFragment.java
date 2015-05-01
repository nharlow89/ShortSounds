package com.sloths.speedy.shortsounds.view;

/**
 * Created by joel on 4/25/2015.
 */
import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.util.List;


public class RecyclerViewFragment extends Fragment implements RecyclerViewAdapter.RVListener {

    public static final String ARG_SOUND_NUMBER = "sound_number";
    private static final String TAG = "RecyclerViewFragment";
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected RecyclerViewAdapter mAdapter;
    protected String[] trackNames;
    private ShortSound sound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // grab the root view of the layout for recycler view
        View rootView = inflater.inflate(R.layout.recycler_view_frag, container, false);
        // grab the RecyclerView component from the layout
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        // create an mLayoutManager which is required for RecyclerViews
        mLayoutManager = new LinearLayoutManager(getActivity());
        // set the LayoutManager for the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        // set the adapter for the RecyclerView, passing in the data
        mAdapter = new RecyclerViewAdapter(trackNames, this);
        mRecyclerView.setAdapter(mAdapter);
//
//        mRecyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
//            @Override
//            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
//
//            }
//        });
        return rootView;

    }


    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
//        String sound = getResources().getStringArray(R.array.shortsounds_array)[sound_num];
//        getActivity().setTitle(sound);

        // Populate array of tracks
//        List<String> trackTitles = Arrays.asList(getResources().getStringArray(R.array.track_array));
        int sound_num = getArguments().getInt(ARG_SOUND_NUMBER);
        String[] trackTitles = getTracks(sound_num);
        trackNames = trackTitles;
    }

    private String[] getTracks(int position) {
        List<ShortSound> sounds = ShortSound.getAll();
        ShortSound thisSound = sounds.get(position);
        List<ShortSoundTrack> tracks = thisSound.getTracks();
        String[] tracksNames = new String[tracks.size()];
        for (int i = 0; i < tracks.size(); i++) {
            tracksNames[i] = tracks.get(i).getTitle();
        }
        return tracksNames;
    }



    @Override
    public void onButtonClicked(View v, int track, String effect) {
        loadEffectDialog(track, effect);
    }

    private void loadEffectDialog(final int track, final String effect) {
        final AlertDialog.Builder imageDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.effect_view,
                (ViewGroup) getActivity().findViewById(R.id.parentEffectPanel));

        ((TextView) layout.findViewById(R.id.effectNameTitle)).setText(effect);
        imageDialog.setView(layout);

        final AlertDialog dialog = imageDialog.create();

        layout.findViewById(R.id.saveEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showToast("Effects saved", Toast.LENGTH_SHORT);
            }
        });
        layout.findViewById(R.id.cancelEffectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Effect cleared", Toast.LENGTH_SHORT);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showToast(String text, int length) {
        Toast toast = Toast.makeText(getActivity(), text, length);
        LinearLayout layout =(LinearLayout)toast.getView();
        TextView textView = ((TextView)layout.getChildAt(0));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }
}
