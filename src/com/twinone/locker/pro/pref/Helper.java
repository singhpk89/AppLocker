package com.twinone.locker.pro.pref;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twinone.locker.pro.ProUtils;

public class Helper {

	private Context mContext;
	private ProUtils mProUtils;

	public Helper(Context context) {
		mContext = context;
		mProUtils = new ProUtils(context);
	}

	/**
	 * 
	 * @return true if the super method should be executed
	 */
	public boolean onClick() {
		if (!mProUtils.proFeaturesEnabled()) {
			mProUtils.showDialogIfProNotEnabled();
			return false;
		} else {
			return true;
		}
	}

	public View getView(View v) {
		TextView tv = new TextView(mContext);
		tv.setText("PRO");
		tv.setBackgroundColor(Color.parseColor("#e74c3c"));
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(10);
		int paddingV = 2 * (int) mContext.getResources().getDisplayMetrics().density;
		int paddingH = 3 * (int) mContext.getResources().getDisplayMetrics().density;
		tv.setPadding(paddingH, paddingV, paddingH, paddingV);
		((ViewGroup) v).addView(tv);
		return v;
	}
}
