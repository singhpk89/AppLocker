package com.twinone.locker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.twinone.locker.ObserverService.LocalBinder;

public class LockActivity extends LockActivityBase {

	private static final String TAG = "Locker";

	/** The password saved in the file */
	private String savedPassword;

	/** The application which we are showing this locker for */
	private LockInfo target;

	private ObserverService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locker);

		initLayout();

		savedPassword = ObserverService.getPassword(this);

		// Get the packagename
		target = (LockInfo) getIntent().getExtras().getSerializable(
				ObserverService.EXTRA_APPINFO);

		// load icon or hide the ImageView
		ApplicationInfo forApp = getAI(target.packageName);
		if (forApp != null) {
			ivAppIcon.setBackground(forApp.loadIcon(getPackageManager()));
			tvHeader.setText(forApp.loadLabel(getPackageManager()));
		} else {
			Log.w(TAG, "Could not load ApplicationInfo image");
			ivAppIcon.setVisibility(View.GONE);
		}

		tvFooter.setText(R.string.locker_footer_default);
		Log.w(TAG, "LockerActivity for " + target);

	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent i = new Intent(this, ObserverService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			LocalBinder b = (LocalBinder) binder;
			mService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mBound = false;
		}
	};

	/**
	 * Checks the password and performs the appropriate action.
	 * 
	 * @param toCheck
	 * @return
	 */
	private boolean checkPassword(String toCheck) {
		if (toCheck.equals(savedPassword)) {
			unlock();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Utility method to get an {@link ApplicationInfo} for a packageName.
	 * 
	 * @param packageName
	 * @return an {@link ApplicationInfo} or null if not found.
	 */
	public ApplicationInfo getAI(String packageName) {
		try {
			return this.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
		finish();
	}

	@Override
	protected void onNumberButton(View v) {
		super.onNumberButton(v);
		checkPassword(tvPassword.getText().toString());
	}

	@Override
	protected void onOkButton() {
		super.onOkButton();
		if (!checkPassword(tvPassword.getText().toString())) {
			// Incorrect password
			tvPassword.setText("");
			Toast.makeText(this, R.string.locker_invalid_password,
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Exit the {@link LockActivity}
	 */
	private void unlock() {
		if (mBound) {
			mService.doUnlock(target.packageName);
		} else {
			Log.w(TAG, "Service not bound, cannot unlock");
		}
		finish();

		// FROM HERE DOWN IT'S THE OLD VERSION
		// if (target.packageName.equals(getApplicationInfo().packageName)) {
		// // Intent i = new Intent(this, MainActivity.class);
		// // startActivity(i);
		// if (mBound) {
		// mService.doUnlock(target.packageName);
		// } else {
		// Log.w(TAG, "Service not bound, cannot unlock");
		// }
		//
		// Log.d(TAG, "Unlocked own app, finishing");
		// finish();
		// } else {
		// // To avoid root and parent activity errors
		// // Log.d(TAG, "Unlocked 3rd party, moving to back");
		// if (mBound) {
		// mService.doUnlock(target.packageName);
		// Log.d(TAG, "Unlocked 3rd party app");
		// } else {
		// Log.w(TAG, "Service not bound, cannot unlock");
		// }
		// finish();
		// // moveTaskToBack(true);
		// }
	}
}
