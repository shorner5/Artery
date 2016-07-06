package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final static int MIN_AGE_ALLOWED = 18;
    final static int HIDE_MENU = 3;
    final static int ON_CRITIQUE = 1, ON_DRAWING = 2;
    boolean isMaleOn, isFemaleOn, filtersChanged = false;
    public static boolean near_me = false;
    int minAge = 18, maxAge = 70;
    int page = ON_CRITIQUE;
    DrawerLayout drawer;
    NavigationView navigationView, filterView;
    public static Firebase rootRef;
    public final static int PERMISSION_LOCATION = 1, PERMISSION_STORAGE = 2;
    CritiqueFragment fragment;

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
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("near_me", true);
        editor.commit();
        Log.d("UID", pref.getString("UID", "null"));
        if (pref.getString("UID", null) == null) {
            clearSavedData();
            Intent intent = new Intent(getApplicationContext(), FirstLaunchActivity.class);
            //startActivityForResult(intent, 1);
            startActivity(intent);
            finish();
        }
        else {
            MyUser.getInstance().populateUser(pref.getString("UID", pref.getString("UID", null)));
        }

        fragment = new CritiqueFragment();
        android.support.v4.app.FragmentManager fragmentManager= getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                if (view.getId() == R.id.filter_view) {
                    if (filtersChanged) {
                        Log.d("replaced", "critique");
                        fragment = new CritiqueFragment();
                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    }
                    filtersChanged = false;
                }
                super.onDrawerClosed(view);
                if (page == ON_DRAWING) {
                    Log.d("drawer", "ON_DRAWING_LOCK");
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                if (drawerView.getId() == R.id.filter_view) {
                    initFilter();
                }
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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
        else {
            new MyLocationListener(this, (ImageButton) findViewById(R.id.filter_near_me), (ImageButton) findViewById(R.id.filter_public), fragment);
        }
        //start notification activity
        Intent serviceIntent = new Intent(MainActivity.this, FirebaseNotifService.class);
        startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResult) {
        if (requestCode == PERMISSION_LOCATION && grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            new MyLocationListener(this, (ImageButton) findViewById(R.id.filter_near_me), (ImageButton) findViewById(R.id.filter_public), fragment);
        }
        if (requestCode == PERMISSION_STORAGE)
            ((DrawFragment)getSupportFragmentManager().findFragmentByTag("DRAW_FRAG")).saveImage(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", Integer.toString(requestCode));
        if (requestCode == 1) {

            new MyLocationListener(this, (ImageButton)findViewById(R.id.filter_near_me), (ImageButton)findViewById(R.id.filter_public), fragment);

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.filter_view));
        }
        else if (page == ON_DRAWING) {
            getMenuInflater().inflate(R.menu.menu_drawing_top, menu);
            if (getSupportActionBar() != null) { getSupportActionBar().setTitle(""); }
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

        switch (id) {
            case R.id.nav_main:
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, findViewById(R.id.filter_view));
                fragment = new CritiqueFragment();
                fragmentManager= getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "MAIN_FRAG").commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle("");}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    appBarLayout.setElevation(0);
                page = ON_CRITIQUE;
                invalidateOptionsMenu();
                break;
            case R.id.nav_draw:
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                fragment = new DrawFragment();
                fragmentManager= getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "DRAW_FRAG").commit();
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();
                break;
            case R.id.nav_settings:
                fragment  = new SettingsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                if (getSupportActionBar() != null) {getSupportActionBar().setTitle(R.string.action_settings);}
                appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    appBarLayout.setElevation(8);
                page = HIDE_MENU;
                invalidateOptionsMenu();

                break;
        }
        navigationView.setCheckedItem(R.id.nav_settings);

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
                intent.putExtra("UID", MyUser.getInstance().getUID());
                startActivity(intent);
            }
        });

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if (pref.getString("UID", null) != null) {
            rootRef.child("users").child(pref.getString("UID", null)).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView title = (TextView) findViewById(R.id.header_username);
                    if (title != null) {
                        title.setText(dataSnapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
    }

    private void initFilter() {
        initSex();
        initLocation();
        initSlider();
    }

    private void initSex() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        isMaleOn = pref.getBoolean("isMaleOn", true);
        isFemaleOn = pref.getBoolean("isFemaleOn", true);
        final ImageButton male = (ImageButton) findViewById(R.id.filter_male);
        final ImageButton female = (ImageButton) findViewById(R.id.filter_female);
        addColor(male, isMaleOn);
        addColor(female, isFemaleOn);
        addAnimation(male);
        addAnimation(female);
    }
    private void initLocation() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        near_me = pref.getBoolean("near_me", false);
        final ImageButton near_me_button = (ImageButton) findViewById(R.id.filter_near_me);
        final ImageButton all = (ImageButton) findViewById(R.id.filter_public);
        addColor(near_me_button, near_me);
        addColor(all, !near_me);
        addAnimation(near_me_button);
        addAnimation(all);
    }

    private void addColor(ImageButton button, boolean on) {
        if (on) {
            button.setColorFilter(getResources().getColor(R.color.colorPrimary));
        }
        else {
            button.setColorFilter(getResources().getColor(R.color.lightGray));
        }
    }

    private void initSlider() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        RangeBar rangeBar = (RangeBar) findViewById(R.id.rangeBar);
        rangeBar.setRangePinsByIndices(pref.getInt("minAge", 18) - MIN_AGE_ALLOWED, pref.getInt("maxAge", 70) - MIN_AGE_ALLOWED);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                if (minAge != leftPinIndex + MIN_AGE_ALLOWED || maxAge != rightPinIndex + MIN_AGE_ALLOWED) {
                    filtersChanged = true;
                    minAge = leftPinIndex + MIN_AGE_ALLOWED;
                    maxAge = rightPinIndex + MIN_AGE_ALLOWED;
                    editor.putInt("minAge", minAge);
                    editor.putInt("maxAge", maxAge);
                    editor.apply();
                }
            }
        });
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
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
        SharedPreferences.Editor pref = getSharedPreferences("data", MODE_PRIVATE).edit();
        int id = button.getId();
        switch (id){
            case R.id.filter_male:
                isMaleOn = (!isMaleOn);
                pref.putBoolean("isMaleOn", isMaleOn);
                pref.apply();
                addColor(button, isMaleOn);
                filtersChanged = true;
                break;
            case R.id.filter_female:
                filtersChanged = true;
                isFemaleOn = (!isFemaleOn);
                pref.putBoolean("isFemaleOn", isFemaleOn);
                pref.apply();
                addColor(button, isFemaleOn);
                break;
            case R.id.filter_near_me:
                if (!near_me) {
                    filtersChanged = true;
                    if (MyLocationListener.hasLocation) {
                        button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        ((ImageButton) findViewById(R.id.filter_public)).setColorFilter(getResources().getColor(R.color.lightGray));
                        near_me = true;
                        pref.putBoolean("near_me", true);
                        pref.apply();
                    }
                    else {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        }
                        else {
                            new MyLocationListener(this, button, (ImageButton) findViewById(R.id.filter_public), (CritiqueFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame));
                        }
                    }
                }
                break;
            case R.id.filter_public:
                if (near_me) {
                    filtersChanged = true;
                    button.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    ((ImageButton) findViewById(R.id.filter_near_me)).setColorFilter(getResources().getColor(R.color.lightGray));
                    near_me = false;
                    pref.putBoolean("near_me", false);
                    pref.apply();
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
