package com.stuhorner.drawingsample;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Stu on 4/5/2016.
 */
public class GalleryOptionsDialog extends android.support.v4.app.DialogFragment {
    final static int SET_AS_CARD = 1;
    final static int EDIT = 2;
    final static int REMOVE = 3;

    private OnDialogSelectionListener callback;

    public interface OnDialogSelectionListener {
        void onDialogSelection(int position);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        callback = (OnDialogSelectionListener) getTargetFragment();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gallery_options_title)
                .setItems(R.array.gallery_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("which", Integer.toString(which));
                        callback.onDialogSelection(which + 1);
                    }
                });
        return builder.create();
    }
}
