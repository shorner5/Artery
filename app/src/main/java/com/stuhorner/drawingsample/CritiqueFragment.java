package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class CritiqueFragment extends Fragment {
    SwipeFlingAdapterView flingContainer;
    List<String> userNames;
    List<Integer> userAges, drawings;
    CardAdapter adapter;
    ImageButton yesButton, noButton;
    public final static String PERSON_NAME = "com.stuhorner.drawingsample.PERSON_NAME";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main, container, false);
        initData();
        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.swipecards);
        adapter = new CardAdapter(getActivity().getApplicationContext(),userNames,userAges,drawings);
        flingContainer.setAdapter(adapter);
        yesButton = (ImageButton) view.findViewById(R.id.yes_button);
        noButton = (ImageButton) view.findViewById(R.id.no_button);
        handleCardSwipes();
        handleButtons();
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
                userNames.add("Kanye West");
                userAges.add(39);
                drawings.add(R.drawable.example_drawing);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float f) {
            }
        });

        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, Object dataObject) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(PERSON_NAME, userNames.get(position));
                intent.putExtra("buttons_off", false);
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            if (resultCode == ProfileActivity.RESULT_YES){
                //swipe right
                flingContainer.getTopCardListener().selectRight();
            }
            else if (resultCode == ProfileActivity.RESULT_NO ) {
                //swipe left
                flingContainer.getTopCardListener().selectLeft();
            }
        }
    }
    private void handleButtons(){
        final Animation scaleDownYes = AnimationUtils.loadAnimation(getContext(), R.anim.translate_down);
        scaleDownYes.setFillAfter(true);
        final Animation scaleUpYes = AnimationUtils.loadAnimation(getContext(),R.anim.translate_up);
        scaleUpYes.setFillAfter(true);

        yesButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        yesButton.startAnimation(scaleDownYes);
                        break;
                    case MotionEvent.ACTION_UP:
                        yesButton.startAnimation(scaleUpYes);
                        flingContainer.getTopCardListener().selectRight();
                        break;
                }
                return false;
            }
        });

        final Animation scaleDownNo = AnimationUtils.loadAnimation(getContext(),R.anim.translate_down);
        scaleDownNo.setFillAfter(true);
        final Animation scaleUpNo = AnimationUtils.loadAnimation(getContext(),R.anim.translate_up);
        scaleUpNo.setFillAfter(true);

        noButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        noButton.startAnimation(scaleDownNo);
                        break;
                    case MotionEvent.ACTION_UP:
                        noButton.startAnimation(scaleUpNo);
                        flingContainer.getTopCardListener().selectLeft();
                        break;
                }
                return false;
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
