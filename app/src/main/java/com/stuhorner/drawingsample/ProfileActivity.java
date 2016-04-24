package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.firebase.client.Firebase;

/**
 * Created by Stu on 1/1/2016.
 */
public class ProfileActivity extends AppCompatActivity{
    String person_name;
    ImageButton noButton, yesButton;
    Button changeProfilePic;
    Toolbar toolbar;
    ImageView backdrop;
    public final static int RESULT_NO = 1, RESULT_YES = 2, GET_FROM_GALLERY = 3;
    int result;
    boolean buttons_on = true;
    boolean edittable = false, editting = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Firebase.setAndroidContext(this);

        person_name = getIntent().getStringExtra(MainActivity.PERSON_NAME);

        buttons_on = getIntent().getBooleanExtra("buttons_off", false);
        edittable = getIntent().getBooleanExtra("editable", false);
        result = 0;

        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();

        noButton = (ImageButton)findViewById(R.id.p_no_button);
        yesButton = (ImageButton)findViewById(R.id.p_yes_button);
        changeProfilePic = (Button)findViewById(R.id.change_picture);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        if (edittable) {
            getMyProfilePicture();
        }
        setupEdit();

        if (buttons_on) {
            noButton.setVisibility(View.INVISIBLE);
            yesButton.setVisibility(View.INVISIBLE);
        }
        else {
            buttonListeners();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (editting) {
            getMenuInflater().inflate(R.menu.menu_drawing_top, menu);
        }
        else if (edittable) {
            getMenuInflater().inflate(R.menu.menu_profile, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            //edit profile
            editting = true;
            changeProfilePic.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
        }
        if (id == R.id.action_done) {
            editting = false;
            changeProfilePic.setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getMyProfilePicture() {
        if (User.getInstance().getProfilePicture() != null) {
            String profilePicture = User.getInstance().getProfilePicture();
            byte[] bytes = Base64.decode(profilePicture.getBytes(), Base64.DEFAULT);
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            backdrop.setImageBitmap(bm);

            //first time, save picture to device
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            editor.putString(getString(R.string.profile_picture), User.getInstance().getProfilePicturePath());
            editor.apply();
        }
        else {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            String bitmapURL = sharedPref.getString(getString(R.string.profile_picture), null);
            if (bitmapURL != null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    Bitmap bitmap = BitmapFactory.decodeFile(bitmapURL, options);
                    backdrop.setImageBitmap(bitmap);
                } catch (Exception e) {
                    backdrop.setImageResource(R.drawable.example_profilepic);
                }
            }
            else {
                backdrop.setImageResource(R.drawable.example_profilepic);

            }
        }
    }
    private void setupToolbar() {
        toolbar = (Toolbar)findViewById(R.id.p_toolbar);
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
    }
    private void setupViewPager(ViewPager viewPager) {
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        adapter.add(new ProfileDetailsFragment(), getString(R.string.title_activity_profile));
        adapter.add(new ProfileGalleryFragment(), getString(R.string.gallery));
        viewPager.setAdapter(adapter);
    }

    private void setupEdit() {
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = null;
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            BitmapUploadTask task = new BitmapUploadTask(BitmapUploadTask.PROFILE_PICTURE, this);
            task.execute(getPathFromURI(selectedImage));
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(getPathFromURI(selectedImage), options);
        backdrop.setImageBitmap(bitmap);
    }

    private String getPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getApplicationContext().getContentResolver().query(uri,proj,null,null,null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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