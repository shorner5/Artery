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
import com.firebase.client.Firebase;
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
    List<ChatRow> chatRows = new LinkedList<>();
    ChatAdapter adapter;
    ProgressBar progressBar;
    Firebase ref = new Firebase("https://artery.firebaseio.com/");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = (ProgressBar) view.findViewById(R.id.chat_pb);
        initData();
        adapter = new ChatAdapter(chatRows, getContext());

        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                //transition to page activity
                if (position < chatRows.size()) {
                    Intent intent = new Intent(getActivity(), ChatPage.class);
                    intent.putExtra("UID", chatRows.get(position).getUID());
                    intent.putExtra("name", chatRows.get(position).getName());
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in, R.anim.fade_out);
                }
            }
        });
        return view;
    }

    private void initData() {
        Query query = ref.child("messages").child(MyUser.getInstance().getUID()).orderByChild("metadata/last_message_time");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatRow row = new ChatRow(dataSnapshot.getKey());
                chatRows.add(0, row);
                getName(row);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String key) {
                //move the row to the front
                int index = findIndexFromKey(dataSnapshot.getKey());
                Log.d("index", Integer.toString(index));
                Log.d("key", dataSnapshot.getKey());
                ChatRow row = chatRows.get(index);
                chatRows.remove(index);
                chatRows.add(0, row);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (chatRows.size() == 0) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getName(final ChatRow row) {
        ref.child("users").child(row.getUID()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                row.setName(dataSnapshot.getValue().toString());
                getNewMessage(row);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getNewMessage(final ChatRow row) {
        ref.child("messages").child(MyUser.getInstance().getUID()).child(row.getUID()).child("metadata").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("seen_message_time").getValue() != null && dataSnapshot.child("last_message_time").getValue() != null)) {
                    if ((long) dataSnapshot.child("seen_message_time").getValue() < (long) dataSnapshot.child("last_message_time").getValue()) {
                        row.setNewMessage(true);
                    } else {
                        row.setNewMessage(false);
                    }
                }
                else {
                    row.setNewMessage(true);
                }
                getSubtitle(row);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getSubtitle(final ChatRow row) {
        ref.child("messages").child(MyUser.getInstance().getUID()).child(row.getUID()).child("metadata").child("last_message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.child("body").getValue() != null && !dataSnapshot.child("body").getValue().toString().equals("")) {
                        row.setSubtitle(dataSnapshot.child("body").getValue().toString());
                    } else {
                        row.setSubtitle(getString(R.string.new_match));
                    }
                    row.setChatIcon(R.drawable.ic_profile);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private int findIndexFromKey(String key) {
        for (int i = 0; i < chatRows.size(); i++) {
            Log.d("AT " + Integer.toString(i), chatRows.get(i).getUID());
            if (chatRows.get(i).getUID().equals(key)) {
                return i;
            }
        }
        return 0;
    }
}