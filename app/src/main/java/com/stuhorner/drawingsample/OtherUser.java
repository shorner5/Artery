package com.stuhorner.drawingsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 4/27/2016.
 */

public class OtherUser {
    //constants
    public static int MALE = 0;
    public static int FEMALE = 1;
    public static int NEITHER = 2;
    public static Firebase ref = new Firebase("https://artery.firebaseio.com/");

    private String name;
    private int age;
    private String email;
    private String profileText;
    private String profilePicture;
    private int gender;
    private Object UID;
    private List<String> gallery = new ArrayList<>();
    private Bitmap card;

    public OtherUser(Context context) {
        Firebase.setAndroidContext(context);
    }

    public void populateInitial(final String UID) {
        this.UID = UID;
        Log.d("UID:", UID);
        ref.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").getValue() != null) {
                    Log.d("name", dataSnapshot.child("name").toString());
                    setName(dataSnapshot.child("name").getValue().toString());
                }
                if (dataSnapshot.child("age").getValue() != null)
                    setAge(((Long) dataSnapshot.child("age").getValue()).intValue());
                //if (dataSnapshot.child("gallery").child("0").getValue() != null)
                    //setCard(dataSnapshot.child("gallery").child("0").getValue().toString());
                //if (dataSnapshot.child("gender").getValue() != null)
                    //setGender(((Long) dataSnapshot.child("gender").getValue()).intValue());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Snackbar.make(null, "No internet connection", Snackbar.LENGTH_LONG);
            }
        });
    }

    public void populateProfile(final String UID) {
        this.UID = UID;
        ref.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("profileText").getValue() != null)
                    setProfileText(dataSnapshot.child("profileText").getValue().toString());
                if (dataSnapshot.child("profilePicture").getValue() != null)
                    setProfilePicture(dataSnapshot.child("profilePicture").getValue().toString());
                if (dataSnapshot.child("gallery").getValue() != null)
                    setGallery((List<String>) dataSnapshot.child("gallery").getValue());
                else {
                    gallery = new ArrayList<String>();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Snackbar.make(null, "No internet connection", Snackbar.LENGTH_LONG);
            }
        });

    }

    /* Getters */

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileText() {
        return profileText;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getUID() {
        return UID.toString();
    }

    public int getGender() {
        return gender;
    }

    public List<String> getGallery() {
        return gallery;
    }

    public Bitmap getCard() {
        return card;
    }

    /* Setters */
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setCard(String base64) {
        //convert to Bitmap in background
        //this.card = base64;
        this.card = BitmapFactory.decodeByteArray(Base64.decode(base64,Base64.DEFAULT),0,base64.length());
    }

    public void setProfileText(String profileText) {
        this.profileText = profileText;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGallery(List<String> gallery) {
        this.gallery = gallery;
    }
}
