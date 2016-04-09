package com.stuhorner.drawingsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchAddProfileFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_create_profile, container, false);
        Button skip = (Button) rootView.findViewById(R.id.button_skip);
            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirstLaunchActivity.mPager.setCurrentItem(3);
                    Intent intent = new Intent();
                    intent.putExtra("firstLaunchDraw", true);

                }
            });

        return rootView;
    }
}
