package com.app.ekottel.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.ekottel.R;

/**
 * Created by ramesh.u on 5/17/2017.
 */

public class HelpScreenTwoFragment extends Fragment {
    private TextView mTvHeader, mTvFooter, mTvHelpTwoWeb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.helpscreen_two_fragment,
                container, false);
        mTvHeader = (TextView) view.findViewById(R.id.tv_help_two_text_header);

        mTvFooter = (TextView) view.findViewById(R.id.tv_help_two_text_footer);
        mTvHelpTwoWeb = (TextView) view.findViewById(R.id.tv_help_two_web);
        return view;
    }


}
