package com.stuhorner.drawingsample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Stu on 1/1/2016.
 */
public class ProfileActivity extends AppCompatActivity{
    String UID;
    Button changeProfilePic;
    Toolbar toolbar;
    ImageView backdrop;
    public final static int RESULT_NO = 1, RESULT_YES = 2, GET_FROM_GALLERY = 3;
    int result = 0;
    boolean buttons_on = true;
    boolean edittable = false, editting = false;
    Firebase ref = new Firebase("https://artery.firebaseio.com/");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Firebase.setAndroidContext(this);

        UID = getIntent().getStringExtra("UID");
        buttons_on = getIntent().getBooleanExtra("buttons_off", false);
        edittable = getIntent().getBooleanExtra("editable", false);

        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();

        ImageButton noButton = (ImageButton)findViewById(R.id.p_no_button);
        ImageButton yesButton = (ImageButton)findViewById(R.id.p_yes_button);
        noButton.setColorFilter(getResources().getColor(R.color.compliment));
        yesButton.setColorFilter(getResources().getColor(R.color.yes_button_green));

        changeProfilePic = (Button)findViewById(R.id.change_picture);
        backdrop = (ImageView) findViewById(R.id.backdrop);
        if (edittable) {
            initMyProfile();
        }
        else {
            initData();
        }
        setupEdit();

        if (buttons_on) {
            if (findViewById(R.id.p_linearlayout) != null)
                findViewById(R.id.p_linearlayout).setVisibility(View.INVISIBLE);
        }
        else {
            buttonListeners(yesButton);
            buttonListeners(noButton);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (editting) {
            getMenuInflater().inflate(R.menu.menu_drawing_top, menu);
            if (menu.getItem(0) != null)
                menu.getItem(0).getIcon().setColorFilter(getResources().getColor(R.color.snow_white), PorterDuff.Mode.SRC_ATOP);
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

    private void initMyProfile() {
        if (MyUser.getInstance() != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(MyUser.getInstance().getName());
            if (MyUser.getInstance().getProfilePicture() != null) {
                String profilePicture = MyUser.getInstance().getProfilePicture();
                byte[] bytes = Base64.decode(profilePicture.getBytes(), Base64.DEFAULT);
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                backdrop.setImageBitmap(bm);
            }
        }
        else {
            initData();
        }
    }
    private void setupToolbar() {
        toolbar = (Toolbar)findViewById(R.id.p_toolbar);
        setSupportActionBar(toolbar);
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
                if (isStoragePermissionGranted()) {
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
                }
            }
        });
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("storage","Permission is granted");
                return true;
            } else {

                Log.v("storage","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("storage","Permission is granted");
            return true;
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage;
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            BitmapUploadTask task = new BitmapUploadTask(BitmapUploadTask.PROFILE_PICTURE, this);
            task.execute(getPathFromURI(selectedImage));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(getPathFromURI(selectedImage), options);
            backdrop.setImageBitmap(bitmap);
        }
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

    private void buttonListeners(final ImageButton button) {
        final Animation scaleDownYes = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDownYes.setFillAfter(true);
        final Animation scaleUpYes = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaleUpYes.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDownYes);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUpYes);
                        handleButtonPress(button);
                        onBackPressed();
                        break;
                }
                return false;
            }
        });

    }

    private void handleButtonPress(ImageButton button) {
        if (button.getId() == R.id.p_yes_button) {
            result = RESULT_YES;
        }
        else {
            result = RESULT_NO;
        }
    }

    private void initData() {
        ref.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
                }
                Object profilePicture = dataSnapshot.child("profilePicture").getValue();
                Bitmap bm;
                if (profilePicture != null) {
                    byte[] bytes = Base64.decode(profilePicture.toString().getBytes(), Base64.DEFAULT);
                    bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
                else {
                    bm = BitmapFactory.decodeResource(getResources(),R.drawable.default_background);
                }

                backdrop.setImageBitmap(bm);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void editSex(View v) {
        if (editting) {
            DialogFragment newFragment = ProfileEditDialog.newInstance(ProfileEditDialog.EDIT_GENDER);
            newFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    public void editAge(View v) {
        if (editting) {
            DialogFragment newFragment = ProfileEditDialog.newInstance(ProfileEditDialog.EDIT_AGE);
            newFragment.show(getSupportFragmentManager(), "dialog");
        }
    }
}