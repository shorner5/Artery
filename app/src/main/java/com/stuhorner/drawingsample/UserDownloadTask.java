package com.stuhorner.drawingsample;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.List;

/**
 * Created by Stu on 4/28/2016.
 */
public class UserDownloadTask extends AsyncTask<String,Void,Void> {
    CardAdapter adapter;
    SwipeFlingAdapterView flingContainer;
    List<OtherUser> users;
    Context context;
    public static Firebase ref = new Firebase("https://artery.firebaseio.com/");

    public UserDownloadTask(CardAdapter adapter, SwipeFlingAdapterView flingContainer, List<OtherUser> users, Context context) {
        this.adapter = adapter;
        this.flingContainer = flingContainer;
        this.users = users;
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Query queryRef = ref.orderByChild("UID").limitToFirst(1);
                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        OtherUser user = new OtherUser(context);
                        user.populateInitial(dataSnapshot.getValue().toString());
                        users.add(user);
                        Log.d("filled user", "populateInitial");
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
        return null;
    }

        @Override
        protected void onPostExecute(Void noResult) {
            adapter.notifyDataSetChanged();
        }
}
