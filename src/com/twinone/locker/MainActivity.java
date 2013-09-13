package com.twinone.locker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockActivity;
import com.twinone.util.ChangeLog;
import com.twinone.util.DialogSequencer;

public class MainActivity extends Activity implements View.OnClickListener {

	// TODO Add a recovery option
	// - Secret number Dial (Share)

	// TODO PRO add Tasker functionality - extend to other app
	// - Wifi / data enables / disables lock
	// - GPS enables / disables lock
	// - Time enables / disables lock

	// TODO Label Pro features with "PRO".
	// - Allow free users to use it until pro is available

	private static final String TAG = "Main";

	private boolean mUnlocked;
	private Button bChangePass;
	private Button bToggleService;
	private Button bSelectApps;
	private Button bChangeMessage;
	private Button bShare;
	private Button bRate;
	public static final String EXTRA_UNLOCKED = "com.twinone.locker.Unlocked";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Dark);
		setContentView(R.layout.activity_main);
		bToggleService = (Button) findViewById(R.id.bToggleService);
		bChangePass = (Button) findViewById(R.id.bChangePassword);
		bSelectApps = (Button) findViewById(R.id.bSelect);
		bChangeMessage = (Button) findViewById(R.id.bPrefs);
		bShare = (Button) findViewById(R.id.bShare);
		bRate = (Button) findViewById(R.id.bRate);

		bToggleService.setOnClickListener(this);
		bChangePass.setOnClickListener(this);
		bSelectApps.setOnClickListener(this);
		bChangeMessage.setOnClickListener(this);
		bShare.setOnClickListener(this);
		bRate.setOnClickListener(this);
		mUnlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);

		// VersionChecker gvt = new VersionChecker(this,
		// "http://twinone.mooo.com/version.txt?");
		// gvt.addFlags(VersionChecker.DENY_DEPRECATED
		// | VersionChecker.NOTIFY_UPDATE);
		// gvt.execute();

		DialogSequencer ds = new DialogSequencer();
		ds.addDialog(getChangelogDialog());
		ds.addDialogs(getEmptyPasswordDialogs());
		ds.startDisplaying();
	}

	private AlertDialog getChangelogDialog() {
		ChangeLog cl = new ChangeLog(this);
		return cl.shouldShow() ? cl.getDialog(true) : null;
	}

	private AlertDialog[] getEmptyPasswordDialogs() {
		if (UtilPref.isPasswordEmpty(this)) {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle("Hello moto");
			ab.setMessage("It seems you don't have an unlock method");
			ab.setPositiveButton(android.R.string.ok, null);

			AlertDialog.Builder ab2 = new AlertDialog.Builder(this);
			ab2.setItems(R.array.lock_type_names, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					int lockType = LockActivity.LOCK_TYPE_DEFAULT;
					switch (which) {
					case 0:

					}
					Intent i = LockActivity.getDefaultIntent(MainActivity.this);
					i.setAction(LockActivity.ACTION_CREATE);
					i.putExtra(LockActivity.EXTRA_VIEW_TYPE, lockType);
				}
			});

			return new AlertDialog[] { ab.create(), ab2.create() };
		}
		return null;
	}

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

			// Intent i = new Intent(MainActivity.this, StagingActivity.class);
			// i.putExtra(StagingActivity.EXTRA_ACTION,
			// StagingActivity.ACTION_RATE);
			// startActivity(i);
			//

			Intent i = new Intent(MainActivity.this, LockActivity.class);
			i.setAction(LockActivity.ACTION_CREATE);
			// i.putExtra(LockActivity.EXTRA_PASSWORD, "1234");
			// i.putExtra(LockActivity.EXTRA_PATTERN, "0125");
			i.putExtra(LockActivity.EXTRA_VIEW_TYPE,
					LockActivity.LOCK_TYPE_PATTERN);
			i.putExtra(LockActivity.EXTRA_PACKAGENAME, getPackageName());
			i.putExtra(LockActivity.EXTRA_VIEW_TYPES,
					LockActivity.LOCK_TYPE_PASSWORD
							| LockActivity.LOCK_TYPE_PATTERN);
			startActivity(i);
			break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mUnlocked = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Log.d(TAG, "onResume");
		updateLayout();
		if (!mUnlocked) {

		}

	}

	private void updateLayout() {

		bToggleService.setText(isServiceRunning() ? R.string.main_stop_service
				: R.string.main_start_service);

		if (UtilPref.getLockTypeInt(this) == LockActivity.LOCK_TYPE_PASSWORD) {
			bChangePass.setText(R.string.main_button_set_password);
		} else {
			bChangePass.setText(R.string.main_button_set_pattern);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showLockActivity() {
		Intent intent = new Intent(this, LockActivity.class);
		intent.setAction(LockActivity.ACTION_COMPARE);
		intent.putExtra(LockActivity.EXTRA_PACKAGENAME, getPackageName());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			Bundle b = ActivityOptions.makeCustomAnimation(this,
					android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
			startActivity(intent, b);
		} else {
			startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		mUnlocked = intent.getBooleanExtra(EXTRA_UNLOCKED, false);
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
			if (AppLockService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private final void doStartService() {
		Intent i = new Intent(this, AppLockService.class);
		startService(i);
		bToggleService.setText(R.string.main_stop_service);
	}

	private final void doStopService() {
		Intent i = new Intent(this, AppLockService.class);
		stopService(i);
		bToggleService.setText(R.string.main_start_service);
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
