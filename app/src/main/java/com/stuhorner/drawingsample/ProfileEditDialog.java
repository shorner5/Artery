package com.stuhorner.drawingsample;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by Stuart on 7/13/2016.
 */
public class ProfileEditDialog extends DialogFragment {
    static final int EDIT_AGE = 0;
    static final int EDIT_GENDER = 1;
    int selection;

    static ProfileEditDialog newInstance(int which) {
        ProfileEditDialog dialog = new ProfileEditDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("which", which);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selection = getArguments().getInt("which");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_edit_frag, container, false);
        final NumberPicker picker = (NumberPicker) v.findViewById(R.id.p_numberPicker);
        Button button = (Button) v.findViewById(R.id.p_edit_button);

        if (selection == EDIT_AGE) {
            getDialog().setTitle(getString(R.string.set_age));
            picker.setMinValue(MainActivity.MIN_AGE_ALLOWED);
            picker.setMaxValue(100);
            picker.setValue(MyUser.getInstance().getAge());
        } else if (selection == EDIT_GENDER) {
            getDialog().setTitle(getString(R.string.set_gender));
            picker.setMinValue(0);
            picker.setMaxValue(2);
            picker.setDisplayedValues( new String[] { getString(R.string.gender_neither), getString(R.string.gender_female), getString(R.string.gender_male)} );
            picker.setValue(2 - MyUser.getInstance().getGender());
        }
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setWrapSelectorWheel(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewValue(picker.getValue());
                dismiss();
            }
        });

        return v;
    }

    private void setNewValue(int newValue) {
        if (selection == EDIT_AGE) {
            MyUser.getInstance().setAge(newValue);
        }
        else if (selection == EDIT_GENDER) {
            // genders are listed backwards, so "2", which is "male", should be "0"
            MyUser.getInstance().setGender(2 - newValue);
        }
    }
}

