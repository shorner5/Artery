package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InterruptedIOException;

/**
 * Created by Stu on 4/17/2016.
 */
class BitmapUploadTask extends AsyncTask<String, Void, String> {
    public static boolean PROFILE_PICTURE = false;
    public static boolean ADD_TO_GALLERY = true;
    private boolean settings = PROFILE_PICTURE;
    Activity currentActivity = null;
    private String path;

    public BitmapUploadTask(boolean settings) {
        this.settings = settings;
    }

    public BitmapUploadTask(boolean settings, Activity currentActivity) {
        this.settings = settings;
        this.currentActivity = currentActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        path = params[0];
        Bitmap resizedBitmap = BitmapFactory.decodeFile(path);
        if (settings == PROFILE_PICTURE) {
            resizedBitmap = getResizedBitmap(resizedBitmap, 800);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        Log.d("resized", Integer.toString(maxSize));
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            if (settings == ADD_TO_GALLERY) {
                MyUser.getInstance().addToGallery(result);
            }
            else if (settings == PROFILE_PICTURE) {
                //set as profile picture
                if (MyUser.getInstance() != null) {
                    MyUser.getInstance().setProfilePicture(result);
                    MyUser.getInstance().setProfilePicturePath(path);
                }
                //save to the device
                if (currentActivity != null) {
                    SharedPreferences sharedPref = currentActivity.getSharedPreferences("data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(currentActivity.getString(R.string.profile_picture), path);
                    editor.apply();
                }
            }
        }
    }
}

