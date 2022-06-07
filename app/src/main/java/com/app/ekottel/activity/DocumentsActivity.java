package com.app.ekottel.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.app.ekottel.R;
import com.app.ekottel.adapter.DocumentsAdapater;
import com.app.ekottel.model.DocumentsModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DocumentsActivity extends AppCompatActivity {
    private File root;
    private ArrayList<File> fileList = new ArrayList<File>();
    private ArrayList<DocumentsModel> myDocsList = new ArrayList<>();
    private LinearLayout view;
    RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private RelativeLayout mTitileLayout, mSearchLayout;
    private ImageView mSearchImg, mSearchCancelImg;
    private EditText mSearchEdt;
    private DocumentsAdapater mDocumentsAdapater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        recyclerView = findViewById(R.id.view);
        toolbar = findViewById(R.id.documents_toolbar);
        mTitileLayout = findViewById(R.id.titile_layout);
        mSearchLayout = findViewById(R.id.search_layout);
        mSearchImg = findViewById(R.id.search_img);
        mSearchCancelImg = findViewById(R.id.search_cancel_img);
        mSearchEdt = findViewById(R.id.documents_search_edt);
        //getting SDcard root path
        root = new File(getExternalFilesDir(null)
                .getAbsolutePath());
        new getDocumnetsTask().execute();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchLayout.getVisibility() == View.VISIBLE) {
                    mSearchEdt.setText("");
                    mSearchCancelImg.setVisibility(View.GONE);
                    mSearchLayout.setVisibility(View.GONE);
                    mTitileLayout.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchEdt.getWindowToken(), 0);
                } else {
                    finish();
                }
            }
        });
        mSearchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchLayout.setVisibility(View.VISIBLE);
                mTitileLayout.setVisibility(View.GONE);
                mSearchEdt.setFocusable(true);
            }
        });
        mSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    mSearchCancelImg.setVisibility(View.GONE);
                } else {
                    mSearchCancelImg.setVisibility(View.VISIBLE);
                }
                documentsSearchFilter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public ArrayList<DocumentsModel> getfile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    // fileList.add(listFile[i]);
                    LOG.info("MainActivity", "folders list: " + listFile[i]);
                    if (listFile[i].getName().equalsIgnoreCase("Android") || listFile[i].getName().startsWith(".")) {
                        continue;
                    }
                    getfile(listFile[i]);
                } else {
                    String mimeType = getMimeType(listFile[i].getAbsolutePath());
                    //    LOG.info("MainActivity", "getfile: mimeType " + getMimeType(listFile[i].getAbsolutePath()));
                    if (mimeType != null && !mimeType.contains("image/") && !mimeType.contains("video/") && !mimeType.contains("audio/")) {
                        DocumentsModel documentsModel = new DocumentsModel();
                        documentsModel.setFileName(listFile[i].getName());
                        documentsModel.setFilePath(listFile[i].getAbsolutePath());
                        documentsModel.setCreatedTime(listFile[i].lastModified());
                        myDocsList.add(documentsModel);
                    }/*else if(mimeType==null&&!listFile[i].getName().startsWith(".")){
                        DocumentsModel documentsModel = new DocumentsModel();
                        documentsModel.setFileName(listFile[i].getName());
                        documentsModel.setFilePath(listFile[i].getAbsolutePath());
                        documentsModel.setCreatedTime(listFile[i].lastModified());
                        myDocsList.add(documentsModel);
                    }*/
                }

            }
        }
        return myDocsList;
    }


    public void clearSearchField(View view) {
        mSearchEdt.setText("");
    }

    private class getDocumnetsTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Getting documents Please wait.");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getfile(root);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           /* if (progressDialog != null) {
                progressDialog.dismiss();

            }*/
            Collections.sort(myDocsList, new Comparator<DocumentsModel>() {
                @Override
                public int compare(DocumentsModel o1, DocumentsModel o2) {
                    String createdTime1 = o1.getFileName().toUpperCase();
                    String createdTime2 = o2.getFileName().toUpperCase();
                    return createdTime1.compareTo(createdTime2);
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            mDocumentsAdapater = new DocumentsAdapater(myDocsList, DocumentsActivity.this);
            recyclerView.setAdapter(mDocumentsAdapater);
        }
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * This method filters the contacts based on given string
     *
     * @param searchText
     */
    public void documentsSearchFilter(String searchText) {
        //new array list that will hold the filtered data
        ArrayList<DocumentsModel> filterdNames = new ArrayList<>();
        ArrayList<DocumentsModel> totalDocuments;
        totalDocuments = myDocsList;


        //looping through existing elements
        for (DocumentsModel s : totalDocuments) {
            //if the existing elements contains the search input
            if (s.getFileName().toLowerCase().contains(searchText.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        //calling a method of the adapter class and passing the filtered list
        if (mDocumentsAdapater != null)
            mDocumentsAdapater.filterList(filterdNames);
    }

}
