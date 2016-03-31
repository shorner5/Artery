package com.stuhorner.drawingsample;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.lang.CharSequence;
import java.lang.Runnable;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 12/24/2015.
 */
public class ChatPage extends AppCompatActivity {
    List<String> messages;
    ImageButton sendButton;
    public final static String PERSON_NAME = "com.stuhorner.drawingsample.PERSON_NAME";
    String person_name;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_page);
        person_name = getIntent().getStringExtra(ChatFragment.PERSON_NAME);

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
         setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
        getSupportActionBar().setTitle(person_name);
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
        final ChatMessageAdapter adapter = new ChatMessageAdapter(ChatPage.this, "a", messages);
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
                            messages.add(editText.getText().toString());
                            editText.setText(null);
                            adapter.notifyDataSetChanged();
                            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                            listView.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView.smoothScrollToPosition(listView.getMaxScrollAmount());
                                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                                }
                            });
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.chat_profile) {
            /*Intent intent = new Intent(ChatPage.this, Profile.class);
            intent.putExtra(PERSON_NAME, person_name);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.fade_out);*/
        }
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();
        sendButton.getBackground().clearColorFilter();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }

    private void initMessages() {
        messages = new ArrayList<>();
        messages.add("The artist is the creator of beautiful things.  To reveal art and");
        messages.add("conceal the artist is art's aim.  The critic is he who can translate");
        messages.add("into another manner or a new material his impression of beautiful things.");
        messages.add("The highest as the lowest form of criticism is a mode of autobiography.");
        messages.add("Those who find ugly meanings in beautiful things are corrupt without being charming");
        messages.add("Those who find beautiful meanings in beautiful things are the cultivated.");
        messages.add("For these there is hope.  They are the elect to whom beautiful things mean only beauty.");
        messages.add("There is no such thing as a moral or an immoral book.  Books are well");
        messages.add("The nineteenth century dislike of realism is the rage of Caliban seeing his own face in a glass.");
        messages.add("The nineteenth century dislike of romanticism is the rage of Caliban");
        messages.add("not seeing his own face in a glass.  The moral life of man forms part");
        messages.add("of the subject-matter of the artist, but the morality of art consists");
    }
}
