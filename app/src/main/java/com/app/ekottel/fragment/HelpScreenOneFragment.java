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

public class HelpScreenOneFragment extends Fragment {

    private TextView mTvHeader, mTvFooter, mTvHelpOneWeb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.helpscreen_one_fragment,
                container, false);

        mTvHeader = (TextView) view.findViewById(R.id.tv_help_one_text_header);

        mTvFooter = (TextView) view.findViewById(R.id.tv_help_one_text_footer);
        mTvHelpOneWeb = (TextView) view.findViewById(R.id.tv_help_one_web);

        return view;
    }
}
