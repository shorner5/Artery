package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileGalleryFragment extends Fragment {
    ArrayList<String> drawings;
    ProfileGalleryAdapter adapter;
    public ProfileGalleryFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_gallery, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.p_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        drawings = new ArrayList<>();
        adapter = new ProfileGalleryAdapter(drawings);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.profile_gallery_loading);
        initData(recyclerView, progressBar);
        return view;
    }

    private void initData(RecyclerView recyclerView, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        ProfileGalleryInitTask task = new ProfileGalleryInitTask(getActivity(), recyclerView, drawings, adapter, progressBar);
        task.execute();
    }
}
