package com.stuhorner.drawingsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

/**
 * Created by Stu on 1/1/2016.
 */
public class ProfileActivity extends AppCompatActivity{
    String person_name;
    ImageButton noButton, yesButton;
    public final static int RESULT_NO = 1, RESULT_YES = 2;
    int result;
    boolean buttons_on = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        person_name = getIntent().getStringExtra(MainActivity.PERSON_NAME);
        buttons_on = getIntent().getBooleanExtra("buttons_off", false);
        result = 0;

        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();

        noButton = (ImageButton)findViewById(R.id.p_no_button);
        yesButton = (ImageButton)findViewById(R.id.p_yes_button);

        if (buttons_on) {
            noButton.setVisibility(View.INVISIBLE);
            yesButton.setVisibility(View.INVISIBLE);
        }
        else {
            buttonListeners();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.p_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(person_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void setupViewPager(){
        final ViewPager viewPager = (ViewPager) findViewById(R.id.p_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.p_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    private void setupCollapsingToolbar(){
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);
        //TODO: set as user profile picture
    }
    private void setupViewPager(ViewPager viewPager) {
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        adapter.add(new ProfileDetailsFragment(), getString(R.string.title_activity_profile));
        adapter.add(new ProfileGalleryFragment(), getString(R.string.gallery));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        setResult(result);
        super.onBackPressed();
        if (result == RESULT_NO)
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
        else
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }

    private void buttonListeners(){
        final Animation scaleDownYes = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate_down);
        scaleDownYes.setFillAfter(true);
        final Animation scaleUpYes = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_up);
        scaleUpYes.setFillAfter(true);

        yesButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        yesButton.startAnimation(scaleDownYes);
                        break;
                    case MotionEvent.ACTION_UP:
                        yesButton.startAnimation(scaleUpYes);
                        result = RESULT_YES;
                        onBackPressed();
                        break;
                }
                return false;
            }
        });

        final Animation scaleDownNo = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_down);
        scaleDownNo.setFillAfter(true);
        final Animation scaleUpNo = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_up);
        scaleUpNo.setFillAfter(true);

        noButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        noButton.startAnimation(scaleDownNo);
                        break;
                    case MotionEvent.ACTION_UP:
                        noButton.startAnimation(scaleUpNo);
                        result = RESULT_NO;
                        onBackPressed();
                        break;
                }
                return false;
            }
        });
    }
}