package com.fonsecakarsten.ahsl.Log_In;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;

public class Log_In_Activity extends AppCompatActivity {
    private static final String PREFS_NAME = "Logging In...";
    public static final String IS_FROM_LOGOUT = "FROM_LOGOUT";
    private static final String SHOULD_SAVE_LOGIN = "SAVE_LOGIN";
    private EditText user;
    private EditText pass;
    private CheckBox rem_me;
    private SharedPreferences.Editor editor;
    private SharedPreferences settings2;
    private static final String USERNAME = "USERNAME";
    private static final String PASS = "PASS";


    public Log_In_Activity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            TransitionInflater inflater1 = TransitionInflater.from(this);
            Transition transition = inflater1.inflateTransition(R.transition.transition);
            getWindow().setExitTransition(transition);
        }

        setContentView(R.layout.log_in_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setIcon(R.drawable.ic_launcher2);

        settings2 = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings2.edit();

        user = ((EditText) findViewById(R.id.Username));
        pass = ((EditText) findViewById(R.id.Password));
        rem_me = ((CheckBox) findViewById(R.id.Remember_Me));

        boolean fromLogout = getIntent().getBooleanExtra(IS_FROM_LOGOUT, false);

        if (!fromLogout)

        {
            if (isLoginSaved()) {
                loadPreferences();
                rem_me.setChecked(true);

                logIn(null);
            }
        } else

        {
            editor.clear();
            editor.apply();
        }

        setUpKeys(user);

        setUpKeys(pass);

    }

    public void logIn(@SuppressWarnings("UnusedParameters") View view) {

        String username = user.getText().toString().trim();
        String passwd = pass.getText().toString().trim();

        if (rem_me.isChecked()) {
            savePreferences();
            editor.putBoolean(SHOULD_SAVE_LOGIN, true);
        } else {
            editor.putBoolean(SHOULD_SAVE_LOGIN, false);
            editor.clear();
        }

        editor.commit();

        startLogIn(username, passwd);
    }

    private void startLogIn(String username, String passwd) {
        if (username.length() == 0 || pass.length() == 0) {
            Toast.makeText(this, "One or more fields are empty. Please correct and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!Utils.isOnline(this)) {
            Toast.makeText(this, "This app requires Internet access. Please connect to the Internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        Utils.lockOrientation(this);

        LogInTask logInTask = new LogInTask(username, passwd, this);
        logInTask.execute();
    }

    private void setUpKeys(EditText text) {
        text.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (int i = s.length(); i > 0; i--) {
                    if (s.subSequence(i - 1, i).toString().equals("\n"))
                        s.replace(i - 1, i, "");
                }
            }
        });
    }

    private boolean isLoginSaved() {
        return settings2.getBoolean(SHOULD_SAVE_LOGIN, false);
    }

    private void savePreferences() {
        editor.putString(USERNAME, user.getText().toString().trim());
        editor.putString(PASS, pass.getText().toString().trim());

        editor.commit();
    }

    private void loadPreferences() {
        user.setText(settings2.getString(USERNAME, ""));
        pass.setText(settings2.getString(PASS, ""));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
    
    
        
    		
   	   	    
               
   