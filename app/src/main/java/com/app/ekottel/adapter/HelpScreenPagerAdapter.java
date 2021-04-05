package com.app.ekottel.adapter;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.ekottel.fragment.HelpScreenOneFragment;
import com.app.ekottel.fragment.HelpScreenTwoFragment;

/**
 * Created by ramesh.u on 5/17/2017.
 */

public class HelpScreenPagerAdapter extends FragmentPagerAdapter {
    private Context _context;
    public static int totalPage = 2;

    public HelpScreenPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        _context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = new Fragment();
        switch (position) {
            case 0:
                f = new HelpScreenOneFragment();
                break;
            case 1:
                f = new HelpScreenTwoFragment();
                break;


        }
        return f;
    }

    @Override
    public int getCount() {
        return totalPage;
    }
}



