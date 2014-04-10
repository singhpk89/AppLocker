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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.twinone.locker.appselect.SelectActivity;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockService;
import com.twinone.locker.pro.ProActivity;
import com.twinone.locker.pro.ProUtils;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.version.Receiver;
import com.twinone.locker.version.VersionManager;
import com.twinone.locker.version.VersionUtils;
import com.twinone.util.Analytics;
import com.twinone.util.DialogSequencer;

public class MainActivity extends Activity implements View.OnClickListener {
	private static final String RUN_ONCE = "com.twinone.locker.pref.run_once";

	public static final boolean DEBUG = false;
	private static final String VERSION_URL_PRD = "https://twinone.org/apps/locker/update.php";
	private static final String VERSION_URL_DBG = "https://twinone.org/apps/locker/dbg-update.php";

	private static final String ANALYTICS_PRD = "https://twinone.org/apps/locker/dbg-analytics.php";
	private static final String ANALYTICS_DBG = "https://twinone.org/apps/locker/analytics.php";
	private static final String VERSION_URL = DEBUG ? VERSION_URL_DBG
			: VERSION_URL_PRD;
	private static final String ANALYTICS_URL = DEBUG ? ANALYTICS_DBG
			: ANALYTICS_PRD;
	private VersionManager mVersionManager;

	private static final String TAG = "Main";

	private TextView tvState;
	private Button bChangePass;
	private Button bStart;
	private Button bSelectApps;
	private Button bChangeMessage;
	private Button bShare;
	private Button bRate;
	private Button bPro;

	private DialogSequencer mSequencer;
	private Analytics mAnalytics;

	public static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		mAnalytics = new Analytics(this);
		mVersionManager = new VersionManager(this);

		setContentView(R.layout.activity_main);
		bStart = (Button) findViewById(R.id.bToggleService);
		bChangePass = (Button) findViewById(R.id.bChangePassword);
		bSelectApps = (Button) findViewById(R.id.bSelect);
		bChangeMessage = (Button) findViewById(R.id.bPrefs);
		bShare = (Button) findViewById(R.id.bShare);
		bRate = (Button) findViewById(R.id.bRate);
		bPro = (Button) findViewById(R.id.bPro);
		tvState = (TextView) findViewById(R.id.tvState);
		bStart.setOnClickListener(this);
		bChangePass.setOnClickListener(this);
		bSelectApps.setOnClickListener(this);
		bChangeMessage.setOnClickListener(this);
		bShare.setOnClickListener(this);
		bRate.setOnClickListener(this);
		bPro.setOnClickListener(this);
		mSequencer = new DialogSequencer();
		if (DEBUG) {
			ViewGroup root = (ViewGroup) findViewById(R.id.mainllRoot);
			Button b = new Button(this);
			b.setText("Test button");
			b.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				}
			});
			root.addView(b);
		}

		showVersionDialogs();

		mVersionManager.setUrlOnce(VERSION_URL);
		mAnalytics.setUrlOnce(ANALYTICS_URL);

		if (mVersionManager.isJustUpgraded()) {
			onUpgrade();
		}

	}

	/**
	 * Added in version 2204
	 * 
	 * @return true if it's deprecated and should update forcedly
	 */
	private void showVersionDialogs() {
		if (mVersionManager.isDeprecated()) {
			new VersionUtils(this).getDeprecatedDialog().show();
		} else if (mVersionManager.shouldWarn()) {
			new VersionUtils(this).getUpdateAvailableDialog().show();
		}
	}

	/** Called when a version upgrade performed (or on fresh install) */
	private void onUpgrade() {
		Receiver.scheduleAlarm(this);
		new ProUtils(this).updateProSettings();
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
		if (PrefUtil.getLockedApps(this).isEmpty()) {
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
				Intent i = LockService.getDefaultIntent(this);
				i.setAction(LockService.ACTION_CREATE);
				startService(i);
			}

			break;
		case R.id.bToggleService:
			doToggleService();
			break;
		case R.id.bSelect:
			mAnalytics.increment(LockerAnalytics.OPEN_SELECT);
			startSelectActivity();
			break;
		case R.id.bPrefs:
			mAnalytics.increment(LockerAnalytics.OPEN_PREFS);
			Intent prefsIntent = new Intent(this, PrefsActivity.class);
			startActivity(prefsIntent);
			break;
		case R.id.bShare:
			mAnalytics.increment(LockerAnalytics.SHARE);
			showShareDialog();
			break;
		case R.id.bRate:
			// startActivity(new Intent(this, NavigationDrawerActivity.class));
			mAnalytics.increment(LockerAnalytics.RATE);
			Intent i = new Intent(MainActivity.this, StagingActivity.class);
			i.setAction(StagingActivity.ACTION_RATE);
			startActivity(i);
			break;
		case R.id.bPro:
			Intent pro = new Intent(MainActivity.this, ProActivity.class);
			startActivity(pro);
			break;
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "OnResume");
		super.onResume();
		boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
		Log.d(TAG, "unlocked: " + unlocked);
		if (PrefUtil.isCurrentPasswordEmpty(this)) {
			unlocked = true;
		}
		Log.d(TAG, "unlocked: " + unlocked);
		if (!unlocked) {
			Intent i = LockService.getDefaultIntent(this);
			i.setAction(LockService.ACTION_COMPARE);
			i.putExtra(LockService.EXTRA_PACKAGENAME, getPackageName());
			startService(i);
		}
		Log.d(TAG, "unlocked: " + unlocked);
		getIntent().putExtra(EXTRA_UNLOCKED, false);
		updateLayout(isServiceRunning());
		showDialogs();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSequencer.stop();
		LockService.hide(this);
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
			tvState.setVisibility(View.INVISIBLE);
			bStart.setText(R.string.main_start_service);
		}
		if (PrefUtil.getLockTypeInt(this) == LockService.LOCK_TYPE_PASSWORD) {
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

	private void doToggleService() {
		if (isServiceRunning()) {
			doStopService();
		} else {
			doStartService();
		}
	}

	private void doStartService() {
		Log.d(TAG, "doStartService");
		if (showDialogs()) {
			AppLockService.start(this);
			updateLayout(isServiceRunning());
		}
	}

	private void doStopService() {
		Log.d(TAG, "doStopService");
		updateLayout(false);
		AppLockService.stop(this);
	}

	private final void startSelectActivity() {
		Intent i = new Intent(this, SelectActivity.class);
		startActivity(i);
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
		if (!(context instanceof Activity)) {
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(i);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		moveTaskToBack(true);
	}

}
