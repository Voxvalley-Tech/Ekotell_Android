package com.app.ekottel.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;

import com.app.ekottel.R;

/**
 * This activity is used to display dialog when long running operations.
 *
 * @author Ramesh U
 * @version 2017
 */
public class Progressdialog_custom extends Dialog {

	@SuppressWarnings("unused")
	private ProgressBar mProgress;
	public Progressdialog_custom(Context context) {
		 super(context,R.style.Theme_Dialog1);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prograss_main);
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		

		
	}


}