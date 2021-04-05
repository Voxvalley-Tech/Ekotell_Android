package com.app.ekottel.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.HelpScreenPagerAdapter;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to display user is understand this application related text.
 *
 * @author Ramesh U
 * @version 2017
 */
public class HelpScreenActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    HelpScreenPagerAdapter mViewPagerAdapter;
    private TextView mBtnCircle, mBtnCircle2, mBtnCircle3, mBtnCircle4, mBtnCircle5;
    TextView mTvRegister, mTvSkip;
    Typeface mTextFont;
    private int currentPosition = 0;
    private String str = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_screen);

        mTextFont = Utils.getTypeface(getApplicationContext());


        initButton();
        setTab();
    }


// This method is used to perform actions while using view pager

    private void setTab() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if (position == 0) {

                    mTvRegister.setText(getResources().getString(R.string.help_screen_skip));
                    mTvSkip.setText(getResources().getString(R.string.help_screen_next));
                    mBtnCircle.setText(getResources().getString(R.string.icon_fill_circle));
                    mBtnCircle2.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle3.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle4.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle5.setText(getResources().getString(R.string.icon_circle));
                } else if (position == 1) {
                    mTvRegister.setText(getResources().getString(R.string.help_screen_skip));
                    mTvSkip.setText(getResources().getString(R.string.help_screen_next));
                    mBtnCircle.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle2.setText(getResources().getString(R.string.icon_fill_circle));
                    mBtnCircle3.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle4.setText(getResources().getString(R.string.icon_circle));
                    mBtnCircle5.setText(getResources().getString(R.string.icon_circle));
                }


                btnAction(position);
            }
        });
    }


    private void btnAction(int action) {
        switch (action) {
            case 0:

                mBtnCircle.setTypeface(mTextFont);

                mBtnCircle.setText(getResources().getString(R.string.icon_fill_circle));


                break;
            case 1:
                mBtnCircle2.setTypeface(mTextFont);

                mBtnCircle2.setText(getResources().getString(R.string.icon_fill_circle));


                break;
            case 2:
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();

                break;
            case 3:
                mBtnCircle4.setTypeface(mTextFont);

                mBtnCircle4.setText(getResources().getString(R.string.icon_fill_circle));

                break;
            case 4:
                mBtnCircle5.setTypeface(mTextFont);

                mBtnCircle5.setText(getResources().getString(R.string.icon_fill_circle));

                break;

        }
    }

    // This method is initialize all variables if required and perform actions to navigate next screen
    private void initButton() {

        mViewPager = (ViewPager) findViewById(R.id.imageviewPager);
        mViewPagerAdapter = new HelpScreenPagerAdapter(HelpScreenActivity.this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(0);

        mTvRegister = (TextView) findViewById(R.id.tv_register);
        mTvSkip = (TextView) findViewById(R.id.tv_skip);


        Intent intent = getIntent();

        str = intent.getStringExtra(getString(R.string.help_screen_prf_more));

        if (str != null && !str.isEmpty()) {
            mTvRegister.setVisibility(View.INVISIBLE);
            mTvSkip.setVisibility(View.INVISIBLE);
        } else {
            mTvRegister.setVisibility(View.VISIBLE);
            mTvSkip.setVisibility(View.VISIBLE);
        }



        mBtnCircle = (TextView) findViewById(R.id.btn1);

        mBtnCircle2 = (TextView) findViewById(R.id.btn2);
        mBtnCircle3 = (TextView) findViewById(R.id.btn3);
        mBtnCircle4 = (TextView) findViewById(R.id.btn4);
        mBtnCircle5 = (TextView) findViewById(R.id.btn5);
        mBtnCircle.setTypeface(mTextFont);

        mBtnCircle2.setTypeface(mTextFont);
        mBtnCircle3.setTypeface(mTextFont);
        mBtnCircle4.setTypeface(mTextFont);
        mBtnCircle5.setTypeface(mTextFont);
        mBtnCircle.setText(getResources().getString(R.string.icon_fill_circle));
        mBtnCircle2.setText(getResources().getString(R.string.icon_circle));
        mBtnCircle3.setText(getResources().getString(R.string.icon_circle));
        mBtnCircle4.setText(getResources().getString(R.string.icon_circle));
        mBtnCircle5.setText(getResources().getString(R.string.icon_circle));

        mTvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mTvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPosition == 0) {
                    mViewPager.setCurrentItem(1);
                } else {
                    Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }
   /* @Override
    public void onBackPressed() {
        Intent intent=new Intent(this,PrivacyPolicyActivity.class);
        startActivity(intent);
        finish();
    }*/
}
