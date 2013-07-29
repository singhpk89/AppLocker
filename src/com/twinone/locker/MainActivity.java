package com.twinone.locker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "Main";

	Button bChangePass;
	Button bToggleService;
	private boolean mShowingLocker = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bToggleService = (Button) findViewById(R.id.bToggleService);
		bChangePass = (Button) findViewById(R.id.bChangePassword);
		bToggleService.setOnClickListener(this);
		bChangePass.setOnClickListener(this);
		// We have to check if a password is set, and if not, ask the user to
		// create a password.

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bChangePassword:
			Log.d(TAG, "Changing password from button");
			startChangePasswordActivity();
			break;
		case R.id.bToggleService:
			if (isServiceRunning()) {
				stopObserverService();
			} else {
				startObserverService();
			}
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!mShowingLocker) {
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Reset the layout if the service has changed while not at activity.
		updateLayout(isServiceRunning());

		// If not showing the locker, we should show it
		if (!mShowingLocker) {
			mShowingLocker = true;
		}
	}

	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ObserverService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private final void startObserverService() {
		if (ObserverService.getPassword(this).isEmpty()) {
			new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage(
							"You must create a password first.\n"
									+ "Do you want to set a password now?")
					.setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startChangePasswordActivity();
						}
					}).setNegativeButton("No", null).show();
		} else {
			Intent i = new Intent(this, ObserverService.class);
			startService(i);
			updateLayout(true);
		}
	}

	private final void stopObserverService() {
		Intent i = new Intent(this, ObserverService.class);
		stopService(i);
		updateLayout(false);

	}

	private final void updateLayout(boolean isServiceRunning) {
		bToggleService.setText(isServiceRunning ? "Stop lock" : "Start lock");
	}

	private final void startChangePasswordActivity() {
		Intent i = new Intent(this, ChangePasswordLockActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
