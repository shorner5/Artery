package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchNameFragment extends Fragment {
    final int LOGIN_FRAGMENT = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_name_fragment, container, false);
        FirstLaunchActivity.mPager = (ViewPager) getActivity().findViewById(R.id.launch_pager);

        final EditText input_name = (EditText) rootView.findViewById(R.id.input_name);
        input_name.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        final EditText input_age = (EditText) rootView.findViewById(R.id.input_age);
        input_age.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        Button login = (Button) rootView.findViewById(R.id.login_button);
        addAnimation(login);

        input_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 1) {
                    animateValid(input_name);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        input_age.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || i == EditorInfo.IME_ACTION_NEXT) {
                    SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    if (input_name.getText().length() > 1) {
                        editor.putString("Username", input_name.getText().toString());
                        animateValid(input_name);
                    } else {
                        animateInvalid(input_name, getString(R.string.name_info));
                        return false;
                    }
                    if (input_age.getText().length() < 4 && input_age.getText().length() > 0 && Integer.parseInt(input_age.getText().toString()) > 17) {
                        editor.putInt("age", Integer.parseInt(input_age.getText().toString()));
                        animateValid(input_age);
                    } else {
                        animateInvalid(input_age, getString(R.string.min_age));
                        return false;
                    }
                    editor.apply();
                    FirstLaunchActivity.mPager.setCurrentItem(FirstLaunchActivity.mPager.getCurrentItem() + 1);
                }
                return false;
            }
        });
        return rootView;
    }

    private void animateInvalid(EditText editText, String errorText) {
        editText.hasFocus();
        editText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
        editText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        editText.setTextColor(getResources().getColor(R.color.red));
        TextView error = (TextView) getView().findViewById(R.id.nameErrorText);
        if (error != null) {
            error.setText(errorText);
            error.setVisibility(View.VISIBLE);
        }
    }

    private void animateValid(EditText editText) {
        editText.setTextColor(getResources().getColor(android.R.color.white));
        editText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        TextView error = (TextView) getView().findViewById(R.id.nameErrorText);
        if (error != null) {
            error.setVisibility(View.INVISIBLE);
        }
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
                        Log.d("her", "heeere");
                        FirstLaunchActivity.mPager.setCurrentItem(FirstLaunchActivity.mPager.getCurrentItem() - 1);
                        break;
                }
                return false;
            }
        });
    }
}
