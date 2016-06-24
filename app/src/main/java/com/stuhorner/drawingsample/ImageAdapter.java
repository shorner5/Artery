package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 4/4/2016.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    ArrayList<String> imageURLs = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();

    public ImageAdapter(Context c, ArrayList<String> imageURLs) {
        context = c;
        this.imageURLs = imageURLs;
        for (String path : this.imageURLs) {
            images.add(BitmapFactory.decodeFile(path));
        }
    }
    public int getCount() {
        return images.size();
    }
    public String getItem(int i) {
        return imageURLs.get(i);
    }
    public long getItemId(int position) {
        return 0;
    }

    @TargetApi(21)
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            Resources r = Resources.getSystem();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, context.getResources().getInteger(R.integer.gallery_size), r.getDisplayMetrics());
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams((int)px, (int)px));
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setPadding(8,8,8,8);
        }
        else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(images.get(position));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            imageView.setTransitionName("image_scale");
        return imageView;
    }

    public void reloadImages(ArrayList<String> imageURLs) {
        this.imageURLs = imageURLs;
        images.clear();
        for (String path : this.imageURLs) {
            images.add(BitmapFactory.decodeFile(path));
        }
        this.notifyDataSetChanged();
    }
}
