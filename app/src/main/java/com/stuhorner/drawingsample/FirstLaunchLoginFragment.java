package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by Stu on 4/22/2016.
 */
public class FirstLaunchLoginFragment extends Fragment {
    Firebase rootRef;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_login_fragment, container, false);
        final EditText email = (EditText) rootView.findViewById(R.id.login_input_email);
        final EditText password = (EditText) rootView.findViewById(R.id.login_input_password);
        rootRef = new Firebase("https://artery.firebaseio.com/");
        loginUser(password, email);

        return rootView;
    }

    private void loginUser(final EditText password, final EditText email) {
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || i == EditorInfo.IME_ACTION_DONE) {
                    showLoading(true);
                    rootRef.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            showLoading(false);
                            MyUser.getInstance().populateUser(authData.getUid());
                            SharedPreferences.Editor pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                            pref.putString("UID", MyUser.getInstance().getUID());
                            Log.d("UID", MyUser.getInstance().getUID());
                            pref.apply();
                            getActivity().setResult(1);
                            getActivity().finish();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            showLoading(false);
                            if (firebaseError.getCode() == FirebaseError.NETWORK_ERROR)
                                invalid(getString(R.string.login_error));
                            else
                                invalid(getString(R.string.login_wrong));
                        }
                    });
                }
                return false;
            }
        });
    }

    private void invalid(String errorText){
        if (getView() != null) {
            TextView error = (TextView) getView().findViewById(R.id.login_errorText);
            error.setText(errorText);
            error.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show_loading) {

        ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar2);
        bar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        TextView error = (TextView) getView().findViewById(R.id.login_loginError);
        TextView auth = (TextView) getView().findViewById(R.id.authenticating2);
        TextView errorText = (TextView) getView().findViewById(R.id.login_errorText);

        if (show_loading) {
            auth.setVisibility(View.VISIBLE);
            bar.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.INVISIBLE);
        }
        else {
            auth.setVisibility(View.INVISIBLE);
            bar.setVisibility(View.INVISIBLE);
            error.setVisibility(View.INVISIBLE);
        }

    }
}
