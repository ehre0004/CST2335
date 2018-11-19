package com.example.rae.androidlabs;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MessageDetails extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        Fragment mFragment = new MessageFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mFragment.setArguments(getIntent().getExtras());
        transaction.add(R.id.details_layout, mFragment).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
