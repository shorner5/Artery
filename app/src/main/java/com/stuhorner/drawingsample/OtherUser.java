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
    public final static int MALE = 0;
    public final static int FEMALE = 1;
    public final static int NEITHER = 2;
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

    /* Getters */

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getUID() {
        return UID.toString();
    }

    public void setUID(Object UID) {
        this.UID = UID;
    }

    public int getGender() {
        return gender;
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        byte[] bytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
        this.card = BitmapFactory.decodeByteArray(bytes,0, bytes.length, options);
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

}
