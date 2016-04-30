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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 4/11/2016.
 */
public class MyUser {
    //constants
    public static int MALE = 0;
    public static int FEMALE = 1;
    public static int NEITHER = 2;
    public static Firebase ref = new Firebase("https://artery.firebaseio.com/");

    //this user instance
    private static MyUser instance;
    private String name;
    private int age;
    private String email;
    private String profileText;
    private String profilePicture;
    private String profilePicturePath;
    private int gender;
    private Object UID;
    private List<String> gallery = new ArrayList<>();
    private String card;

    public MyUser() {}

    public MyUser(Context context) {
        instance = new MyUser();
        Firebase.setAndroidContext(context);
    }

    public void newInstance(Context context) {
        instance = new MyUser(context);
    }

    public void populateUser(final String UID) {
        instance.UID = UID;
        ref.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").getValue() != null)
                    instance.setName(dataSnapshot.child("name").getValue().toString());
                if (dataSnapshot.child("age").getValue() != null)
                    instance.setAge(((Long) dataSnapshot.child("age").getValue()).intValue());
                if (dataSnapshot.child("email").getValue() != null)
                    instance.setEmail(dataSnapshot.child("email").getValue().toString());
                if (dataSnapshot.child("profileText").getValue() != null)
                    instance.setProfileText(dataSnapshot.child("profileText").getValue().toString());
                if (dataSnapshot.child("profilePicture").getValue() != null)
                    instance.setProfilePicture(dataSnapshot.child("profilePicture").getValue().toString());
                if (dataSnapshot.child("profilePicturePath").getValue() != null)
                    instance.setProfilePicturePath(dataSnapshot.child("profilePicturePath").getValue().toString());
                if (dataSnapshot.child("gender").getValue() != null)
                    instance.setGender(((Long) dataSnapshot.child("gender").getValue()).intValue());
                if (dataSnapshot.child("gallery").getValue() != null)
                    instance.setGallery((List<String>) dataSnapshot.child("gallery").getValue());
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
    public static MyUser getInstance() {
        return instance;
    }

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

    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    public String getCard() {
        return card;
    }

    /* Setters */
    public void setName(String name) {
        ref.child("users").child(getUID()).child("name").setValue(name);
        this.name = name;
    }

    public void setAge(int age) {
        ref.child("users").child(getUID()).child("age").setValue(age);
        this.age = age;
    }

    public void setEmail(String email) {

        ref.child("users").child(getUID()).child("email").setValue(email);
        this.email = email;
    }

    public void setProfileText(String profileText) {
        ref.child("users").child(getUID()).child("profileText").setValue(profileText);
        this.profileText = profileText;
    }

    public void setProfilePicture(String profilePicture) {
        ref.child("users").child(getUID()).child("profilePicture").setValue(profilePicture);
        this.profilePicture = profilePicture;
    }

    public void setUID(Object UID) {
        this.UID = UID;
    }

    public void setGender(int gender) {
        ref.child("users").child(getUID()).child("gender").setValue(gender);
        this.gender = gender;
    }

    public void setGallery(List<String> gallery) {
        ref.child("users").child(getUID()).child("gallery").setValue(gallery);
        this.gallery = gallery;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        ref.child("users").child(getUID()).child("profilePicturePath").setValue(profilePicturePath);
        this.profilePicturePath = profilePicturePath;
    }

    public void setCard(String cardPath) {
        Bitmap BitmapToDelete = BitmapFactory.decodeFile(cardPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapToDelete.compress(Bitmap.CompressFormat.PNG, 100, baos);
        BitmapToDelete.recycle();
        byte[] bytes = baos.toByteArray();
        card = Base64.encodeToString(bytes, Base64.DEFAULT);
        ref.child("users").child(getUID()).child("card").setValue(card);
    }

    public void addToGallery(String drawing) {
        gallery.add(drawing);
        ref.child("users").child(getUID()).child("gallery").setValue(gallery);
    }

    public void removeFromGallery(int i) {
        gallery.remove(i);
        ref.child("users").child(getUID()).child("gallery").setValue(gallery);
    }
}
