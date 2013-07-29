package com.twinone.locker;

import android.app.Activity;
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
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twinone.locker.ObserverService.LocalBinder;

public class OLDLockerActivity extends Activity implements OnClickListener {

	private static final String TAG = "Locker";

	LinearLayout llMain;

	private Button bOK;
	private Button bBack;
	private Button b0;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Button b6;
	private Button b7;
	private Button b8;
	private Button b9;

	private String mPassword;

	private TextView tvPassword;

	private LockInfo forApp;

	private ObserverService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locker);

		llMain = (LinearLayout) findViewById(R.id.llLockerRoot);
		tvPassword = (TextView) findViewById(R.id.tvPassword);
		// tvPassword.setKeyListener(null);

		mPassword = ObserverService.getPassword(this);

		bOK = (Button) findViewById(R.id.bOK);
		bBack = (Button) findViewById(R.id.bBack);
		b0 = (Button) findViewById(R.id.b0);
		b1 = (Button) findViewById(R.id.b1);
		b2 = (Button) findViewById(R.id.b2);
		b3 = (Button) findViewById(R.id.b3);
		b4 = (Button) findViewById(R.id.b4);
		b5 = (Button) findViewById(R.id.b5);
		b6 = (Button) findViewById(R.id.b6);
		b7 = (Button) findViewById(R.id.b7);
		b8 = (Button) findViewById(R.id.b8);
		b9 = (Button) findViewById(R.id.b9);

		bOK.setOnClickListener(this);
		bBack.setOnClickListener(this);
		b0.setOnClickListener(this);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		b3.setOnClickListener(this);
		b4.setOnClickListener(this);
		b5.setOnClickListener(this);
		b6.setOnClickListener(this);
		b7.setOnClickListener(this);
		b8.setOnClickListener(this);
		b9.setOnClickListener(this);
		bBack.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				tvPassword.setText("");
				return true;
			}
		});

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
		String currentPassword = tvPassword.getText().toString();
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
}
