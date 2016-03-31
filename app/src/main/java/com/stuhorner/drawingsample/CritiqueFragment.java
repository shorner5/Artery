package com.stuhorner.drawingsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class CritiqueFragment extends Fragment {
    SwipeFlingAdapterView flingContainer;
    List<String> userNames;
    List<Integer> userAges, drawings;
    CardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main, container, false);
        initData();
        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.swipecards);
        adapter = new CardAdapter(getActivity().getApplicationContext(),userNames,userAges,drawings);
        flingContainer.setAdapter(adapter);
        handleCardSwipes();
        return view;
    }

    private void handleCardSwipes() {
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                userNames.remove(0);
                userAges.remove(0);
                drawings.remove(0);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onLeftCardExit(Object dataObject) {
            }

            @Override
            public void onRightCardExit(Object dataObject) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                //userNames.add("Kanye West");
                //userAges.add(39);
                //drawings.add(R.drawable.example_drawing);
            }

            @Override
            public void onScroll(float f) {
            }
        });

        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, Object dataObject) {
                Toast.makeText(getActivity().getApplicationContext(),userNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        userNames = new ArrayList<>();
        userNames.add("Fred");
        userNames.add("Joe");
        userNames.add("Bob");
        userNames.add("Srikar");
        userNames.add("Harry");
        userNames.add("Fred 2.0");
        userNames.add("Fred 2.1");
        userNames.add("Terminator");

        userAges = new ArrayList<>();
        drawings = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            userAges.add(20 + i);
            drawings.add(R.drawable.example_drawing);
        }
    }
}
