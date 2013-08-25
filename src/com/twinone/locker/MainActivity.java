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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "Main";

	Button bStartChangePass;
	Button bToggleService;
	Button bStartSelect;
	Button bChangeMessage;
	Button bShare;
	Button bRate;
	public static final String EXTRA_UNLOCKED = "com.twinone.locker.Unlocked";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this,
				ObserverService.PREF_FILE_DEFAULT, MODE_PRIVATE, R.xml.prefs,
				false);

		setTheme(R.style.Theme_Dark);

		setContentView(R.layout.activity_main);
		bToggleService = (Button) findViewById(R.id.bToggleService);
		bStartChangePass = (Button) findViewById(R.id.bChangePassword);
		bStartSelect = (Button) findViewById(R.id.bSelect);
		bChangeMessage = (Button) findViewById(R.id.bPrefs);
		bShare = (Button) findViewById(R.id.bShare);
		bRate = (Button) findViewById(R.id.bRate);

		bToggleService.setOnClickListener(this);
		bStartChangePass.setOnClickListener(this);
		bStartSelect.setOnClickListener(this);
		bChangeMessage.setOnClickListener(this);
		bShare.setOnClickListener(this);
		bRate.setOnClickListener(this);
		// Show welcome message
		firstTime();
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
		case R.id.bSelect:
			startSelectActivity();
			break;
		case R.id.bPrefs:
			Intent prefsIntent = new Intent(this, PrefsActivity.class);
			startActivity(prefsIntent);
			break;
		case R.id.bShare:
			showShareDialog();
			break;
		case R.id.bRate:
			Intent i = new Intent(MainActivity.this, StagingActivity.class);
			i.putExtra(StagingActivity.EXTRA_ACTION,
					StagingActivity.ACTION_RATE);
			startActivity(i);
			break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		getIntent().putExtra(EXTRA_UNLOCKED, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.w(TAG, "onResume");

		// If there is no password, don't show locker
		// If it's already unlocked, don't show locker
		boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
		boolean emptyPassword = (ObserverService.getPassword(this).length() == 0);
		// Log.d(TAG, "unlocked: " + unlocked + " emptyPassword: " +
		// emptyPassword);
		if (!unlocked && !emptyPassword) {
			Intent i = new Intent(this, LockOwnActivity.class);
			startActivity(i);
			finish();
			return;
		}
		// Reset the layout if the service has changed while not at activity.
		updateLayout(isServiceRunning());

	}

	private void showShareDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final View dialogRoot = getLayoutInflater().inflate(
				R.layout.share_dialog, null);
		final EditText etShareText = (EditText) dialogRoot
				.findViewById(R.id.etShareText);
		etShareText.setText(R.string.main_share_text);
		builder.setTitle(R.string.main_share_tit);
		builder.setView(dialogRoot);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(MainActivity.this, StagingActivity.class);
				i.putExtra(StagingActivity.EXTRA_ACTION,
						StagingActivity.ACTION_SHARE);
				i.putExtra(StagingActivity.EXTRA_TEXT, etShareText.getText()
						.toString());
				startActivity(i);
				dialog.cancel();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
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
		if (!firstTime()) {
			Intent i = new Intent(this, ObserverService.class);
			startService(i);
			updateLayout(true);
		}
	}

	/**
	 * 
	 * @return True if it's the first time, false if not.
	 */
	private final boolean firstTime() {
		boolean isEmpty = (ObserverService.getPassword(this).length() == 0);
		if (isEmpty) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.welcome_tit)
					.setMessage(R.string.welcome_message)
					.setPositiveButton(android.R.string.yes,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startChangePasswordActivity();
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();
		}
		return isEmpty;
	}

	private final void stopObserverService() {
		Intent i = new Intent(this, ObserverService.class);
		stopService(i);
		updateLayout(false);
	}

	private final void updateLayout(boolean isServiceRunning) {
		bToggleService.setText(isServiceRunning ? R.string.main_stop_service
				: R.string.main_start_service);
	}

	private final void startChangePasswordActivity() {
		Intent i = new Intent(this, ChangePasswordActivity.class);
		startActivity(i);
	}

	private final void startSelectActivity() {
		Intent i = new Intent(this, SelectActivity.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Provide a way back to {@link MainActivity} without having to provide a
	 * password again. It finishes the calling {@link Activity}
	 * 
	 * @param context
	 */
	public static final void showWithoutPassword(Context context) {
		Intent i = new Intent(context, MainActivity.class);
		i.putExtra(EXTRA_UNLOCKED, true);
		if (context instanceof Activity) {
			if (!((Activity) context).isFinishing()) {
				((Activity) context).finish();
			}
		}
		context.startActivity(i);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		moveTaskToBack(true);
	}

}
