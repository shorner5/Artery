package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 4/29/2016.
 */
public class ProfileGalleryInitTask extends AsyncTask<String, Void, ArrayList<String>> {
    Activity activity;
    RecyclerView recyclerView;
    ArrayList<String> drawings;
    ProfileGalleryAdapter img;
    ProgressBar progressBar;

    public ProfileGalleryInitTask(Activity activity, RecyclerView recyclerView, ArrayList<String> drawings, ProfileGalleryAdapter img, ProgressBar progressBar) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.drawings = drawings;
        this.img = img;
        this.progressBar = progressBar;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        List<Bitmap> tempDrawings = new ArrayList<>();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("data", Context.MODE_PRIVATE);
        String dir = sharedPreferences.getString(activity.getString(R.string.directory), null);
        if (dir != null) {
            File directory = new File(dir);
            File[] files = directory.listFiles();
            for (File file : files) {
                try {
                    if (!file.exists()) continue;
                    tempDrawings.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    drawings.add(file.getAbsolutePath());
                    Log.d("added file", "yup");
                } catch (Exception e) {
                    Log.d("error", e.toString());
                }
            }
        }
        if (MyUser.getInstance().getGallery() != null){
            //if dir is null, load gallery from server
            for (String drawingAsBase64 : MyUser.getInstance().getGallery()) {
                //convert from base64 to bitmap and save to device
                byte[] imageAsBytes = Base64.decode(drawingAsBase64.getBytes(), Base64.DEFAULT);
                Bitmap image = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                //check if the image is already in the array
                boolean uniqueDrawing = true;
                for (Bitmap drawing : tempDrawings) {
                    if (image.sameAs(drawing)) {
                        uniqueDrawing = false;
                    }
                }
                if (uniqueDrawing) {
                    drawings.add(saveToDevice(image));
                    Log.d("uniquedrawing saved", "yup");
                }
                image.recycle();
            }
        }
        return drawings;
    }

    private String saveToDevice(Bitmap bm) {
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());
        File file = null, dir = cw.getExternalFilesDir(null);
        SharedPreferences.Editor editor = activity.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putString(activity.getString(R.string.directory), dir.getAbsolutePath());
        editor.apply();

        String title = "drawing" + System.currentTimeMillis() + ".png";
        try {
            if (!dir.isDirectory() || !dir.exists()){
                dir.mkdirs();
            }
            file = new File(dir, title);
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(activity.getApplicationContext(), "Not enough space on this device!", Toast.LENGTH_SHORT).show();
        } catch(IOException e){
            e.printStackTrace();
            Toast.makeText(activity.getApplicationContext(), "Not enough space on this device", Toast.LENGTH_SHORT).show();
        }
        return file.getAbsolutePath();
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        img = new ProfileGalleryAdapter(result);
        recyclerView.setAdapter(img);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
