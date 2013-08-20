package com.twinone.locker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ShareActivity extends Activity {

	private boolean mJustCreated = true;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.w("Share", "onCreate");
		String shareText = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
		share(shareText);

	}

	private void share(String shareText) {
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(i,
				getString(R.string.main_share_tit)));
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