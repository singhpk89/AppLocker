package com.twinone.locker.pro;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twinone.locker.MainActivity;
import com.twinone.locker.R;

public class ProActivity extends Activity implements OnClickListener {

	private LinearLayout mPvFree;
	private LinearLayout mPvAds;
	private LinearLayout mPvPaid;
	private LinearLayout mRoot;
	private LinearLayout mLlFeatures;
	private ProUtils mProUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pro);
		mProUtils = new ProUtils(this);

		mRoot = (LinearLayout) findViewById(R.id.prollRoot);
		mLlFeatures = (LinearLayout) findViewById(R.id.prollFeatures);
		mPvFree = (LinearLayout) findViewById(R.id.proItemFree);
		mPvAds = (LinearLayout) findViewById(R.id.proItemAds);
		mPvPaid = (LinearLayout) findViewById(R.id.proItemPaid);

		mPvFree.setOnClickListener(this);
		mPvAds.setOnClickListener(this);
		mPvPaid.setOnClickListener(this);
		mPvPaid.setVisibility(View.GONE);

		String[] fts = getResources()
				.getStringArray(R.array.pro_features_array);
		for (String ft : fts) {
			TextView tv = new TextView(this);
			tv.setText(Html.fromHtml("&#8226; " + ft));
			mLlFeatures.addView(tv);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
	}

	private void updateLayout() {
		int type = mProUtils.getProType();
		mPvFree.setBackgroundResource(type == ProUtils.TYPE_FREE ? R.drawable.pro_overlay_stroke
				: R.drawable.pro_overlay);
		mPvAds.setBackgroundResource(type == ProUtils.TYPE_ADS ? R.drawable.pro_overlay_stroke
				: R.drawable.pro_overlay);
		mPvPaid.setBackgroundResource(type == ProUtils.TYPE_PAID ? R.drawable.pro_overlay_stroke
				: R.drawable.pro_overlay);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.proItemFree:
			mProUtils.setProType(ProUtils.TYPE_FREE);
			break;
		case R.id.proItemAds:
			mProUtils.setProType(ProUtils.TYPE_ADS);
			break;
		case R.id.proItemPaid:
			mProUtils.setProType(ProUtils.TYPE_PAID);
			break;
		}
		updateLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MainActivity.showWithoutPassword(this);
	}

}
