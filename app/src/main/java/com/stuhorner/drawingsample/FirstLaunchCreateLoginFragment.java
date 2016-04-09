package com.stuhorner.drawingsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchCreateLoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_create_login_fragment, container, false);
        Button email = (Button) rootView.findViewById(R.id.button_email_password);
        Button facebook = (Button) rootView.findViewById(R.id.button_facebook);
        Button google = (Button) rootView.findViewById(R.id.button_google);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        return rootView;
    }

    private void nextPage() {
        FirstLaunchActivity.mPager.setCurrentItem(2);
    }
}
