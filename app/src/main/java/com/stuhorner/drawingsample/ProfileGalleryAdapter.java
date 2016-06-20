package com.stuhorner.drawingsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileGalleryAdapter extends RecyclerView.Adapter<ProfileGalleryAdapter.MyViewHolder> {
    List<String> mListData;
    List<Bitmap> images = new ArrayList<>();

    public ProfileGalleryAdapter(List<String> mListData) {
        this.mListData = mListData;
        convertBase64ToBitmap();
    }

    private void convertBase64ToBitmap() {
        for (String base64 : mListData) {
            byte[] bytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            images.add(bm);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_gallery_card,
                viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        myViewHolder.img.setImageBitmap(images.get(i));
    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        public MyViewHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.profile_gallery_img);
        }
    }

}
