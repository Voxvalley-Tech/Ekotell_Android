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
   // WebView mWebView;
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

                       // mWebView.clearFocus();
                        finish();

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        tv_faqsContent.setText(Html.fromHtml("<h3>1. HOW DO I SIGN UP FOR EKOTTEL INTERNATIONAL CALLING APP?</h3>\n" +
                "\n" +
                "<p> You can sign up by downloading our Award-Winning free EKOTTEL App from the App Store or Google Play Store and create your account, follow these steps to start calling your loved ones: You don't need to type &quot;+&quot; it is added automatically. Use +, 00, 011 before the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! Then simply search for the contact you want to call or dial a number on the keypad. Now you can start talking! Example: +291 736XX99, 00291740XX33 All work.</p>\n" +
                "\n" +
                "<h3>2. DIDN'T GET A MESSAGE WITH THE VERIFICATION CODE?</h3>\n" +
                "\n" +
                "<p> Please ensure you enter your phone number in international format with the country code. Example: +29136XX99, +251 91xxx7586 You don't need to type &quot;+&quot; - it is added automatically. No 0 or 00 or 011 after or before the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! 2. If the validation message doesn't come, please wait for a validation call or try again. 3. Some VoIP services may be blocked by Internet providers in some Countries. To make sure Ekottel App calls are not blocked just try to open http://ekottel.com/ in your mobile web browser. If you can't open it, try using some other Internet connection.</p>\n" +
                "\n" +
                "<h3>3. DOES EKOTTEL CHARGE ANY EXTRA FEES FOR INTERNATIONAL CALLS</h3>\n" +
                "\n" +
                "<p>There is a fixed per-minute rate that you see before making your call to cellular and landline phones. There are no hidden charges or connection fees in ekottel App. *Please note that data charges may be applied by your service provider if you are using cellular internet connection.</p>\n" +
                "\n" +
                "<h3>4. WILL EKOTTEL RUN ON MY DEVICE?</h3>" +
                " <p>Ekottel is available for: &bull; iPhone&reg; 4s and higher (OS 8.0 and higher); &bull; iPad&reg; (OS 8.0 and higher); &bull; Android&trade; phones (OS 4.4 and higher); &bull; Android&trade; tablets (OS 4.4 and higher).</p>\n" +
                "\n" +
                "<h3>5. 1) WHAT IS VOIP, AND HOW DOES IT WORK? <h3> " +
                "<p> a. VoIP (Voice over Internet Protocol) lets you access phone service over the internet. Calls are no longer dependent on old traditional phone lines. VoIP achieves greater functionality at a lower cost for businesses. This innovation means that communication expenses are much cheaper than traditional phone service. Many business owners report cost savings up to 70% after switching to phone service to VoIP. These modern phone systems include many business features that boost productivity and revenue ekottel is one of the highest-rated VoIP providers for businesses. According to customer reviews published on Get VoIP, people enjoy its cost and service. With clients like Conan, Stanley Steamer, Shelby American, and thousands more, you can trust that ekottel will work in your business.</p>\n" +
                "\n" +
                "<h3>6. WHAT CAN EKOTTEL DO FOR ME?</h3>\n" +
                "\n" +
                "<p>a. Ekottel helps thousands of businesses across nearly every industry communicate with their customers. Not only will its VoIP phone system lower costs, but it offers an array of features, including: Customer Relationship Management (CRM) Email, voice, chat, and SMS Productivity tools Case management tools Voicemail-to-email Online faxing On-hold music and messages On-demand call recording Conference calls</p>\n" +
                "\n" +
                "<h3>7. HOW IS THE CALL QUALITY OF A VOIP PHONE?  </h3>" +
                "<p> a. The call quality of cloud communications systems depends on the speed and reliability of your internet. You will hear a vast improvement in call quality compared to traditional landlines. Landlines don&rsquo;t have as much audio bandwidth, which can result in muffled or fuzzy calls. VoIP calls achieve this through HD Voice technology. The number of HD Voice calls you can handle will depend on how fast your internet download and upload speed is. Find out your internet speed by taking the VoIP speed test.</p>\n" +
                "\n" +
                "<h3>8. WHAT SHOULD I LOOK FOR IN A CLOUD COMMUNICATIONS SYSTEM (VOIP)?</h3>\n" +
                "\n" +
                "<p>a. Your requirements depend on how many employees you have and the features you need. Luckily at ekottel, we&rsquo;re able to satisfy the needs of tried-and-true businesses for more than 11 years. Some aspects you want to look for, include: Cost: Calling plans can be on a month-to-month basis or a contract Service: Will you need to be able to contact support during your business hours? Coverage Area: Does the VoIP provider offer calling capabilities where you operate? Users: Does your VoIP provider give you the ability to manage service for multiple users? Functionality: What requirements does your business have that VoIP can fulfill?</p>\n" +
                "\n" +
                "<h3> 9. HOW MUCH DOES VOIP PHONE SERVICE TYPICALLY COST?<h3> " +
                "<p>Ekottel provides three main cloud communications plans: Business Communication Suite starts at $20 per month. SIP Trucking starts at $14.95 per month VoIP Call Center starts at $50 per month Visit the ekottel Pricing page for a straightforward look at all the features and costs. ekottel offers two product suites designed to meet the demands of a growing company. The Business Communication Suite provides all the tools a company needs, including phone service. Alternatively, the Customer Relationship Suite does not include phone service. As for savings, this can vary from business to business. Many companies report savings of up to 70% after switching to VoIP. The more users and phone lines you use, the more you&rsquo;ll save!</p>\n" +
                "\n" +
                "<h3>10. Do I need to sign a contract?</h3>\n" +
                "\n" +
                "<p>a. Ekottel offers the flexibility of month-to-month service for business VoIP. To maximize your savings, ekottell provides annual plans. Not all VoIP providers offer monthly plans, either. And be careful of some annual contracts on the market, they may require payment upfront. ekottel does not require any upfront fees.</p>\n" +
                "\n" +
                "<h3>11. DO I NEED TO HAVE AN EXISTING VOIP PHONE?</h3>\n" +
                "\n" +
                "<p>a. No, you do not need to have an existing VoIP phone. Since VoIP phones adhere to approved internet standards, you can bring your own, buy one, or lease one from your VoIP provider. You can skip the desk phone entirely and use the ekottel App on your desktop, laptop, or smartphone to complete calls over VoIP.</p>\n" +
                "\n" +
                "<h3>12. HOW MANY CALLS CAN I MAKE AT A TIME?</h3>\n" +
                "\n" +
                "<p>a. Ekottel allows for unlimited calls. How many is that? It&rsquo;s up to your devices and your users. A typical VoIP phone supports three simultaneous calls, but that can vary based on the equipment. In contrast, a traditional phone system would require you to roll over lines to manage multiple concurrent calls. Handling numerous calls at once is one of the strengths of VoIP phone service. Callers will never get a busy signal, even if you only have one phone and one phone number.</p>\n" +
                "\n" +
                "<h3>13. IS INTERNET SERVICE INCLUDED WITH YOUR VOIP PHONE SERVICE?</h3>\n" +
                "\n" +
                "<p>a. No, we do not provide internet service as a part of our plans. If you currently have an ISP (Internet Service Provider), you would have no problems with speed or reliability. A business will require at least 100kbps per phone line (or 0.1 Mbps).</p>\n" +
                "\n" +
                "<h3>14. Are there any limitations to using a VoIP phone system?</h3>" +
                " <p> No, there are no practical limitations on our business voice service. You can make as many calls, users, and phone numbers as needed. However, to prevent misuse and abuse, only one phone call can be placed per line per second. (This helps avoid those infamous ekottel Additionally, it won&rsquo;t cost you a fortune to upgrade your current plan. The major strength of our virtual phone system is that it&rsquo;s easy to upgrade as your business grows. The only thing you need to make sure of is that your internet connection is fast and stable. Your call quality and reliability will be dependent on this. For emergency calls, ekottel provides the Enhanced 777(E777) feature. This feature allows public safety staff to know the address of your phone call.</p>\n" +
                "\n" +
                "<h3>15. IF I LOSE POWER, WILL I STILL BE ABLE TO MAKE PHONE CALLS WITH VOIP?</h3>\n" +
                "\n" +
                "<p>a. In most cases, you will not be able to connect calls if there&rsquo;s a power outage. Our VoIP phone system works over the internet. During a power outage, your router will not be able to function, which means there will be no internet access. The only exception to this is if you have a backup power source that could power the router (and therefore maintain your internet connection). If you use Power over Ethernet (PoE), that may need backup power as well. In the event of a power outage, you can instruct your VoIP provider like ekottel to forward calls to a cell phone or direct them to a voicemail box. This solution avoids busy signals and rejected calls for customers.</p>\n" +
                "\n" +
                "<h3>16. WHAT IS A VIRTUAL NUMBER?</h3>\n" +
                "\n" +
                "<p>a. A virtual number is a telephone number that isn&rsquo;t directly associated with a person or business. Virtual phone numbers can also be used to track response rates from specific marketing campaigns. For example, this is great for long-distance callers because they can dial a local virtual number to them and still reach your team.</p>\n" +
                "\n" +
                "<h3>17. WILL I LOSE ANY BUSINESS FEATURES FROM LANDLINES IF I SWITCH TO VOIP?</h3>\n" +
                "\n" +
                "<p>a. No, your business will be able to access 50+ features from ekottel . These are the real deal. For sure, they will almost always include any feature your current landline provider offers. Check out our extensive list of included VoIP features.</p>\n" +
                "\n" +
                "<h3>18. DOES FAXING WORK OVER VOIP?</h3>\n" +
                "\n" +
                "<p>a. Traditional fax machines may be unable to transmit faxes over VoIP lines, so you will want to use an online faxing solution. Ekottel offers unlimited online faxing with its VoIP plans. This feature alone is worth $20 per month&mdash;you get it free with ekottel . See how online faxing can help send and receive faxes easily.</p>\n" +
                "\n" +
                "<h3>19. HOW IS SKYPE OR GOOGLE VOICE DIFFERENT FROM EKOTTEL?</h3>" +
                " <p>At a consumer level, Skype uses the same technology as ekottel &mdash;VoIP. Skype and Google Voice are simple VoIP solutions that allow free messages and calls over the internet meant for personal use. In contrast,ekottel is a complete business communications solution. It covers phone calls, email, SMS, call recording, analytics, CRM, and more. Ekottel also has a dedicated support team and business features to improve productivity and lower costs.</p>\n" +
                "\n" +
                "<h3>20. DOES MY COMPUTER NEED TO BE ONLINE FOR ME TO MAKE A CALL?<h3>" +
                "<p> If you make calls with a VoIP app on your computer, then yes. Otherwise, your computer does not need to be connected. You can complete calls as long as you have internet access and a VoIP-enabled phone or smartphone. Alternatively, you can forward calls to any other phone you have, and callers will never know.</p>\n" +
                "\n" +
                "<h3>21. WHAT ARE INTERNATIONAL CALLING RATES FOR VOIP?</h3>\n" +
                "\n" +
                "<p>a. Ekottel offers some of the lowest international calling rates in the industry. Call rates vary by country, phone provider, and method of calling. For some countries, it can be as low as two cents per minute. View the full list of international calling rates.</p>\n" +
                "\n" +
                "<h3>22. IF MY BUSINESS IS GROWING, CAN I ADJUST MY VOIP PHONE SYSTEM? <h3>" +
                "<p> a. Yes, scalability is one of the advantages of using a VoIP phone system. Because a VoIP phone system is hosted in the cloud, upgrading is easy. There&rsquo;s no need for pricey infrastructure upgrades. In essence, you can scale your company&rsquo;s phone system to an almost unlimited number of users or locations.</p>\n" +
                "\n" +
                "<p></p>\n"));
       /* tv_faqsContent.setText(Html.fromHtml("<h3>1. How do I sign up for  Ekottel International calling APP?</h3>" +
                "</br>" +
                "<p>You can sign up by downloading our Award-Winning free EKOTTEL  App from the App Store or Google Play Store and create your account, follow these steps to start calling your loved ones: You don't need to type \"+\" it is added automatically. Use +, 00, 011 before the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! Then simply search for the contact you want to call or dial a number on the keypad. Now you can start talking! Example: +291 736XX99, 00291740XX33 All work .You can sign up by downloading our Award-Winning free EKOTTEL  App from the App Store or Google Play Store and create your account, follow these steps to start calling your loved ones: You don't need to type \"+\" it is added automatically. Use +, 00, 011 before the country code unless it is the part of your number. If it doesn't help, please send us your phone number and we will try to help! Then simply search for the contact you want to call or dial a number on the keypad. Now you can start talking! Example: +291 736XX99, 00291740XX33 All work .<br/></p>" +

                "<h3>2. 2.\t DIDN'T GET A MESSAGE WITH THE VERIFICATION CODE? ?<br/></h3>" +
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


        ));*/

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if (!is3g && !isWifi) {

            Toast.makeText(getApplicationContext(), getString(R.string.splash_network_message), Toast.LENGTH_LONG).show();
            return;
        }
       /* mWebView = (WebView) findViewById(R.id.faq_web_view);
        mWebView.setVisibility(View.GONE);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.setWebViewClient(new MyWebViewClient());

        mWebView.loadUrl(Constants.FAQ_URL);
        mWebView.getSettings().setSupportMultipleWindows(true);
*/

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
            //mWebView.setVisibility(View.VISIBLE);

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

            //mWebView.clearFocus();
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
