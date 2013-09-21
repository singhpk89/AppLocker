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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.twinone.locker.automation.TempRuleActivity;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockActivity;
import com.twinone.locker.util.PrefUtil;
import com.twinone.util.ChangeLog;
import com.twinone.util.DialogSequencer;
import com.twinone.util.VersionChecker;

public class MainActivity extends Activity implements View.OnClickListener {

	public static final String PUBLISHER_ID = "63db1a5b579e6c250d9c7d7ed6c3efd5";
	public static final boolean SHOW_ADS = true;

	// TODO PRO add Tasker functionality - extend to other app
	// - Wifi / data enables / disables lock
	// - GPS enables / disables lock
	// - Time enables / disables lock

	// TODO FIXME
	// - Add custom number dial to open app
	// -

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
	public static final String EXTRA_UNLOCKED = "com.twinone.locker.Unlocked";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Dark);
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
	}

	/**
	 * 
	 * @return True if the service should start
	 */
	private boolean showDialogs() {
		boolean res = true;
		final ChangeLog cl = new ChangeLog(this);
		if (cl.shouldShow()) {
			mSequencer.addDialog(cl.getDialog(true));
		}

		// Recovery code
		String code = PrefUtil.getRecoveryCode(this);
		if (code == null) {
			final String newCode = PrefUtil.generateRecoveryCode();
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setCancelable(false);
			ab.setNegativeButton(R.string.vc_later, null);
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
			ab.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor editor = PrefUtil.prefs(
							MainActivity.this).edit();
					editor.putString(
							getString(R.string.pref_key_recovery_code), newCode);
					PrefUtil.apply(editor);
				}
			});
			ab.setTitle(R.string.recovery_tit);
			ab.setMessage(String.format(getString(R.string.recovery_dlgmsg),
					newCode));
			mSequencer.addDialog(ab.create());
		}

		// Empty password
		final boolean empty = PrefUtil.isCurrentPasswordEmpty(this);
		if (empty) {
			// setup two dialogs, one to ask the user if he wants to use it
			final AlertDialog.Builder msg = new AlertDialog.Builder(this);
			msg.setTitle("Setup");
			msg.setMessage("You have not setup a password yet.\nDo you want to setup a password now?");
			msg.setCancelable(false);
			msg.setPositiveButton(android.R.string.ok, null);
			msg.setNegativeButton(android.R.string.cancel,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSequencer.removeNext(dialog);
						}
					});
			mSequencer.addDialog(msg.create());
			final AlertDialog.Builder choose = new AlertDialog.Builder(this);
			choose.setCancelable(false);
			choose.setTitle("Choose Lock Type");
			choose.setItems(R.array.lock_type_names, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final int lockType = which == 0 ? LockActivity.LOCK_TYPE_PASSWORD
							: LockActivity.LOCK_TYPE_PATTERN;
					final Intent i = LockActivity
							.getDefaultIntent(MainActivity.this);
					i.setAction(LockActivity.ACTION_CREATE);
					i.putExtra(LockActivity.EXTRA_VIEW_TYPE, lockType);
					startActivity(i);
				}
			});
			mSequencer.addDialog(choose.create());
			res = false;
		}
		// No apps
		if (PrefUtil.getTrackedApps(this).isEmpty()) {
			final AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("You have no apps locked");
			ab.setMessage("Please add some apps");
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

	// public static int randInt(int min, int max) {
	//
	// ;
	// Random rand = new Random();
	// int randomNum = rand.nextInt((max - min) + 1) + min;
	// return randomNum;
	// }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bChangePassword:
			changePassword();
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
			Intent prefsIntent = new Intent(this, PrefsActivity.class);
			startActivity(prefsIntent);
			break;
		case R.id.bShare:
			showShareDialog();
			break;
		case R.id.bRate:
			Intent i = new Intent(MainActivity.this, StagingActivity.class);
			i.setAction(StagingActivity.ACTION_RATE);
			startActivity(i);
			break;
		case R.id.bBeta:
			Intent i2 = new Intent(MainActivity.this, TempRuleActivity.class);
			startActivity(i2);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
		Log.d(TAG, "onResume unlocked:" + unlocked);
		if (PrefUtil.isCurrentPasswordEmpty(this)) {
			Log.d(TAG, "Current password empty");
			unlocked = true;
		}
		if (!unlocked) {
			Log.d(TAG, "Locking own activity again");
			final Intent i = LockActivity.getDefaultIntent(this);
			i.setAction(LockActivity.ACTION_COMPARE);
			i.putExtra(LockActivity.EXTRA_MESSAGE,
					getString(R.string.locker_footer_default));
			i.putExtra(LockActivity.EXTRA_PACKAGENAME, getPackageName());
			startActivity(i);
			finish();
		} else {
			new VersionChecker(this, "http://twinone.pkern.at/version.txt")
					.execute();
			showDialogs();
		}
		getIntent().putExtra(EXTRA_UNLOCKED, false);
		updateLayout(isServiceRunning());
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSequencer.stop();
	}

	private void updateLayout(boolean running) {
		if (running) {
			tvState.setText(R.string.main_state_running);
			tvState.setTextColor(Color.GREEN);
			bStart.setText(R.string.main_stop_service);
		} else {
			tvState.setText(R.string.main_state_not_running);
			tvState.setTextColor(Color.RED);
			bStart.setText(R.string.main_start_service);
		}
		if (PrefUtil.getLockTypeInt(this) == LockActivity.LOCK_TYPE_PASSWORD) {
			bChangePass.setText(R.string.main_button_set_password);
		} else {
			bChangePass.setText(R.string.main_button_set_pattern);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
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

	private final void doStartService() {
		if (showDialogs()) {
			Intent i = AppLockService.getStartIntent(this);
			startService(i);
			updateLayout(true);
		}
	}

	private final void doStopService() {
		Intent i = new Intent(this, AppLockService.class);
		stopService(i);
		updateLayout(false);
	}

	private final void changePassword() {
		Intent i = LockActivity.getDefaultIntent(this);
		i.setAction(LockActivity.ACTION_CREATE);
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
		// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
