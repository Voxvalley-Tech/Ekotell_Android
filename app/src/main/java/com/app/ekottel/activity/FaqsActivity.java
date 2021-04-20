package com.app.ekottel.activity;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to display faqs information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class FaqsActivity extends AppCompatActivity {
    TextView mTvHeader,tv_faqsContent;
    WebView mWebView;
    private String TAG;
    ProgressBar pb_faq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);
        TAG = getString(R.string.faq_screen_tag);
        mTvHeader = (TextView) findViewById(R.id.tv_faq_header);
        pb_faq=(ProgressBar)findViewById(R.id.pb_faq);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_faqs_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_faqs_back_arrow);
        tv_faqsContent = findViewById(R.id.tv_faqs_content);
        if (back != null)
            back.setTypeface(webTypeFace);

        if (ll_back != null) {
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        mWebView.clearFocus();
                        finish();

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        tv_faqsContent.setText(Html.fromHtml("<h3>1. How do I sign up for  Ekottel International calling APP?</h3>" +
                "</br>" +
                "<p>You can sign up by downloading our Award-Winning free Ekottel App from the App Store or Google Play Store and create your account, follow these steps to start calling your loved ones:<br/>" +
                "You don't need to type \"+\"  it is added automatically. No 0 or 00 or 011 before or after the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! Then simply search for the contact you want to call or dial a number on the keypad.Now you can start talking!<br/>" +
                "Example: +291 717XX93, +251 9659XXX88<br/></p>" +

                "<h3>2. How do I add Funds, TopUp/ Recharge For International Calling ?<br/></h3>" +
                "<p><ul>" +
                "<li> Purchase a subscription or World Credits By going to Settings then Tap on Subscribe Packages or TopUp/Recharge Using Credit/debit cards and Paypal Account so you have an active Calling offer</li>" +
                "<li> If you don't have Credit/debit cards and Paypal Account. <br/>" +
                "You can add Funds, TopUp/Recharge from one of our Local retailers or Ekottel Distributors by going to Settings “VOUCHER RECHARGE”  so you have an active calling offer</li>" +
                "<li> Then simply search for the contact you want to call or dial a number on the keypad</li>" +
                "</ul>" +
                "</br>" +
                "Tip:  Make sure to save your contacts in your native phone device in the international calling format +291 xxxxxx, so Ekottel can identify them and connect your calls. <br/>" +
                "</p>" +


                "<h3>3. How do I login to my Ekottel International Calling Account ?<br/></h3>" +
                "<p>" +
                "<ul>" +
                "<li> Goto Ekottelcalls.com login page <br/></li>" +
                "<li> Enter your registered phone number with “+” sign  and password or security code.<br/></li>" +
                "<li> Press the Login button.<br/></li>" +
                "</ul>" +
                "</p>" +


                "<h3>4. What is Ekottel and why should I use it?<br/></h3>" +
                "<p>Ekottel is an application that lets you make free HD-quality calls to other Ekottel App users and  Low cost High quality International calls to any phone (mobile or landline) all over the world. All at low rates! Ekottel uses your cell phone’s internet connection, be it WiFi, 3G, 4G/LTE instead of your phone’s voice network. Your friends and family always get calls from your personal phone number. They know it’s you and can even call you back!<br/></p>" +

                "<h3>5. Which calls are free?<br/></h3>" +
                "<p>All Ekottel to Ekottel App to App Audio and video calls are completely free. Moreover, it is really easy to earn free credits to call landlines and mobiles by inviting friends.<br/>" +
                "*Please note that data charges may be applied by your service provider if you are using a cellular internet connection<br/></p>" +


                "<h3>6. I didn't get a message with the verification code?</h3>" +
                "<ul>" +
                "<li> Please ensure you enter your phone number in international format with the country code.<br/>" +
                "Example: +291 717xx93, +251 96xxx0088<br/>" +
                "You don't need to type \"+\" - it is added automatically. No 0 or 00 or 011 after or before the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! <br/></li>" +
                "<li> If the validation message doesn't come, please wait for a validation call or try again<br/></li>" +
                "<li> Some VoIP services may be blocked by Internet providers in some Countries. To make sure Ekottel App calls are not blocked just try to open Ekottelcalls.com in your mobile web browser. If you can't open it, try using some other Internet connection.<br/></li>" +
                "</ul>" +


                "<h3>7. Does Ekottel charge any extra fees for International Calls ?<br/></h3>" +
                "<p>There is a fixed per-minute rate that you see before making your call to cellular and landline phones. There are no hidden charges or connection fees in Ekottel App.<br/>" +
                "*Please note that data charges may be applied by your service provider if you are using cellular internet connection.<br/></p>" +


                "<h3>8.Will Ekottel run on my device?<br/></h3>" +
                "<p>Kodate is available for:<br/>" +
                "<ul>" +
                "<li> iPhone® 4s and higher (OS 8.0 and higher);<br/></li>" +
                "<li> iPad® (OS 8.0 and higher);<br/></li>" +
                "<li> Android™ phones (OS 4.4 and higher);<br/></li>" +
                "<li> Android™ tablets (OS 4.4 and higher).<br/></li>" +
                "</ul>" +
                "</p>" +


                "<h3>9. How do I report a bug or quality issue?<br/></h3>" +
                "<p>Please go to the Setting in Android or iOS, choose or Tap on Support Contact Support, and describe the issue that you are experiencing.<br/></p>" +


                "<h3>10. How can I enable or disable auto top-up?<br/></h3>" +
                "<p>We strongly recommend you check the auto top-up checkbox after successful payment with your credit or debit card. This setting automatically tops-up your Ekottel balance by $5 when the balance falls below $2.<br/>" +
                "You can disable auto top-up any time in the My balance section. <br/></p>" +


                "<h3>11. What are subscriptions?<br/></h3>" +
                "<p>" +
                "If you want to call home a lot at the lowest price possible, Ekottel’s  unlimited subscriptions are your best bet! We offer subscriptions depending on the country you wish to call:<br/>" +
                "<ul><li> 30-day subscription: available for 40 countries<br/></li>" +
                "<li> 90-day subscription: currently available for Eritrea, Ethiopia and  India <br/></li>" +
                "<li> 180-day subscription: currently available for Eritrea, Ethiopia and India </li></ul>" +
                "</br>" +
                "When you buy one of these subscriptions, you can call any mobile or landline in a select country however much you want during the period of time specified in the subscription. Your subscription begins on the day you buy it. It will renew automatically for 30/90/180 days, depending on the subscription you have. You can cancel your subscription at any time at Ekottel calls.com in your Ekottel  App. <br/>" +
                "Note: Deals/subscriptions are reflected as minutes, not credit, and that's why you don't see it in your balance as additional money You can see It under your Profile.<br/>" +
                "</p>" +


                "<h3>12. Ekottel welcome offers:<br/></h3>" +
                "<p>If you’re new to Ekottel, you can try one of our welcome offers for subscriptions! Welcome offers give you a certain period of  calling for free (outlined below) depending on the subscription you buy. After your welcome offer ends, the subscription will be renewed automatically, but you can always cancel the subscription at Ekottelcalls.com in your Ekottel App. 30-day subscription: 7 days free.<br/>" +
                "<ul><li> 90-day subscription: 1 month free</li>" +
                "<li> 180-day subscription: 1 month free</li></ul>" +
                "</br>" +
                "Note: Subscriptions are non-transferable, non-refundable, and for private use only. Premium, special, service, and non-geographic numbers are excluded. We also reserve the right to block any number that is exhibiting unusual calling patterns as flagged by our system, such as excessive conferencing or call forwarding, excessive numbers of regular calls of short duration, calls to multiple numbers in a short period of time, and more. For more details, please see our Terms and Conditions.<br/>" +
                "VAT: VAT will be charged depending on the country (EU or Australia) from which you are using the service.<br/>" +
                "</p>"


        ));

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if (!is3g && !isWifi) {

            Toast.makeText(getApplicationContext(), getString(R.string.splash_network_message), Toast.LENGTH_LONG).show();
            return;
        }
        mWebView = (WebView) findViewById(R.id.faq_web_view);
        mWebView.setVisibility(View.GONE);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.setWebViewClient(new MyWebViewClient());

        mWebView.loadUrl(Constants.FAQ_URL);
        mWebView.getSettings().setSupportMultipleWindows(true);


    }


    //This class is used for loading data whatever you provided url
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (pb_faq != null)
                pb_faq.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (pb_faq != null)
                pb_faq.setVisibility(View.VISIBLE);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                    boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

                    if (!is3g && !isWifi) {
                        Toast.makeText(getApplicationContext(), getString(R.string.splash_network_message), Toast.LENGTH_LONG).show();
                        finish();
                    }


                }
            }, 600);


        }



    }

    @Override
    public void onBackPressed() {
        try {

            LOG.info("BAck CAlled ");
            LOG.info("BAck CAlled inside");

            mWebView.clearFocus();
            super.onBackPressed();


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pb_faq != null)
            pb_faq.setVisibility(View.GONE);
    }
}
