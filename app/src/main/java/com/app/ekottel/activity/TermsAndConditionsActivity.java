package com.app.ekottel.activity;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

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
 * This activity is used to display terms and conditions data.
 *
 * @author Ramesh U
 * @version 2017
 */
public class TermsAndConditionsActivity extends AppCompatActivity {
    private TextView mTvHeader;
    WebView mWebView;
    private String TAG;
    ProgressBar pb_terms_conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TAG = getString(R.string.terms_and_condition_tag);
        mTvHeader = (TextView) findViewById(R.id.tv_help_header);
        pb_terms_conditions = (ProgressBar) findViewById(R.id.pb_terms_conditions);

        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_terms_condition_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_terms_condition_back_arrow);
        if (ll_back != null) {
            back.setTypeface(webTypeFace);
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if (!is3g && !isWifi) {

            Toast.makeText(getApplicationContext(), getString(R.string.splash_network_message), Toast.LENGTH_LONG).show();
            return;
        }
        mWebView = (WebView) findViewById(R.id.term_web_view);
        mWebView.setVisibility(View.GONE);
        mWebView.setWebViewClient(new MyWebViewClient());

        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.loadUrl(Constants.TERM_AND_CONDITION_URL);
        mWebView.getSettings().setSupportMultipleWindows(true);
    }

    //This is used for load dat
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mWebView.setVisibility(View.VISIBLE);
            if (pb_terms_conditions != null)
                pb_terms_conditions.setVisibility(View.GONE);


            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        /*    try {
                if (pb_terms_conditions != null)
                    pb_terms_conditions.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }*/


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
            mWebView.clearFocus();
            super.onBackPressed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mWebView != null)
            mWebView.destroy();

        if (pb_terms_conditions != null)
            pb_terms_conditions.setVisibility(View.GONE);
    }
}
