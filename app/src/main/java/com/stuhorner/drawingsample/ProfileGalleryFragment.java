package com.stuhorner.drawingsample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileGalleryFragment extends Fragment {
    private List<Integer> data;
    public ProfileGalleryFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_gallery, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.p_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        data = initData();
        ProfileGalleryAdapter adapter = new ProfileGalleryAdapter(data);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Integer> initData(){
        List<Integer> gallery_items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            gallery_items.add(R.drawable.example_drawing);
        }
        return gallery_items;
    }
}
