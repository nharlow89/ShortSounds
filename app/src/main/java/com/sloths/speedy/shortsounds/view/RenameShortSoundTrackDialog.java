package com.sloths.speedy.shortsounds.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * This DialogFragment inflates a dialog box to rename a ShortSound.
 */
public class RenameShortSoundTrackDialog extends DialogFragment {
    static String DialogboxTitle;
    private int track;


    public void setTrack(int track) {
        this.track = track;
    }


    /**
     * The interface that has an onRenameShortSound button for the MainActivity to implement.
     */
    public interface RenameShortSoundTrackDialogListener {
        void onRenameShortSoundTrack(String inputText, int track);
    }

    /**
     * Creates a new Dialog Box.
     */
    public RenameShortSoundTrackDialog() {
        DialogboxTitle = "Rename ShortSound track";
    }


    /**
     * Prepares the DialogBox.
     * @param inflater The LayoutInflater
     * @param container The ViewGroup
     * @param saveInstanceState The Bundle with the saved instance
     * @return the View associated with the DialogBox.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rename, container);
        final EditText newTitle = (EditText) view.findViewById(R.id.newTitle);
        Button okay = (Button) view.findViewById(R.id.okay);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        okay.setOnClickListener(new View.OnClickListener() {
            final EditText trackName = newTitle;
            public void onClick(View view) {
                RenameShortSoundTrackDialogListener activity = (RenameShortSoundTrackDialogListener) getActivity();
                activity.onRenameShortSoundTrack(trackName.getText().toString(), track);
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });
        newTitle.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle(DialogboxTitle);
        return view;
    }
}
