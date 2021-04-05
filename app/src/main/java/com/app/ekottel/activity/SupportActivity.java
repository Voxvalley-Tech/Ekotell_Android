package com.app.ekottel.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to display list of features related support.
 *
 * @author Ramesh U
 * @version 2017
 */
public class SupportActivity extends AppCompatActivity {
    private TextView mTvVersion, mTvInfo;
    private TextView mTvTopupHeader;
    public static String versionNum = "";
    private Typeface webTypeFace;
    private int[] icons = {R.drawable.ic_contact_support, R.drawable.ic_faq, R.drawable.ic_terms_conditions};
    private String[] mTitles;
    private SupportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        mTitles = new String[]{getString(R.string.support_contact_support), getString(R.string.support_faqs), getString(R.string.support_terms_and_condition)};

        mTvInfo = (TextView) findViewById(R.id.tv_support_info);

        webTypeFace = Utils.getTypeface(getApplicationContext());

        mTvVersion = (TextView) findViewById(R.id.tv_support_version);
        mTvTopupHeader = (TextView) findViewById(R.id.tv_support_header);
        ListView supportList = (ListView) findViewById(R.id.support_list);
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_support_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_support_back_arrow);
        if(ll_back!=null) {
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            versionNum = pinfo.versionName;

        } catch (Exception e) {
            e.printStackTrace();
            versionNum = getString(R.string.splash_version_number);

        }
        mTvVersion.setText(getString(R.string.splash_version_number_single) + versionNum);

        mTvInfo.setText(getString(R.string.splash_version_name));

        if(back!=null)
        back.setTypeface(webTypeFace);


        adapter = new SupportAdapter(mTitles, icons);
        if(supportList!=null) {
            supportList.setAdapter(adapter);
            supportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            startActivity(new Intent(getApplicationContext(), ContactSupportActivity.class));
                            break;
                        case 1:
                            startActivity(new Intent(getApplicationContext(), FaqsActivity.class));
                            break;
                        case 2:
                            startActivity(new Intent(getApplicationContext(), TermsAndConditionsActivity.class));
                            break;
                        /*case 3:

                            Intent intent = new Intent(getApplicationContext(), HelpScreenActivity.class);
                            intent.putExtra(getString(R.string.support_intent_more), getString(R.string.support_intent_more));
                            startActivity(intent);
                            break;*/

                    }
                }
            });
        }


    }

    class SupportAdapter extends ArrayAdapter<String> {

        private String[] titles;
        private int[] icons;

        public SupportAdapter(String[] titles, int[] icons) {
            super(getApplicationContext(), R.layout.more_list_item, titles);
            this.titles = titles;
            this.icons = icons;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.more_list_item, null);
            }
            ImageView icon = convertView.findViewById(R.id.icon);
            TextView title = convertView.findViewById(R.id.title);
            TextView go =  convertView.findViewById(R.id.next);
            Typeface text_regular = Utils.getTypeface(getApplicationContext());
            go.setTypeface(webTypeFace);
            icon.setImageResource(icons[position]);
            title.setText(titles[position]);
            return convertView;
        }
    }

}
