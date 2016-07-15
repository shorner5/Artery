package com.stuhorner.drawingsample;

import com.firebase.client.Firebase;

/**
 * Created by Stu on 6/22/2016.
 */
public class FirebaseContext extends android.app.Application {

    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(false);
    }
}
