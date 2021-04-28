package com.app.ekottel.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.CallMethodHelper;
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
    TextView chatWithUs,callus,mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mTvHeader = (TextView) findViewById(R.id.tv_contact_support_header);
        chatWithUs = findViewById(R.id.chat_with_us);
        callus = findViewById(R.id.callus);
        mail = findViewById(R.id.mail);
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

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ekottel7@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "App feedback");
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    //ToastUtil.showShortToast(getActivity(), "There are no email client installed on your device.");
                }
            }
        });
        chatWithUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String url = "https://api.whatsapp.com/send?phone="+"447459719982";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);


                /*Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, "+919963474576");
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, "support");
                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

                finish();*/
            }
        });

        callus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_dialpad = new Intent(ContactSupportActivity.this, DialpadActivity.class);
                intent_dialpad.putExtra("DialNumberFromAnotherApp", "+201153430639");
                startActivity(intent_dialpad);
                //CallMethodHelper.processAudioCall(ContactSupportActivity.this, "+201153430639","PSTN");
            }
        });

    }

}
