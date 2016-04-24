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
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileDetailsFragment extends Fragment {

    TextView textView;
    EditText editText;
    String profileText = "ERROR";
    ProgressBar bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        textView = (TextView) view.findViewById(R.id.p_text);
        editText = (EditText) view.findViewById(R.id.p_edit_text);
        bar = (ProgressBar) view.findViewById(R.id.p_progress_bar);
        textView.setMovementMethod(new ScrollingMovementMethod());
        setHasOptionsMenu(true);
        initData();
        textView.setText(profileText);
        editText.setText(profileText);
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
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.profile_text), textView.getText().toString());
        editor.apply();

        User.getInstance().setProfileText(textView.getText().toString());
        //TODO: save to interwebs
    }


    private void initData(){
        //bar.setVisibility(View.VISIBLE);
        //if user's profile
        if (getActivity().getIntent().getBooleanExtra("editable", false)) {
            //get text saved to the device
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            //TODO: PUT THIS IN LOG OUT
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.profile_text));
            editor.apply();

            //if no text has been saved to the device before, check the server
            profileText = sharedPreferences.getString(getString(R.string.profile_text), null);
            if (profileText == null) {
                profileText = User.getInstance().getProfileText();
                if (profileText == null) {
                    profileText = getString(R.string.default_profile_text);
                }
                return;
            }
        }
        else {
            profileText = "Lorem ipsum dolor sit amet, eam an sumo erant, mei feugiat aliquando ne, dico graeco audiam ea pri. No mea purto elit clita. Sea in saepe quando. Doctus inermis no per, ornatus volutpat hendrerit quo cu, ea malis saepe offendit pri.\n" +
                    "\n" +
                    "No sit homero labitur evertitur, mel ei vulputate appellantur. Ex mei stet dolor. Meis verear vulputate usu no. Ad erat sadipscing qui. Eum eius nusquam lucilius in, sanctus invidunt scribentur ei duo.\n" +
                    "\n" +
                    "Dico definitionem at vis, duo id feugiat fastidii, modo alterum ea mea. Ignota scriptorem qui cu, at posse epicurei usu, sit tation omittam id. Cu quidam pertinax theophrastus quo. Ad solet meliore phaedrum mel, nam iracundia euripidis id, zril aeterno cum in. Vel evertitur theophrastus cu, ius erat error electram ad.";
        }

        bar.setVisibility(View.INVISIBLE);
    }

    private void checkForText() {
        Log.d("loc", "checkforText");
        MainActivity.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("loc", "onDATACHANCE");
                Object profile_text = dataSnapshot.child("users").child(User.getInstance().getUID().toString()).child("profileText").getValue();
                Object profile_name = dataSnapshot.child("users").child(User.getInstance().getUID().toString()).child("name").getValue();
                //if no text is saved on the server, set the default text
                if (profile_text == null)
                    profileText = getString(R.string.default_profile_text);
                else
                    profileText = profile_text.toString();
                //save to the device
                SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(getString(R.string.profile_text), profileText);
                editor.putString(getString(R.string.profile_name), profile_name.toString());
                editor.apply();

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
