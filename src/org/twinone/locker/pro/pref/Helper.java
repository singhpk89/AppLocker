package org.twinone.locker.pro.pref;

import org.twinone.locker.pro.ProUtils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Helper {

	private Context mContext;
	private ProUtils mProUtils;

	public void setProUtils(ProUtils proUtils) {
		this.mProUtils = proUtils;
	}

	public Helper(Context context) {
		mContext = context;
		mProUtils = new ProUtils(context);
	}

	/**
	 * 
	 * @return true if the super method should be executed
	 */
	public boolean onClick() {
		if (mProUtils != null && !mProUtils.proFeaturesEnabled()) {
			mProUtils.showDialogIfProNotEnabled();
			return false;
		} else {
			return true;
		}
	}

	private ViewGroup mRootView;
	private TextView mTvPro;

	public void updateProFlag() {
		boolean show = false;
		if (mProUtils != null) {
			show = !mProUtils.proFeaturesEnabled();
		}
		if (mTvPro != null) {
			mTvPro.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}

	public View getView(View v) {
		mRootView = (ViewGroup) v;
		mTvPro = new TextView(mContext);
		mTvPro.setText("PRO");
		mTvPro.setBackgroundColor(Color.parseColor("#e74c3c"));
		mTvPro.setTextColor(Color.WHITE);
		mTvPro.setTextSize(10);
		int paddingV = 2 * (int) mContext.getResources().getDisplayMetrics().density;
		int paddingH = 3 * (int) mContext.getResources().getDisplayMetrics().density;
		mTvPro.setPadding(paddingH, paddingV, paddingH, paddingV);
		updateProFlag();
		mRootView.addView(mTvPro);
		return v;
	}
}
