package com.sloths.speedy.shortsounds.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.sloths.speedy.shortsounds.R;

/**
 * This DialogFragment inflates a dialog box to rename a ShortSound
 */
public class NoticeDialogFragment extends DialogFragment {
    EditText newTitle;
    Button okay;
    Button cancel;
    static String DialogboxTitle;

    /*
     * The interface that has an onOkay button for the MainAcitiviy to implement
     */
    public interface NoticeDialogListener {
        void onOkay(String inputText);
    }

    /*
     * Creates a new Dialog Box
     */
    public NoticeDialogFragment() {
        DialogboxTitle = "Rename ShortSound";
    }

    /*
     * Prepares the DialogBox
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rename, container);
        newTitle = (EditText) view.findViewById(R.id.newTitle);
        okay = (Button) view.findViewById(R.id.okay);
        cancel = (Button) view.findViewById(R.id.cancel);

        okay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                NoticeDialogListener activity = (NoticeDialogListener) getActivity();
                activity.onOkay(newTitle.getText().toString());
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
