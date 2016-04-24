package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchAddProfileFragment extends Fragment {
    EditText editText;
    boolean genderSelected = false;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_add_profile, container, false);
        Intent intent = new Intent();
        intent.putExtra("firstLaunchDraw", true);
        getActivity().setIntent(intent);

        final Button skip = (Button) rootView.findViewById(R.id.button_skip);
        Button addPicture = (Button) rootView.findViewById(R.id.button_profile_picture);
        Button genderMale = (Button) rootView.findViewById(R.id.button_gender_male);
        Button genderFemale = (Button) rootView.findViewById(R.id.button_gender_female);
        Button genderNeither = (Button) rootView.findViewById(R.id.button_gender_neither);
        addAnimation(skip);
        addAnimation(addPicture);
        addAnimation(genderMale);
        addAnimation(genderFemale);
        addAnimation(genderNeither);

        editText = (EditText) rootView.findViewById(R.id.input_profile);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    editText.setGravity(Gravity.START | Gravity.TOP);
                    skip.setText(getString(R.string.next));
                } else {
                    // no text entered. Center the hint text.
                    editText.setGravity(Gravity.CENTER);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return rootView;
    }

    private void addAnimation(final Button button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down_small);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getContext(),R.anim.scale_up_small);
        scaleUp.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUp);
                        handleButtonPress(button);
                        break;
                }
                return false;
            }
        });
    }

    private void handleButtonPress(final Button button) {
        switch(button.getId()) {
            case R.id.button_skip:
                if (genderSelected) {
                    if (editText.getText().length() > 0) {
                        User.getInstance().setProfileText(editText.getText().toString());
                    }
                    FirstLaunchActivity.mPager.setCurrentItem(FirstLaunchActivity.mPager.getCurrentItem() + 1);
                }
                else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(R.string.gender_error);
                    alertDialogBuilder.setMessage(R.string.gender_error_detial).
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialogBuilder.create().show();
                }
                break;
            case R.id.button_profile_picture:
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), ProfileActivity.GET_FROM_GALLERY);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        button.setText(getString(R.string.select_different_picture));
                    }
                }, 1000);
                break;
            case R.id.button_gender_male:
                clearGender();
                button.setTextColor(getResources().getColor(R.color.green));
                User.getInstance().setGender(User.MALE);
                break;
            case R.id.button_gender_female:
                clearGender();
                button.setTextColor(getResources().getColor(R.color.green));
                User.getInstance().setGender(User.FEMALE);
                break;
            case R.id.button_gender_neither:
                clearGender();
                button.setTextColor(getResources().getColor(R.color.green));
                User.getInstance().setGender(User.NEITHER);
                break;
        }
    }

    private void clearGender() {
        Button genderMale = (Button) getView().findViewById(R.id.button_gender_male);
        Button genderFemale = (Button) getView().findViewById(R.id.button_gender_female);
        Button genderNeither = (Button) getView().findViewById(R.id.button_gender_neither);
        genderMale.setTextColor(getResources().getColor(R.color.lightGray));
        genderFemale.setTextColor(getResources().getColor(R.color.lightGray));
        genderNeither.setTextColor(getResources().getColor(R.color.lightGray));
        genderSelected = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = null;
        if (requestCode == ProfileActivity.GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();

            BitmapUploadTask task = new BitmapUploadTask(BitmapUploadTask.PROFILE_PICTURE);
            task.execute(getPathFromURI(selectedImage));
        }
    }

    private String getPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContext().getContentResolver().query(uri,proj,null,null,null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
