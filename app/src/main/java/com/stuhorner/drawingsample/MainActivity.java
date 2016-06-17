package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final static int MIN_AGE_ALLOWED = 18;
    final static int HIDE_MENU = 3;
    final static int ON_CRITIQUE = 1, ON_DRAWING = 2;
    boolean isMaleOn = true, isFemaleOn = true;
    public static boolean near_me = false;
    int minAge = 18, maxAge = 70;
    int page = ON_CRITIQUE;
    DrawerLayout drawer;
    NavigationView navigationView, filterView;
    public static Firebase rootRef;
    public final static String PERSON_NAME = "com.stuhorner.buckit.PERSON_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}
        Firebase.setAndroidContext(this);
        rootRef = new Firebase("https://artery.firebaseio.com/");

        //create MyUser instance
        new MyUser(this);


        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        Log.d("UID", pref.getString("UID", "null"));
        if (pref.getString("UID", null) == null) {
            clearSavedData();
            Intent intent = new Intent(getApplicationContext(), FirstLaunchActivity.class);
            startActivityForResult(intent, 1);
        }
        else {
            MyUser.getInstance().populateUser(pref.getString("UID", pref.getString("UID", null)));
        }

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
        new MyLocationListener(this, (ImageButton)findViewById(R.id.filter_near_me), (ImageButton)findViewById(R.id.filter_public));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppTheme_PopupOverlay);
            alertDialogBuilder.setTitle(R.string.welcome_title);
            alertDialogBuilder.setMessage(R.string.welcome_body).
                    setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (page == ON_CRITIQUE) {
                super.onBackPressed();
            }
            else {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CritiqueFragment()).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}
                AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(0);
                page = ON_CRITIQUE;
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                invalidateOptionsMenu();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //default: unlock left, lock right
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.filter_view));

        page = getIntent().getIntExtra("page", page);
        getIntent().removeExtra("page");
        if (page == ON_CRITIQUE) {
            getMenuInflater().inflate(R.menu.main, menu);
            menu.findItem(R.id.action_filter).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        else if (page == ON_DRAWING) {
            getMenuInflater().inflate(R.menu.menu_drawing_top, menu);
            if (getSupportActionBar() != null) { getSupportActionBar().setTitle(""); }
            menu.findItem(R.id.action_done).getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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

        switch (id) {
            case R.id.nav_main:
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.filter_view));
                fragment = new CritiqueFragment();
                fragmentManager= getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "MAIN_FRAG").commit();
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
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = ON_DRAWING;
                invalidateOptionsMenu();
                break;
            case R.id.nav_chat:
                fragment = new ChatFragment();
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle(R.string.action_chat);}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();
                break;
            case R.id.nav_gallery:
                fragment = new GalleryFragment();
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle(R.string.action_gallery);}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();
                break;
            case R.id.nav_settings:
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle(R.string.action_settings);}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();

                break;
        }
        navigationView.setCheckedItem(R.id.nav_settings);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.closeDrawer(GravityCompat.START);
            }
        }, 0);

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
                final Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("editable", true);
                intent.putExtra("buttons_off", true);
                intent.putExtra(PERSON_NAME, MyUser.getInstance().getName());
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
        near_me.setColorFilter(getResources().getColor(R.color.lightGray));
        all.setColorFilter(getResources().getColor(R.color.colorPrimary));
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

    private void handleFilterButtonPress(final ImageButton button) {
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
                    if (MyLocationListener.hasLocation) {
                        button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        ((ImageButton) findViewById(R.id.filter_public)).setColorFilter(getResources().getColor(R.color.lightGray));
                        near_me = true;
                    }
                    else {
                        new MyLocationListener(this, button, (ImageButton) findViewById(R.id.filter_public));
                    }
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

    private void clearSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //delete gallery files
        String dir = sharedPreferences.getString(getString(R.string.directory), null);

        if (dir != null) {
            File directory = new File(dir);
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
        editor.clear();
        editor.apply();
    }

}
