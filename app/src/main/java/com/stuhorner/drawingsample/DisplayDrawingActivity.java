package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Button;
import android.widget.ImageView;

public class DisplayDrawingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_drawing);

        ImageView img =(ImageView) findViewById(R.id.fullscreen_image);
        final String path = getIntent().getStringExtra("edit_image");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        img.setImageBitmap(bitmap);

        setTransitions();
        setListeners(path);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setListeners(String path) {
        Button setAsCard = (Button) findViewById(R.id.set_as_card);
        Button edit = (Button) findViewById(R.id.edit);
        Button remove = (Button) findViewById(R.id.remove);
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("edit_image", path);
        setAsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(GalleryOptionsDialog.SET_AS_CARD, returnIntent);
                onBackPressed();
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
                Log.d("result:", Integer.toString(GalleryOptionsDialog.REMOVE));
                setResult(GalleryOptionsDialog.REMOVE, returnIntent);
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
