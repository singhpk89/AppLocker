package com.twinone.locker.pro;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.twinone.locker.MainActivity;
import com.twinone.locker.R;

public class ProActivity extends Activity implements OnClickListener {

	private LinearLayout mPvFree;
	private LinearLayout mPvAds;
	private LinearLayout mPvPaid;

	private ProUtils mProUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pro);
		mProUtils = new ProUtils(this);

		mPvFree = (LinearLayout) findViewById(R.id.proItemFree);
		mPvAds = (LinearLayout) findViewById(R.id.proItemAds);
		mPvPaid = (LinearLayout) findViewById(R.id.proItemPaid);

		mPvFree.setOnClickListener(this);
		mPvAds.setOnClickListener(this);
		mPvPaid.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
	}

	private void updateLayout() {
		int type = mProUtils.getProType();
		mPvFree.setBackgroundResource(type == ProUtils.TYPE_FREE ? R.drawable.overlay_stroke
				: R.drawable.overlay);
		mPvAds.setBackgroundResource(type == ProUtils.TYPE_ADS ? R.drawable.overlay_stroke
				: R.drawable.overlay);
		mPvPaid.setBackgroundResource(type == ProUtils.TYPE_PAID ? R.drawable.overlay_stroke
				: R.drawable.overlay);
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
