package com.twinone.locker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class StagingActivity extends Activity {

	private boolean mJustCreated = true;

	public static final String ACTION_SHARE = "com.twinone.locker.action.share";
	public static final String ACTION_RATE = "com.twinone.locker.action.rate";
	public static final String EXTRA_TEXT = "com.twinone.locker.extra.text";
	public static final String EXTRA_ACTION = "com.twinone.locker.extra.action";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.w("Stager", "onCreate");
		String action = getIntent().getExtras().getString(EXTRA_ACTION);
		if (ACTION_SHARE.equals(action)) {
			String shareText = getIntent().getExtras().getString(
					Intent.EXTRA_TEXT);
			Intent i = new Intent(android.content.Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(EXTRA_TEXT, shareText);
			startActivity(Intent.createChooser(i,
					getString(R.string.main_share_tit)));
		} else if (ACTION_RATE.equals(action)) {
			String str = "https://play.google.com/store/apps/details?id=" + getPackageName();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mJustCreated) {
			mJustCreated = false;
		} else {
			MainActivity.showWithoutPassword(this);
			finish();
		}
	}
}