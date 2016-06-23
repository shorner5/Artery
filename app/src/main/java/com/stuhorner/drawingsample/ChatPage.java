package com.stuhorner.drawingsample;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;

import java.lang.CharSequence;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 12/24/2015.
 */
public class ChatPage extends AppCompatActivity {
    List<Message> messages = new ArrayList<>();
    String UID;
    ImageButton sendButton;
    ChatMessageAdapter adapter;
    boolean active = true;
    Firebase ref;
    Firebase rootRef = new Firebase("https://artery.firebaseio.com/");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        UID = getIntent().getStringExtra("UID");

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initMessages();

        final EditText editText = (EditText) findViewById(R.id.chat_edit_text);
        sendButton = (ImageButton) findViewById(R.id.chat_sendButton);
        sendButton.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        sendButton.setAlpha(.2f);

        final ListView listView = (ListView) findViewById(R.id.chat_messages);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        adapter = new ChatMessageAdapter(ChatPage.this, messages);
        listView.setAdapter(adapter);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    sendButton.setAlpha(1f);
                } else {
                    sendButton.setAlpha(.2f);
                }
            }
        });

        //sendButton animation
        final Animation scaleDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
        scaleDown.setFillAfter(true);
        final Animation scaleUp = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale_up);
        scaleUp.setFillAfter(true);

        sendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (editText.getText().length() > 0) {
                            sendButton.startAnimation(scaleDown);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (editText.getText().length() > 0) {
                            sendButton.startAnimation(scaleUp);
                            //launch chat bubble
                            Message messageToSend = new Message(editText.getText().toString(), MyUser.getInstance().getUID());
                            //messages.add(messageToSend);
                            sendMessage(messageToSend);
                            editText.setText(null);
                            //adapter.notifyDataSetChanged();
                            //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                            //listView.post(new Runnable() {
                              //  @Override
                                //public void run() {
                                  //  listView.smoothScrollToPosition(listView.getMaxScrollAmount());
                                    //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                                //}
                            //});
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void sendMessage(Message messageToSend) {
        rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).child("metadata").child("last_message_time").setValue(ServerValue.TIMESTAMP);
        rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).child("metadata").child("last_message").setValue(messageToSend);
        rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).child("metadata").child("last_message").setValue(messageToSend);
        rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).push().setValue(messageToSend);
        rootRef.child("messages").child(UID).child(MyUser.getInstance().getUID()).push().setValue(messageToSend);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.chat_profile) {
            Intent intent = new Intent(ChatPage.this, ProfileActivity.class);
            intent.putExtra("UID", UID);
            intent.putExtra("buttons_off", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
        }
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();
        active = false;
        sendButton.getBackground().clearColorFilter();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }

    private void initMessages() {
        Log.d(MyUser.getInstance().getUID(), "a");
        Log.d(UID, "a");

        ref = rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("datasnap", dataSnapshot.toString());
                if (!dataSnapshot.getKey().equals("metadata") && !dataSnapshot.child("body").getValue().toString().equals("")) {
                    Message message = new Message(dataSnapshot.child("body").getValue().toString(), dataSnapshot.child("sender").getValue().toString());
                    messages.add(message);
                    adapter.notifyDataSetChanged();
                }

                //update seen time
                if (active)
                rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).child("metadata").child("seen_message_time").setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
