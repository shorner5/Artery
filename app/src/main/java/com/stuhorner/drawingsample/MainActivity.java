package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import com.appyvet.rangebar.RangeBar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final static int MIN_AGE_ALLOWED = 18;
    final static int HIDE_MENU = 3;
    final static int ON_CRITIQUE = 1;
    final static int ON_DRAWING = 2;
    boolean isMaleOn = true;
    boolean isFemaleOn = true;
    boolean near_me = true;
    int minAge = 18;
    int maxAge = 70;
    int page = ON_CRITIQUE;
    DrawerLayout drawer;
    NavigationView navigationView, filterView;

    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}

        Fragment fragment = new CritiqueFragment();
        android.support.v4.app.FragmentManager fragmentManager= getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (page == ON_DRAWING) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.nav_view));
            }
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // this disables the animation
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        filterView = (NavigationView) findViewById(R.id.filter_view);
        initFilter();
        initNavHeader();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (page == ON_CRITIQUE) {
            getMenuInflater().inflate(R.menu.main, menu);
            menu.findItem(R.id.action_filter).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
        else if (page == ON_DRAWING) {
            getMenuInflater().inflate(R.menu.menu_drawing_top, menu);
            menu.findItem(R.id.action_done).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            handleFilter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(21)
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        android.support.v4.app.FragmentManager fragmentManager;
        AppBarLayout appBarLayout;

        //default: unlock left, lock right
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.filter_view));

        switch (id) {
            case R.id.nav_main:
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.filter_view));
                fragment = new CritiqueFragment();
                fragmentManager= getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(0);
                page = ON_CRITIQUE;
                invalidateOptionsMenu();
                break;
            case R.id.nav_draw:
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new DrawFragment();
                fragmentManager= getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = ON_DRAWING;
                invalidateOptionsMenu();
                break;
            case R.id.nav_chat:
                fragment = new ChatFragment();
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle("Chat");}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_settings:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleFilter() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawer.openDrawer(filterView);
    }

    private void initNavHeader() {
        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra(PERSON_NAME, "Stuart Horner");
                intent.putExtra("buttons_off", true);
                startActivity(intent);
            }
        });
    }

    private void initFilter() {
        initSex();
        initLocation();
        initSlider();
    }

    private void initSex() {
        final ImageButton male = (ImageButton) findViewById(R.id.filter_male);
        final ImageButton female = (ImageButton) findViewById(R.id.filter_female);
        male.setColorFilter(getResources().getColor(R.color.colorPrimary));
        female.setColorFilter(getResources().getColor(R.color.colorPrimary));
        addAnimation(male);
        addAnimation(female);
    }
    private void initLocation() {
        final ImageButton near_me = (ImageButton) findViewById(R.id.filter_near_me);
        final ImageButton all = (ImageButton) findViewById(R.id.filter_public);
        near_me.setColorFilter(getResources().getColor(R.color.colorPrimary));
        all.setColorFilter(getResources().getColor(R.color.lightGray));
        addAnimation(near_me);
        addAnimation(all);
    }
    private void initSlider() {
        RangeBar rangeBar = (RangeBar) findViewById(R.id.rangeBar);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                minAge = leftPinIndex + MIN_AGE_ALLOWED;
                maxAge = rightPinIndex + MIN_AGE_ALLOWED;
            }
        });
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale_up);
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
                        handleFilterButtonPress(button);
                        break;
                }
                return false;
            }
        });
    }

    private void handleFilterButtonPress(ImageButton button) {
        int id = button.getId();
        switch (id){
            case R.id.filter_male:
                if (isMaleOn) {
                    button.setColorFilter(getResources().getColor(R.color.lightGray));
                    isMaleOn = false;
                } else {
                    button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    isMaleOn = true;
                }
                break;
            case R.id.filter_female:
                if (isFemaleOn) {
                    button.setColorFilter(getResources().getColor(R.color.lightGray));
                    isFemaleOn = false;
                } else {
                    button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    isFemaleOn = true;
                }
                break;
            case R.id.filter_near_me:
                if (!near_me) {
                    button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    ((ImageButton)findViewById(R.id.filter_public)).setColorFilter(getResources().getColor(R.color.lightGray));
                    near_me = true;
                }
                break;
            case R.id.filter_public:
                if (near_me) {
                    button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    ((ImageButton)findViewById(R.id.filter_near_me)).setColorFilter(getResources().getColor(R.color.lightGray));
                    near_me = false;
                }
                break;
        }
    }

}
