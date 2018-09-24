package com.example.rae.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
    protected static final String ACTIVITY_NAME = "LoginActivity";
    SharedPreferences prefs;
    EditText login, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.i(ACTIVITY_NAME, "in onCreate()");

        // get preferences file
        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        // retrieve preference as string
        String loginName = prefs.getString("loginName", "email@domain.com");
        String password = prefs.getString("password", "password");

        // set login/password EditText fields
        login = findViewById(R.id.editText);
        pass = findViewById(R.id.editText2);
        login.setText(loginName);
        pass.setText(password);

        // reference the login button
        Button loginButt = findViewById(R.id.loginButt);
        loginButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save preferences
                String saveLogin = login.getText().toString();
                String savePass = pass.getText().toString();

                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("loginName", saveLogin);
                edit.putString("password", savePass);
                edit.commit();

                // start next activity
                Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(ACTIVITY_NAME, "in onResume()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(ACTIVITY_NAME, "in onStart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(ACTIVITY_NAME, "in onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(ACTIVITY_NAME, "in onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(ACTIVITY_NAME, "in onDestroy()");
    }
}
