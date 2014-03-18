package com.twinone.locker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.twinone.locker.automation.TempRuleActivity;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockViewService;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.version.Receiver;
import com.twinone.locker.version.VersionManager;
import com.twinone.util.Analytics;
import com.twinone.util.ChangeLog;
import com.twinone.util.DialogSequencer;

public class MainActivity extends Activity implements View.OnClickListener {
	private static final String RUN_ONCE = "com.twinone.locker.pref.run_once";

	private static final boolean TEST_BUTTON = true;

	private void onTestButton() {
		VersionManager.queryServer(this, null);

	}

	public static String getMobFoxId() {
		return "63db1a5b579e6c250d9c7d7ed6c3efd5";
	}

	public static String getAdMobId() {
		return "a152407835a94a7";
	}

	private static final String TAG = "Main";

	private TextView tvState;
	private Button bChangePass;
	private Button bStart;
	private Button bSelectApps;
	private Button bChangeMessage;
	private Button bShare;
	private Button bRate;
	private Button bBeta;

	private DialogSequencer mSequencer;
	private Analytics mAnalytics;

	public static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAnalytics = new Analytics(this);
		mAnalytics.increment(LockerAnalytics.MAIN_OPENED);
		runOnceCheck();

		setContentView(R.layout.activity_main);
		bStart = (Button) findViewById(R.id.bToggleService);
		bChangePass = (Button) findViewById(R.id.bChangePassword);
		bSelectApps = (Button) findViewById(R.id.bSelect);
		bChangeMessage = (Button) findViewById(R.id.bPrefs);
		bShare = (Button) findViewById(R.id.bShare);
		bRate = (Button) findViewById(R.id.bRate);
		bBeta = (Button) findViewById(R.id.bBeta);
		tvState = (TextView) findViewById(R.id.tvState);
		bStart.setOnClickListener(this);
		bChangePass.setOnClickListener(this);
		bSelectApps.setOnClickListener(this);
		bChangeMessage.setOnClickListener(this);
		bShare.setOnClickListener(this);
		bRate.setOnClickListener(this);
		bBeta.setOnClickListener(this);
		mSequencer = new DialogSequencer();
		bBeta.setVisibility(View.GONE);
		if (TEST_BUTTON) {
			ViewGroup root = (ViewGroup) findViewById(R.id.mainllRoot);
			Button b = new Button(this);
			b.setText("Test button");
			b.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					onTestButton();
				}
			});
			root.addView(b);
		}

		initVersionManager();
		shouldUpdate();
	}

	/**
	 * Added in version 2204
	 * 
	 * @return true if it's deprecated and should update forcedly
	 */
	private boolean shouldUpdate() {
		if (VersionManager.shouldWarn(this)) {
			int days = VersionManager.getDaysLeft(this);
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.update_available);
			ab.setMessage(getString(R.string.update_available_msg, days));
			ab.setPositiveButton(R.string.update_button,
					new ToPlayStoreListener());
			ab.setNegativeButton(R.string.update_button_cancel, null);
			ab.show();
		} else if (VersionManager.isDeprecated(this)) {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.update_needed);
			ab.setCancelable(false);
			ab.setMessage(R.string.update_needed_msg);
			ab.setPositiveButton(R.string.update_button,
					new ToPlayStoreListener());
			ab.show();
			return true;
		}
		return false;
	}

	/** When the user clicks this button he will be sent to the play store */
	private class ToPlayStoreListener implements OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String str = "https://play.google.com/store/apps/details?id="
					+ getPackageName();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
			startActivity(intent);
		}
	}

	private void runOnceCheck() {
		SharedPreferences prefs = PrefUtil.prefs(this);
		boolean runonce = prefs.getBoolean(RUN_ONCE, false);
		if (!runonce) {
			runOnceImpl();
			SharedPreferences.Editor editor = prefs.edit().putBoolean(RUN_ONCE,
					true);
			PrefUtil.apply(editor);
		}
	}

	private void runOnceImpl() {
		// Set default background
		SharedPreferences.Editor editor = PrefUtil.prefs(this).edit();
		PrefUtil.setLockerBackground(editor, this,
				getString(R.string.pref_val_bg_blue));
		PrefUtil.apply(editor);
	}

	private void initVersionManager() {
		String url = "https://twinone.org/apps/locker/update.php";
		VersionManager.setUrlOnce(this, url);
		if (VersionManager.isJustUpgraded(this)) {
			Receiver.scheduleAlarm(this);
		}
	}

	/**
	 * 
	 * @return True if the service should start
	 */
	private boolean showDialogs() {
		boolean res = true;
		// ChangeLog cl = new ChangeLog(this);
		// if (cl.shouldShow()) {
		// mSequencer.addDialog(cl.getDialog(true));
		// }

		// Recovery code
		String code = PrefUtil.getRecoveryCode(this);
		if (code == null) {
			final String newCode = PrefUtil.generateRecoveryCode();
			// save it directly to avoid it to change
			SharedPreferences.Editor editor = PrefUtil.prefs(MainActivity.this)
					.edit();
			editor.putString(getString(R.string.pref_key_recovery_code),
					newCode);
			PrefUtil.apply(editor);
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setCancelable(false);
			ab.setNeutralButton(R.string.recovery_code_send_button,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(
									android.content.Intent.ACTION_SEND);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_TEXT, "Locker: " + newCode);
							startActivity(Intent.createChooser(i,
									getString(R.string.main_share_tit)));
						}
					});
			ab.setPositiveButton(android.R.string.ok, null);
			ab.setTitle(R.string.recovery_tit);
			ab.setMessage(String.format(getString(R.string.recovery_dlgmsg),
					newCode));
			mSequencer.addDialog(ab.create());
		}

		// Empty password
		final boolean empty = PrefUtil.isCurrentPasswordEmpty(this);
		if (empty) {
			mSequencer.addDialog(getEmptyPasswordDialog(this, mSequencer));
			mSequencer.addDialog(PrefsActivity.getChangePasswordDialog(this));
			res = false;
		}
		// No apps
		if (PrefUtil.getTrackedApps(this).isEmpty()) {
			final AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.main_setup);
			ab.setMessage(R.string.main_no_locked_apps);
			ab.setCancelable(false);
			ab.setNegativeButton(android.R.string.cancel, null);
			ab.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(MainActivity.this,
							SelectActivity.class);
					startActivity(i);
				}
			});
			mSequencer.addDialog(ab.create());
			res = false;
		}
		mSequencer.start();
		return res;
	}

	private static AlertDialog getEmptyPasswordDialog(Context c,
			final DialogSequencer ds) {
		final AlertDialog.Builder msg = new AlertDialog.Builder(c);
		msg.setTitle(R.string.main_setup);
		msg.setMessage(R.string.main_no_password);
		msg.setCancelable(false);
		msg.setPositiveButton(android.R.string.ok, null);
		msg.setNegativeButton(android.R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ds.removeNext(dialog);
			}
		});
		return msg.create();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bChangePassword:
			if (PrefUtil.isCurrentPasswordEmpty(this)) {
				PrefsActivity.getChangePasswordDialog(this).show();
			} else {
				Intent i = LockViewService.getDefaultIntent(this);
				i.setAction(LockViewService.ACTION_CREATE);
				startService(i);
			}

			break;
		case R.id.bToggleService:
			if (isServiceRunning()) {
				doStopService();
			} else {
				doStartService();
			}
			break;
		case R.id.bSelect:
			startSelectActivity();
			break;
		case R.id.bPrefs:
			mAnalytics.increment(LockerAnalytics.MAIN_LAUNCH_PREFS);
			Intent prefsIntent = new Intent(this, PrefsActivity.class);
			startActivity(prefsIntent);
			break;
		case R.id.bShare:
			showShareDialog();
			break;
		case R.id.bRate:
			// startActivity(new Intent(this, NavigationDrawerActivity.class));
			mAnalytics.increment(LockerAnalytics.RATE);
			Intent i = new Intent(MainActivity.this, StagingActivity.class);
			i.setAction(StagingActivity.ACTION_RATE);
			startActivity(i);
			break;
		case R.id.bBeta:
			mAnalytics.increment(LockerAnalytics.MAIN_BETA);
			Intent i2 = new Intent(MainActivity.this, TempRuleActivity.class);
			startActivity(i2);
			break;
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "OnResume");
		super.onResume();
		boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
		if (PrefUtil.isCurrentPasswordEmpty(this)) {
			unlocked = true;
		}
		if (!unlocked) {
			Intent i = LockViewService.getDefaultIntent(this);
			i.setAction(LockViewService.ACTION_COMPARE);
			i.putExtra(LockViewService.EXTRA_PACKAGENAME, getPackageName());
			startService(i);
		}
		getIntent().putExtra(EXTRA_UNLOCKED, false);
		updateLayout(isServiceRunning());
		showDialogs();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSequencer.stop();
		AppLockService.notifyPackageChanged(this, null);
	}

	private void updateLayout(boolean running) {
		if (running) {
			tvState.setText(R.string.main_state_running);
			tvState.setVisibility(View.VISIBLE);
			// tvState.setTextColor(Color.parseColor("#10599D"));
			bStart.setText(R.string.main_stop_service);
		} else {
			tvState.setText(R.string.main_state_not_running);
			// tvState.setTextColor(Color.RED);
			tvState.setVisibility(View.GONE);
			bStart.setText(R.string.main_start_service);
		}
		if (PrefUtil.getLockTypeInt(this) == LockViewService.LOCK_TYPE_PASSWORD) {
			bChangePass.setText(R.string.main_button_set_password);
		} else {
			bChangePass.setText(R.string.main_button_set_pattern);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		// to get unlocked state in onResume
		setIntent(intent);
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
				mAnalytics.increment(LockerAnalytics.SHARE);
				Intent i = new Intent(MainActivity.this, StagingActivity.class);
				i.setAction(StagingActivity.ACTION_SHARE);
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
			if (AppLockService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void doStartService() {
		Log.d(TAG, "doStartService");
		if (showDialogs() && !shouldUpdate()) {
			mAnalytics.increment(LockerAnalytics.MAIN_START);
			Intent i = AppLockService.getStartIntent(this);
			startService(i);
			updateLayout(isServiceRunning());
		}
	}

	private void doStopService() {
		Log.d(TAG, "doStopService");
		// Intent i = AppLockService.getStopIntent(this);
		// startService(i);
		Intent i = new Intent(this, AppLockService.class);
		updateLayout(false);
		stopService(i);
	}

	private final void startSelectActivity() {
		mAnalytics.increment(LockerAnalytics.MAIN_SELECT_APPS);
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
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		moveTaskToBack(true);
	}

}
