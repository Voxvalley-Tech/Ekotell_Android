package com.app.ekottel.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;

import com.app.ekottel.R;


/**
 * Created by ramesh.u on 11/16/2017.
 */

public class ContactsFragmentActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_fragment_activity);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
