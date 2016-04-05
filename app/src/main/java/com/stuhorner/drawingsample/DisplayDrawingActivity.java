package com.stuhorner.drawingsample;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
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

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.fullscreen_layout);
        ImageView img =(ImageView) findViewById(R.id.fullscreen_image);
        String path = getIntent().getStringExtra("edit_image");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        img.setImageBitmap(bitmap);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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
