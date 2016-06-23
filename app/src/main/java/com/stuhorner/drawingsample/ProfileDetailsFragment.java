package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileDetailsFragment extends Fragment {
    String UID;
    TextView textView;
    EditText editText;
    String profileText;
    ProgressBar bar;
    Firebase ref = new Firebase("https://artery.firebaseio.com/");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        UID = getActivity().getIntent().getStringExtra("UID");
        textView = (TextView) view.findViewById(R.id.p_text);
        editText = (EditText) view.findViewById(R.id.p_edit_text);
        bar = (ProgressBar) view.findViewById(R.id.p_progress_bar);
        textView.setMovementMethod(new ScrollingMovementMethod());
        setHasOptionsMenu(true);
        initData(view);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit :
                editText.setText(textView.getText());
                textView.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.VISIBLE);
                break;
            case R.id.action_done:
                textView.setText(editText.getText().toString());
                textView.setVisibility(View.VISIBLE);
                editText.setVisibility(View.INVISIBLE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                saveText();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveText() {
        /*SharedPreferences sharedPref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.profile_text), textView.getText().toString());
        editor.apply();*/

        MyUser.getInstance().setProfileText(textView.getText().toString());
    }


    private void initData(View view){
        getAge(view);
        getSex(view);
        //if user's profile
        if (getActivity().getIntent().getBooleanExtra("editable", false)) {
            profileText = MyUser.getInstance().getProfileText();
            if (profileText != null) {
                textView.setText(profileText);
                bar.setVisibility(View.INVISIBLE);
                return;
            }
        }
        checkForText();
    }

    private void getAge(final View view) {
        ref.child("users").child(UID).child("age").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView textView = (TextView) view.findViewById(R.id.profile_age);
                if (textView != null) {
                    textView.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getSex(final View view) {
        ref.child("users").child(UID).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView textView = (TextView) view.findViewById(R.id.profile_gender);
                if (textView != null) {
                    if (dataSnapshot.getValue().toString().equals("0")) {
                        textView.setText(getString(R.string.gender_male));
                    }
                    if (dataSnapshot.getValue().toString().equals("1")) {
                        textView.setText(getString(R.string.gender_female));
                    }
                    if (dataSnapshot.getValue().toString().equals("2")) {
                        textView.setText(getString(R.string.gender_neither));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void checkForText() {
        ref.child("users").child(UID).child("profileText").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object profile_text = dataSnapshot.getValue();
                //if no text is saved on the server, set the default text
                if (profile_text == null)
                    profileText = getString(R.string.default_profile_text);
                else
                    profileText = profile_text.toString();

                textView.setText(profileText);
                editText.setText(profileText);
                bar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }
}
