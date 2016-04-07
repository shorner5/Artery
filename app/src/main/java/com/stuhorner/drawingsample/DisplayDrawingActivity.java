package com.stuhorner.drawingsample;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DisplayDrawingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawing);

        ImageView img =(ImageView) findViewById(R.id.fullscreen_image);
        final String path = getIntent().getStringExtra("edit_image");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        img.setImageBitmap(bitmap);
        Button setAsCard = (Button) findViewById(R.id.set_as_card);
        Button edit = (Button) findViewById(R.id.edit);
        Button remove = (Button) findViewById(R.id.remove);
        setTransitions();

        final Intent returnIntent = new Intent();
        returnIntent.putExtra("edit_image", path);
        setAsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(GalleryOptionsDialog.SET_AS_CARD, returnIntent);
                Snackbar.make(view, R.string.card_set, Snackbar.LENGTH_SHORT).show();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(GalleryOptionsDialog.EDIT, returnIntent);
                onBackPressed();
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(GalleryOptionsDialog.REMOVE, returnIntent);
                onBackPressed();
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    @TargetApi(21)
    private void setTransitions() {
        Transition slide = new Slide();
        slide.excludeTarget(android.R.id.statusBarBackground, true);
        slide.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
