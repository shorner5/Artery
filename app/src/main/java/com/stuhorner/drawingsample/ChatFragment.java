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
import com.firebase.client.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stu on 12/14/2015.
 */
public class ChatFragment extends Fragment {
    List<String> UID = new ArrayList<>();
    List<String> personNames = new ArrayList<>();
    List<String> subtitles = new ArrayList<>();
    List<Integer> chatIcons = new ArrayList<>();
    ChatAdapter adapter;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = (ProgressBar) view.findViewById(R.id.chat_pb);
        initData();
        adapter = new ChatAdapter(personNames, subtitles, chatIcons);

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
        MainActivity.rootRef.child("users").child(MyUser.getInstance().getUID()).child("matchedUsers").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UID.add(dataSnapshot.getValue().toString());
                getName(dataSnapshot.getValue().toString());
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

    private void getName(final String UID) {
        MainActivity.rootRef.child("users").child(UID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                personNames.add(dataSnapshot.getValue().toString());
                subtitles.add("New Match! Tap to send a message");
                Log.d("added", personNames.get(personNames.size() - 1));
                getSubtitle(UID, personNames.size() - 1);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getSubtitle(String UID, final int position) {
        MainActivity.rootRef.child("messages").child(MyUser.getInstance().getUID()).child(UID).child("last_message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    subtitles.remove(position);
                    subtitles.add(position, dataSnapshot.child("body").getValue().toString());
                    chatIcons.add(R.drawable.ic_profile);
                }
                else {
                    subtitles.remove(position);
                    subtitles.add(position, "New Match! Tap to send a message");
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