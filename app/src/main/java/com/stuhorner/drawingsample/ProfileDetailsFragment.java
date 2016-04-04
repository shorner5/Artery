package com.stuhorner.drawingsample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Stu on 3/23/2016.
 */
public class ProfileDetailsFragment extends Fragment {

    TextView textView;
    EditText editText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.profile_details, container, false);
        textView = (TextView) view.findViewById(R.id.p_text);
        editText = (EditText) view.findViewById(R.id.p_edit_text);
        editText.setText(initData());
        textView.setText(initData());
        textView.setMovementMethod(new ScrollingMovementMethod());
        setHasOptionsMenu(true);
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
    }


    private String initData(){
        if (getActivity().getIntent().getBooleanExtra("editable", false)) {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            String defaultValue = getResources().getString(R.string.default_profile_text);
            return sharedPreferences.getString(getString(R.string.profile_text), defaultValue);
        }
        else {
            return "Lorem ipsum dolor sit amet, eam an sumo erant, mei feugiat aliquando ne, dico graeco audiam ea pri. No mea purto elit clita. Sea in saepe quando. Doctus inermis no per, ornatus volutpat hendrerit quo cu, ea malis saepe offendit pri.\n" +
                    "\n" +
                    "No sit homero labitur evertitur, mel ei vulputate appellantur. Ex mei stet dolor. Meis verear vulputate usu no. Ad erat sadipscing qui. Eum eius nusquam lucilius in, sanctus invidunt scribentur ei duo.\n" +
                    "\n" +
                    "Dico definitionem at vis, duo id feugiat fastidii, modo alterum ea mea. Ignota scriptorem qui cu, at posse epicurei usu, sit tation omittam id. Cu quidam pertinax theophrastus quo. Ad solet meliore phaedrum mel, nam iracundia euripidis id, zril aeterno cum in. Vel evertitur theophrastus cu, ius erat error electram ad.";
        }
    }
}
