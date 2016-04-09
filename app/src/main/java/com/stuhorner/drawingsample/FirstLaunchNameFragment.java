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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.Key;

/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchNameFragment extends Fragment {
    Button login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_name_fragment, container, false);
        FirstLaunchActivity.mPager = (ViewPager) getActivity().findViewById(R.id.launch_pager);

        final EditText input_name = (EditText) rootView.findViewById(R.id.input_name);
        input_name.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);


        login = (Button) rootView.findViewById(R.id.login_button);
        input_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    login.setText(getString(R.string.next));
                } else {
                    login.setText(getString(R.string.login));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login.getText().equals(getString(R.string.login))) {
                    //launch login
                } else {
                    SharedPreferences.Editor pref = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    pref.putString("Username", input_name.getText().toString());
                    pref.apply();
                    FirstLaunchActivity.mPager.setCurrentItem(FirstLaunchActivity.mPager.getCurrentItem() + 1);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        return rootView;
    }
}
