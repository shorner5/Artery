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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        UID = getActivity().getIntent().getStringExtra("UID");
        textView = (TextView) view.findViewById(R.id.p_text);
        editText = (EditText) view.findViewById(R.id.p_edit_text);
        bar = (ProgressBar) view.findViewById(R.id.p_progress_bar);
        textView.setMovementMethod(new ScrollingMovementMethod());
        setHasOptionsMenu(true);
        initData();
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
        SharedPreferences sharedPref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.profile_text), textView.getText().toString());
        editor.apply();

        MyUser.getInstance().setProfileText(textView.getText().toString());
    }


    private void initData(){
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

    private void checkForText() {
        Log.d("loc", "checkforText");
        MainActivity.rootRef.child("users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("loc", "onDATACHANCE");
                Object profile_text = dataSnapshot.child("profileText").getValue();
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
                Log.d("FIREBASEERROR", firebaseError.toString());
            }
        });
    }
}
