package com.stuhorner.drawingsample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.firebase.client.Firebase;

import java.util.Map;

public class FirstLaunchActivity extends AppCompatActivity {
    private static final int NUM_PAGES = 5;
    public static ViewPager mPager;
    Firebase rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
        Firebase.setAndroidContext(this);
        rootRef = new Firebase("https://artery.firebaseio.com/");
        PagerAdapter mPagerAdapter;
        mPager = (ViewPager) findViewById(R.id.launch_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
    }

    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
        else if (mPager.getCurrentItem() != 1){
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FirstLaunchLoginFragment();
                case 1:
                    return new FirstLaunchNameFragment();
                case 2:
                    return new FirstLaunchCreateLoginFragment();
                case 3:
                    return new FirstLaunchAddProfileFragment();
                case 4:
                    return new DrawFragment();
                default:
                    return new FirstLaunchNameFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
