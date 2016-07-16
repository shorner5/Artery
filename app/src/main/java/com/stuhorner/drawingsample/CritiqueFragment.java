package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CritiqueFragment extends Fragment {
    SwipeFlingAdapterView flingContainer;
    List<OtherUser> users = new ArrayList<>();
    Queue<String> userQueue = new LinkedList<>();
    String lastKnownKey;
    CardAdapter adapter;
    ImageButton yesButton, noButton;
    GeoFire geoFire = new GeoFire(new Firebase("https://artery.firebaseio.com"));
    ProgressBar progressBar;
    TextView outOfUsers;
    boolean near_me, isMaleOn, isFemaleOn;
    int minAge, maxAge;
    HashSet<String> seenBuffer = new HashSet<>();
    GeoQuery geoQuery;
    boolean locationQueried = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_main, container, false);
        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.swipecards);
        yesButton = (ImageButton) view.findViewById(R.id.yes_button);
        noButton = (ImageButton) view.findViewById(R.id.no_button);
        progressBar = (ProgressBar) view.findViewById(R.id.critique_progress);
        outOfUsers = (TextView) view.findViewById(R.id.outOfUsers);
        adapter = new CardAdapter(getActivity().getApplicationContext(),users, progressBar);
        flingContainer.setAdapter(adapter);

        handleCardSwipes();
        handleButtons();
        populateApprovals();

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
                Log.d("removed", users.get(0).getName());
                MyUser.getInstance().swiped(users.get(0).getUID());
                users.remove(0);
                updateProgressBar();
                Log.d("outOfUsers", "removeFirstObjectInAdapter");
                outOfUsers();
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
                checkForMatch(((OtherUser)dataObject).getName(), ((OtherUser)dataObject).getUID());
                MyUser.getInstance().swipeRight(((OtherUser)dataObject).getUID());
                if (yesDown) {
                    yesDown = false;
                    yesButton.startAnimation(scaleUpYes);
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.d("onAdapterAboutToEmpty", Integer.toString(itemsInAdapter) + ", " + Integer.toString(userQueue.size()));
                if (userQueue.isEmpty()) {
                    handleNearMe();
                }
                else {
                    populateFromKey(userQueue.poll());
                }
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
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("UID", adapter.getUID(flingContainer.getFirstVisiblePosition()));
                intent.putExtra("buttons_off", false);
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
    }

    private void checkForMatch(final String name, final String UID) {
        MainActivity.rootRef.child("users").child(UID).child("right")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().getValue().toString().equals(MyUser.getInstance().getUID())) {
                                MyUser.getInstance().addMatch(UID);
                                launchMatch(name, UID);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void updateProgressBar() {
        if (users.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void populateApprovals() {
        SharedPreferences pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        isMaleOn = pref.getBoolean("isMaleOn", true);
        isFemaleOn = pref.getBoolean("isFemaleOn", true);
        minAge = pref.getInt("minAge", MainActivity.MIN_AGE_ALLOWED);
        maxAge = pref.getInt("maxAge", 70);
    }

    private void handleNearMe() {
        SharedPreferences pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        near_me = pref.getBoolean("near_me", true);
        if (near_me && MyUser.getInstance().getLocation() != null) {
            if (!locationQueried) {
                initData(MyUser.getInstance().getLocation());
                locationQueried = true;
            }
            else {
                Log.d("outOfUsers", "handleNearMe");
                outOfUsers();
            }
        }
        else if (!near_me) {
            initData();
        }
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
        if (users.size() > 0) {
            if (button == noButton)
                flingContainer.getTopCardListener().selectLeft();
            else if (button == yesButton) {
                flingContainer.getTopCardListener().selectRight();
            }
        }
    }

    private void launchMatch(String name, String UID) {
        sendEmptyMessage(UID);
        MatchOverlayFragment match = MatchOverlayFragment.newInstance(name, UID);
        match.show(getActivity().getFragmentManager(), "match");
    }

    private void addAnimation(final ImageButton button) {
        final Animation scaleDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.translate_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.translate_up);
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

    public void initData(final Location location) {
        Log.d("called initData", "here");
        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 20);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("onKeyEntered", key);
                if (!key.equals(MyUser.getInstance().getUID()) && !MyUser.getInstance().seen(key) && !seenBuffer.contains(key)) {
                    Log.d("add key", key);
                    userQueue.add(key);
                    seenBuffer.add(key);
                }
                if (!userQueue.isEmpty() && users.isEmpty()) {
                    populateFromKey(userQueue.poll());
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.d("onGeoQueryReady", Integer.toString(userQueue.size()));
                geoQuery.removeAllListeners();
            }

            @Override
            public void onGeoQueryError(FirebaseError error) {
                Log.d("onGeoError", error.getDetails());
                outOfUsers();
            }
        });
    }

    private void outOfUsers() {
        updateProgressBar();
        if (users.isEmpty() && userQueue.isEmpty()) {
            outOfUsers.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Log.d("outOfUsers", "VISIBLE");
        }
        else {
            outOfUsers.setVisibility(View.INVISIBLE);
        }
    }

    public void initData() {
        //remove geoQuery listeners
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }

        Query query;
        if (lastKnownKey == null) {
            query = MainActivity.rootRef.child("user_index").orderByKey().limitToFirst(10);
        }
        else {
            Log.d("lastKnownKey", lastKnownKey);
            query = MainActivity.rootRef.child("user_index").orderByKey().startAt(lastKnownKey).limitToFirst(10);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot data = iterator.next();
                    String key = data.getValue().toString();
                    lastKnownKey = data.getKey();
                    if (!key.equals(MyUser.getInstance().getUID()) && !MyUser.getInstance().seen(key) && !seenBuffer.contains(key)) {
                        userQueue.add(key);
                        seenBuffer.add(key);
                    }
                }
                if (!userQueue.isEmpty()) {
                    Log.d("path", "populateFromKey");
                    populateFromKey(userQueue.poll());
                }
                else {
                    Log.d("outOfUsers", "initData");
                    outOfUsers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void populateFromKey(final String key) {
        //create the OtherUser object
        if (getContext() != null) {
            final OtherUser user = new OtherUser(getContext());
            //get name
            MainActivity.rootRef.child("users").child(key).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        user.setName(dataSnapshot.getValue().toString());
                        user.setUID(key);
                        Log.d("path", "calling getAge");
                        getAge(user);
                    }
                    else if (users.isEmpty() && !userQueue.isEmpty()) {
                        populateFromKey(userQueue.poll());
                    }
                    else {
                        Log.d("outOfUsers", "populateFromKey " + key);
                        outOfUsers();
                    }

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void getAge(final OtherUser user) {
        MainActivity.rootRef.child("users").child(user.getUID()).child("age").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    user.setAge(Integer.parseInt(dataSnapshot.getValue().toString()));
                    Log.d("path", "calling getGender");
                    getGender(user);
                }
                else if (users.isEmpty() && !userQueue.isEmpty()) {
                    populateFromKey(userQueue.poll());
                }
                else {
                    Log.d("outOfUsers", "getAge");
                    outOfUsers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getGender(final OtherUser user) {
        MainActivity.rootRef.child("users").child(user.getUID()).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    user.setGender(Integer.parseInt(dataSnapshot.getValue().toString()));
                    if (approveUser(user)) {
                        Log.d("path", "calling getCard");
                        getCard(user);
                    }
                    else if (users.isEmpty() && !userQueue.isEmpty()) {
                        populateFromKey(userQueue.poll());
                    }
                    else {
                        Log.d("outOfUsers", "getGender");
                        outOfUsers();
                    }
                }
                else if (users.isEmpty() && !userQueue.isEmpty()) {
                    populateFromKey(userQueue.poll());
                }
                else {
                    Log.d("outOfUsers", "getGender2, " + user.getUID());
                    outOfUsers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getCard(final OtherUser user) {
        MainActivity.rootRef.child("users").child(user.getUID()).child("card").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("path", "getCard-onDataChange " + user.getUID());
                if (dataSnapshot.getValue() != null) {
                    Log.d("path", "value is not null");
                    user.setCard(dataSnapshot.getValue().toString());
                    users.add(user);
                    adapter.notifyDataSetChanged();
                    Log.d("outOfUsers", "getCard");
                    outOfUsers();
                    Log.d("added user", user.getName());
                    Log.d("user size", Integer.toString(users.size()));
                }
                else if (users.isEmpty() && !userQueue.isEmpty()) {
                    Log.d("path", "calling populateFromKey cuz canceled");
                    populateFromKey(userQueue.poll());
                }
                else {
                    Log.d("outOfUsers", "getCard2");
                    outOfUsers();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private boolean approveUser(OtherUser user) {
        Log.d("path", "at approveUser");
        if ((user.getAge() <= maxAge || maxAge == 70) && user.getAge() >= minAge) {
            //both on
            if ((isMaleOn && isFemaleOn)) {
                return true;
            }
            //just male on
            else if (isMaleOn && user.getGender() == OtherUser.MALE) {
                    return true;
            }
            //just female on
            else if (isFemaleOn && user.getGender() == OtherUser.FEMALE) {
                return true;
            }
            //neither
            else if (!isFemaleOn && !isMaleOn && user.getGender() == OtherUser.NEITHER){
                return true;
            }
        }
        return false;
    }

    private void sendEmptyMessage(String UID) {
        Message messageToSend = new Message("", "");
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).
                child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        MainActivity.rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).
                child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).
                child("metadata").child("last_message").setValue(messageToSend);
        MainActivity.rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).
                child("metadata").child("last_message").setValue(messageToSend);
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).
                push().setValue(messageToSend);
        MainActivity.rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).
                push().setValue(messageToSend);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (geoQuery != null)
            geoQuery.removeAllListeners();
    }
}