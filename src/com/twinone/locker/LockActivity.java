package com.twinone.locker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
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
	private String target;

	private ObserverService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locker);

		initLayout();
		loadApplicationInfo(getIntent());
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@SuppressWarnings("deprecation")
	void loadApplicationInfo(Intent i) {
		savedPassword = ObserverService.getPassword(this);

		// Get the packagename
		target = (String) i.getExtras().getSerializable(
				ObserverService.EXTRA_TARGET_PACKAGENAME);

		Log.d(TAG, "TARGET:" + target);
		// load icon or hide the ImageView
		ApplicationInfo ai = getAI(target);
		if (ai != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				ivAppIcon.setBackgroundDrawable(ai
						.loadIcon(getPackageManager()));
			} else {
				ivAppIcon.setBackground(ai.loadIcon(getPackageManager()));
			}
			tvHeader.setText(ai.loadLabel(getPackageManager()));
		} else {
			Log.w(TAG, "Could not load ApplicationInfo image");
			ivAppIcon.setVisibility(View.GONE);
		}
		String getMessage = ObserverService.getMessage(this);
		tvFooter.setText(getMessage.replace("%s",
				ai.loadLabel(getPackageManager())));
		setPassword("");
		Log.w(TAG, "LockerActivity for " + target);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent i = new Intent(this, ObserverService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "OnStart");

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent " + intent.hashCode());
		loadApplicationInfo(intent);
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
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
		// leaveLockActivity();
	}

	@Override
	protected void onNumberButton(View v) {
		super.onNumberButton(v);
		checkPassword(getPassword());
	}

	@Override
	protected void onOkButton() {
		super.onOkButton();
		if (!checkPassword(getPassword())) {
			// Incorrect password
			setPassword("");
			Toast.makeText(this, R.string.locker_invalid_password,
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Exit the {@link LockActivity}
	 */
	private void unlock() {
		if (mBound) {
			mService.unlock(target);
		} else {
			Log.w(TAG, "Service not bound, cannot unlock");
		}
		leaveLockActivity();
	}

	private void leaveLockActivity() {
		// finish();
		moveTaskToBack(true);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

	}
}
