package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stu on 4/8/2016.
 */
public class FirstLaunchCreateLoginFragment extends Fragment {
    Firebase rootRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fl_create_login_fragment, container, false);
        final EditText email = (EditText)rootView.findViewById(R.id.input_email);
        final EditText password = (EditText) rootView.findViewById(R.id.input_password);
        rootRef = new Firebase("https://artery.firebaseio.com/");

        email.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        password.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        setTextWatchers(email,password);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || i == EditorInfo.IME_ACTION_DONE) {
                    if (password.getText().length() < 6) {
                        animateInvalid(password, getString(R.string.password_info));
                        return false;
                    }
                    if (!isEmailValid(email.getText().toString())) {
                        animateInvalid(email, getString(R.string.email_info));
                        return false;
                    }

                    showLoading(true);

                    rootRef.createUser(email.getText().toString(), password.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(final Map<String, Object> result) {
                            //log in the user
                            rootRef.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    if (MyUser.getInstance() != null) {
                                        MyUser.getInstance().setUID(result.get("uid"));
                                        //save UID
                                        rootRef.child("user_index").push().setValue(result.get("uid"));
                                        SharedPreferences.Editor pref = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                                        pref.putString("UID", MyUser.getInstance().getUID());
                                        pref.commit();

                                        //set name and age
                                        SharedPreferences getData = getActivity().getPreferences(Context.MODE_PRIVATE);
                                        MyUser.getInstance().setName(getData.getString("Username", null));
                                        MyUser.getInstance().setAge(getData.getInt("age", 0));

                                        MyUser.getInstance().setEmail(email.getText().toString());
                                        FirstLaunchActivity.mPager.setCurrentItem(FirstLaunchActivity.mPager.getCurrentItem() + 1);
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                                        showLoading(false);
                                    }
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                }
                            });
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            showLoading(false);
                            TextView auth = (TextView) getView().findViewById(R.id.loginError);
                            auth.setVisibility(View.INVISIBLE);
                            Log.d("error", firebaseError.toException().toString());
                            if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
                                animateInvalid(email, getString(R.string.email_taken));
                            }
                            else if (firebaseError.getCode() == FirebaseError.INVALID_EMAIL) {
                                animateInvalid(email, getString(R.string.email_info));
                            }
                            else {
                                animateValid(email);
                                auth.setVisibility(View.VISIBLE);
                                auth.setText(R.string.login_error);
                            }
                        }
                    });

                }
                return false;
            }
        });
        return rootView;
    }

    private void showLoading(boolean show_loading) {

        ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
        bar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        TextView error = (TextView) getView().findViewById(R.id.loginError);
        TextView auth = (TextView) getView().findViewById(R.id.authenticating);

        if (show_loading) {
            auth.setVisibility(View.VISIBLE);
            bar.setVisibility(View.VISIBLE);
        }
        else {
            auth.setVisibility(View.INVISIBLE);
            bar.setVisibility(View.INVISIBLE);
            error.setVisibility(View.INVISIBLE);
        }

    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void animateInvalid(EditText editText, String errorText) {
        editText.hasFocus();
        editText.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
        editText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        editText.setTextColor(getResources().getColor(R.color.red));
        TextView error = (TextView) getView().findViewById(R.id.errorText);
        if (error != null) {
            error.setText(errorText);
            error.setVisibility(View.VISIBLE);
        }
    }

    private void animateValid(EditText editText) {
        editText.setTextColor(getResources().getColor(android.R.color.white));
        editText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        TextView error = (TextView) getView().findViewById(R.id.errorText);
        if (error != null) {
            error.setVisibility(View.INVISIBLE);
        }
    }

    private void setTextWatchers(final EditText email, final EditText password) {
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 5) {
                    animateValid(password);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() < 1) {
                    animateValid(email);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }
}
