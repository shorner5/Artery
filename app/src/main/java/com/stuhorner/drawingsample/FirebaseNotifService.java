package com.stuhorner.drawingsample;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.List;

public class FirebaseNotifService extends Service {
    private Firebase ref = new Firebase("https://artery.firebaseio.com/");
    String UID;

    public FirebaseNotifService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        UID = pref.getString("UID", null);

        if (UID != null) {
            ref.child("messages").child(UID).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("onChildAdded", dataSnapshot.toString());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
                    if (!services.get(0).topActivity.getPackageName().equalsIgnoreCase(getApplicationContext().getPackageName())) {
                        getName(dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Log.d("onChildMoved", dataSnapshot.toString());
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void getName(final String senderUID) {
        ref.child("users").child(senderUID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getLastMessage(senderUID, dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getLastMessage(final String senderUID, final String senderName) {
        ref.child("messages").child(UID).child(senderUID).child("metadata").child("last_message").child("body").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String last_message = dataSnapshot.getValue().toString();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //if there's a new match
                if (last_message.equals("")) {
                    if (preferences.getBoolean("pref_match_notif", true)) {
                        last_message = String.format(getString(R.string.new_match_notif), senderName);
                        String title = getString(R.string.new_match);
                        makeNotification(title, last_message, senderUID, senderName);
                    }
                }
                else if (preferences.getBoolean("pref_message_notif", true)){
                    makeNotification(senderName, last_message, senderUID, null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void makeNotification(String senderName, String last_message, String senderUID, String matchSenderName) {
        if (MyUser.getInstance() == null) {
            new MyUser(this);
            MyUser.getInstance().setUID(UID);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(this, ChatPage.class);
        if (matchSenderName != null) {
            resultIntent.putExtra("name", matchSenderName);
        }
        else {
            resultIntent.putExtra("name", senderName);
        }
        resultIntent.putExtra("UID", senderUID);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = R.mipmap.ic_launcher;
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(senderName)
                .setContentText(last_message)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), icon))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
