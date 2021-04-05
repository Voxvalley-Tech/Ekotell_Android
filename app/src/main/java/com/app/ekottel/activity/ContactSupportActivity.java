package com.app.ekottel.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to display support information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ContactSupportActivity extends AppCompatActivity {
    private TextView mTvHeader;
    TextView chatWithUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mTvHeader = (TextView) findViewById(R.id.tv_contact_support_header);
        chatWithUs = findViewById(R.id.chat_with_us);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_contact_support_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_contact_support_back_arrow);
        back.setTypeface(webTypeFace);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chatWithUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, "+919963474576");
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, "support");
                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

                finish();
            }
        });

    }

}
