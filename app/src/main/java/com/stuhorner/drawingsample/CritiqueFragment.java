package com.stuhorner.drawingsample;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class CritiqueFragment extends Fragment {
    SwipeFlingAdapterView flingContainer;
    List<OtherUser> users = new ArrayList<>();
    CardAdapter adapter;
    ImageButton yesButton, noButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main, container, false);
        initData();
        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.swipecards);
        //adapter = new CardAdapter(getActivity().getApplicationContext(),users);
        //flingContainer.setAdapter(adapter);
        yesButton = (ImageButton) view.findViewById(R.id.yes_button);
        noButton = (ImageButton) view.findViewById(R.id.no_button);
        handleCardSwipes();
        handleButtons();
        return view;
    }

    private void handleCardSwipes() {
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            boolean noDown = false, yesDown = false;
            final Animation scaleDownYes = AnimationUtils.loadAnimation(getContext(), R.anim.translate_down);
            final Animation scaleUpYes = AnimationUtils.loadAnimation(getContext(), R.anim.translate_up);
            final Animation scaleDownNo = AnimationUtils.loadAnimation(getContext(), R.anim.translate_down);
            final Animation scaleUpNo = AnimationUtils.loadAnimation(getContext(), R.anim.translate_up);

            @Override
            public void removeFirstObjectInAdapter() {
                users.remove(0);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                if (noDown) {
                    noDown = false;
                    noButton.startAnimation(scaleUpNo);
                }
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                if (yesDown) {
                    yesDown = false;
                    yesButton.startAnimation(scaleUpYes);
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                initData();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float f) {
                scaleUpYes.setFillAfter(true);
                scaleUpNo.setFillAfter(true);
                scaleDownYes.setFillAfter(true);
                scaleDownNo.setFillAfter(true);
                if (f == -1 && !noDown) {
                    noButton.startAnimation(scaleDownNo);
                    noDown = true;
                    yesDown = false;
                } else if (f == 1 && !yesDown) {
                    yesButton.startAnimation(scaleDownYes);
                    yesDown = true;
                    noDown = false;
                }
                if (f < 1 && f > -1) {
                    if (noDown) {
                        noButton.startAnimation(scaleUpNo);
                        noDown = false;
                    }
                    if (yesDown) {
                        yesButton.startAnimation(scaleUpYes);
                        yesDown = false;
                    }
                }
            }
        });
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int position, Object dataObject) {
                /*Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(MainActivity.PERSON_NAME,);
                intent.putExtra("buttons_off", false);
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);*/
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
        addAnimation(yesButton);
        addAnimation(noButton);
    }

    private void handleButtonLogic(ImageButton button) {
        if (button == noButton)
            flingContainer.getTopCardListener().selectLeft();
        else if (button == yesButton) {
            flingContainer.getTopCardListener().selectRight();
            launchMatch();
        }
    }

    private void launchMatch() {
        TextView name = (TextView) flingContainer.getSelectedView().findViewById(R.id.card_name);
        MatchOverlayFragment match = MatchOverlayFragment.newInstance(name.getText());
        match.show(getActivity().getFragmentManager(), "hello");
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.translate_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.translate_up);
        scaleUp.setFillAfter(true);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        button.startAnimation(scaleDown);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.startAnimation(scaleUp);
                        handleButtonLogic(button);
                        break;
                }
                return false;
            }
        });
    }

    private void initData() {
        //UserDownloadTask task = new UserDownloadTask();
        //task.execute();
    }
}
