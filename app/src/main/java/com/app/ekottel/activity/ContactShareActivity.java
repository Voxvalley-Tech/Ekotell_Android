package com.app.ekottel.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.adapter.ShareConatctsAdapter;
import com.app.ekottel.model.ContactsModel;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;


import java.util.ArrayList;

public class ContactShareActivity extends AppCompatActivity {
    private RecyclerView mContactRecyclerView;
    public static Toolbar mToolbar;
    private ArrayList<ContactsModel> contactList = new ArrayList<>();
    public static ArrayList<ContactsModel> selectedContactsList = new ArrayList<>();
    private FloatingActionButton shareContactImg;
    private String TAG = "ContactShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_share);
        mToolbar = findViewById(R.id.toolbar);
        mContactRecyclerView = findViewById(R.id.contact_share_recycler_view);
        shareContactImg = findViewById(R.id.share_contact_img);
        mToolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getBooleanExtra("isCameForContactShare", false)) {
            mToolbar.setSubtitle("Share contacts");
        } else {
            mToolbar.setSubtitle("Select contact to Forward");
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mContactRecyclerView.setLayoutManager(linearLayoutManager);
        loadContacts();

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        shareContactImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedContactsList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "At least 1 contact must be selected", Toast.LENGTH_SHORT);
                } else {
                    ArrayList<String> contactNames = new ArrayList<>();
                    ArrayList<String> contactNumbers = new ArrayList<>();
                    ArrayList<String> lables = new ArrayList<>();
                    for (int i = 0; i < selectedContactsList.size(); i++) {
                        contactNames.add(selectedContactsList.get(i).getContactName());
                        contactNumbers.add(selectedContactsList.get(i).getContactNumber());
                        lables.add("Mobile");
                    }
                    Intent intent = new Intent();
                    intent.putExtra("contactNames", contactNames);
                    intent.putExtra("contactNumbers", contactNumbers);
                    intent.putExtra("labes", lables);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void loadContacts() {
        Cursor cursor = null;
        if (getIntent().getBooleanExtra("isCameForContactShare", false)) {
            cursor = CSDataProvider.getContactsCursor();
        } else {
            cursor = CSDataProvider.getAppContactsCursor();
        }

        selectedContactsList.clear();
        if (cursor.moveToNext()) {
            do {
                ContactsModel contactsModel = new ContactsModel();
                contactsModel.setContactName(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME)));
                contactsModel.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER)));
                contactsModel.setConatctID(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID)));
                contactList.add(contactsModel);
            } while (cursor.moveToNext());
        }
        ShareConatctsAdapter shareConatctsAdapter = new ShareConatctsAdapter(getApplicationContext(), contactList);
        mContactRecyclerView.setAdapter(shareConatctsAdapter);
    }
}
