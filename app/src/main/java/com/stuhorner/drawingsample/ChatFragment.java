package com.stuhorner.drawingsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stu on 12/14/2015.
 */
public class ChatFragment extends Fragment {
    List<String> UID = new ArrayList<>();
    List<String> personNames = new LinkedList<>();
    List<String> subtitles = new LinkedList<>();
    List<Integer> chatIcons = new LinkedList<>();
    List<Boolean> newMessage = new LinkedList<>();
    ChatAdapter adapter;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = (ProgressBar) view.findViewById(R.id.chat_pb);
        initData();
        adapter = new ChatAdapter(personNames, subtitles, chatIcons, newMessage, getContext());

        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //transition to page activity
                Intent intent = new Intent(getActivity(), ChatPage.class);
                intent.putExtra("UID", UID.get(position));
                intent.putExtra("name", personNames.get(position));
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
            }
        });
        return view;
    }

    private void initData() {
        Query query = MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).orderByChild("metadata/last_message_time");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UID.add(0, dataSnapshot.getKey());
                newMessage.add(0, false);
                getName(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //move index to the front
                int index = UID.indexOf(dataSnapshot.getKey());
                Log.d(Integer.toString(index), personNames.get(index));
                personNames.add(0, personNames.get(index));
                personNames.remove(index + 1);
                subtitles.add(0, subtitles.get(index));
                subtitles.remove(index + 1);
                UID.add(0, UID.get(index));
                UID.remove(index + 1);
                newMessage.add(0, newMessage.get(index));
                newMessage.remove(index + 1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getName(final String UID) {
        MainActivity.rootRef.child("users").child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                personNames.add(0, dataSnapshot.getValue().toString());
                subtitles.add(0, "New Match! Tap to send a message");
                getNewMessage(UID, dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getNewMessage(final String uid, final String name) {
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(uid).child("metadata").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("seen_message_time").getValue() != null && (long)dataSnapshot.child("seen_message_time").getValue() < (long)dataSnapshot.child("last_message_time").getValue())
                        || dataSnapshot.child("seen_message_time").getValue() == null) {
                    newMessage.remove(UID.indexOf(uid));
                    newMessage.add(UID.indexOf(uid), true);
                }
                else {
                    newMessage.remove(UID.indexOf(uid));
                    newMessage.add(UID.indexOf(uid), false);
                }
                getSubtitle(uid, name);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getSubtitle(String UID, final String name) {
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).child("metadata").child("last_message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("id", dataSnapshot.getValue().toString());
                if (!dataSnapshot.child("body").getValue().toString().equals("")) {
                    subtitles.remove(personNames.indexOf(name));
                    subtitles.add(personNames.indexOf(name), dataSnapshot.child("body").getValue().toString());
                    chatIcons.add(R.drawable.ic_profile);
                } else {
                    subtitles.remove(personNames.indexOf(name));
                    subtitles.add(personNames.indexOf(name), "New Match! Tap to send a message");
                    chatIcons.add(R.drawable.ic_profile);
                }
                Log.d("with subtitle", subtitles.get(subtitles.size() - 1));
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}