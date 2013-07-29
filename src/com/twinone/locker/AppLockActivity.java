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
import android.widget.Button;
import android.widget.Toast;

import com.twinone.locker.ObserverService.LocalBinder;

public class AppLockActivity extends LockActivityBase {

	private static final String TAG = "Locker";

	private String mPassword;

	private LockInfo forApp;

	private ObserverService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locker);

		initLayout();

		mPassword = ObserverService.getPassword(this);

		// Get the packagename
		forApp = (LockInfo) getIntent().getExtras().getSerializable(
				ObserverService.EXTRA_APPINFO);
		Log.w(TAG, "LockerActivity for " + forApp + " " + forApp.hashCode());

	}

	@Override
	protected void onResume() {
		super.onResume();
		// overridePendingTransition(0, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isFinishing()) {
			finish();
		}
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

	@Override
	public void onClick(View v) {
		String currentPassword = tvPassword
				.
				getText()
				.
				toString();
		switch (v.getId()) {
		case R.id.bOK:
			if (!checkPassword(currentPassword)) {
				// Incorrect password
				tvPassword.setText("");
				Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.bBack:
			// Remove last character
			if (!currentPassword.isEmpty()) {
				currentPassword = currentPassword.substring(0,
						currentPassword.length() - 1);
				tvPassword.setText(currentPassword);

			}
			break;
		default:
			currentPassword += ((Button) v).getText();
			tvPassword.setText(currentPassword);
			checkPassword(currentPassword);
			break;
		}
	}

	private boolean checkPassword(String toCheck) {
		if (toCheck.equals(mPassword)) {
			onCorrectPassword();
			return true;
		} else {
			return false;
		}
	}

	private void onCorrectPassword() {

		if (forApp.packageName.equals(getApplicationInfo().packageName)) {
			// Intent i = new Intent(this, MainActivity.class);
			// startActivity(i);
			if (mBound) {
				mService.doUnlock(forApp.packageName);
			} else {
				Log.w(TAG, "Service not bound, cannot unlock");
			}

			Log.d(TAG, "Unlocked own app, finishing");
			finish();
		} else {
			// To avoid root and parent activity errors
			// Log.d(TAG, "Unlocked 3rd party, moving to back");
			if (mBound) {
				mService.doUnlock(forApp.packageName);
				Log.d(TAG, "Unlocked 3rd party app");
			} else {
				Log.w(TAG, "Service not bound, cannot unlock");
			}
			finish();
			moveTaskToBack(true);
			// myFinish();
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void onBackButton(boolean longPress) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onOkButton() {
		// TODO Auto-generated method stub

	}
}
