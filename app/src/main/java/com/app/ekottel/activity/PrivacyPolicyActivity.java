package com.app.ekottel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.ekottel.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    TextView tv_allow,tv_deny;




    String htmlString2 =
            "<p>Privacy Policy</p>\n" +
                    "<p>Last updated September 2021</p>\n" +
                    "<p>The ekottel service is provided by H&amp;N Marketing and Communication</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>Be confidential because we assure the protection of your Privacy and all data we collected from you.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>INFORMATION WE NEED.</p>\n" +
                    "<p>Ekottel need a Valid Phone number in order to access the service i.e. getting verification code.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>AGE PERMISSION: You must be at least 18 years of age to use ekottel.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>SECURITY: Your mobile phone number will be used as the main account username, and also it will be your caller Id in order to identify who you are and where are calling from.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>We may monitor accounts and usage for suspicious activity to provide security and safety on our service, and to prevent abuse, fraud, illegal usage, and breaches of our terms and conditions.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>According to law of Federal Republic of Nigeria (FRN) and Nigerian Communications Commission (NCC) no illegal act will be allowed. If account found with illegal act, it will be blocked forever and will not will no longer use our service.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>We were partnered with European communication companies for better and good service.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p>PRIVACY POLICY CHANGES:</p>\n" +
                    "<p>We are viewing this Policy time to time, anytime we update this policy we will notify you</p>";
                 /*
                    "<p>Users are responsible for any third-party Personal Data obtained, published or shared through this Application and confirm that they have the third party's consent to provide the Data to the Owner.</p>\n"+
                    "<li>CallMeSoft's Privacy Policy does not apply to other advertisers or websites. Thus, we are advising you to consult the respective Privacy Policies of these third-party ad servers for more detailed information. It may include their practices and instructions about how to opt-out of certain options. You may find a complete list of these Privacy Policies and their links here: Privacy Policy Links.</li> \n" +
                    "\n" +*/









    TextView tv_help_one_text_footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        tv_deny=findViewById(R.id.tv_deny);
        tv_allow=findViewById(R.id.tv_allow);
        tv_help_one_text_footer=findViewById(R.id.tv_help_one_text_footer);
        tv_help_one_text_footer.setMovementMethod(new ScrollingMovementMethod());
        //tv_help_one_text_footer.setText(Html.fromHtml(htmlString2));
        tv_help_one_text_footer.setText(Html.fromHtml("<p>Privacy Policy<p>\n" +
                "<p> Last updated September 2021 <p>\n" +
                "<p>The ekottel service is provided by H&amp;N Marketing and Communication</p>\n" +
                "\n" +
                "<p>Be confidential because we assure the protection of your Privacy and all data we collected from you.</p>\n" +
                "\n" +
                "<p>INFORMATION WE NEED. Ekottel need a Valid Phone number in order to access the service i.e. getting verification code.</p>\n" +
                "\n" +
                "<p>AGE PERMISSION: You must be at least 18 years of age to use ekottel.</p>\n" +
                "\n" +
                "<p>SECURITY: Your mobile phone number will be used as the main account username, and also it will be your caller Id in order to identify who you are and where are calling from.</p>\n" +
                "\n" +
                "<p>We may monitor accounts and usage for suspicious activity to provide security and safety on our service, and to prevent abuse, fraud, illegal usage, and breaches of our terms and conditions.</p>\n" +
                "\n" +
                "<p>According to law of Federal Republic of Nigeria (FRN) and Nigerian Communications Commission (NCC) no illegal act will be allowed. If account found with illegal act, it will be blocked forever and will not will no longer use our service.</p>\n" +
                "\n" +
                "<p>We were partnered with European communication companies for better and good service.</p>\n" +
                "\n" +
                "<p>PRIVACY POLICY CHANGES: We are viewing this Policy time to time, anytime we update this policy we will notify you.</p>"



        ));
        tv_allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ExistingUserOrNotActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tv_deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
