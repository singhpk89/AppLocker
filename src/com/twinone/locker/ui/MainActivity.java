package com.twinone.locker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.twinone.locker.Constants;
import com.twinone.locker.R;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockService;
import com.twinone.locker.ui.NavigationFragment.NavigationListener;
import com.twinone.locker.util.PrefUtils;
import com.twinone.util.Analytics;
import com.twinone.util.DialogSequencer;

public class MainActivity extends ActionBarActivity implements
		NavigationListener {
	// private static final String RUN_ONCE =
	// "com.twinone.locker.pref.run_once";

	// public static final boolean DEBUG = false;
	private static final String VERSION_URL_PRD = "https://twinone.org/apps/locker/update.php";
	private static final String VERSION_URL_DBG = "https://twinone.org/apps/locker/dbg-update.php";

	private static final String ANALYTICS_PRD = "https://twinone.org/apps/locker/dbg-analytics.php";
	private static final String ANALYTICS_DBG = "https://twinone.org/apps/locker/analytics.php";
	private static final String VERSION_URL = Constants.DEBUG ? VERSION_URL_DBG
			: VERSION_URL_PRD;
	private static final String ANALYTICS_URL = Constants.DEBUG ? ANALYTICS_DBG
			: ANALYTICS_PRD;
	// private VersionManager mVersionManager;

	// private static final String TAG = "Main";

	private DialogSequencer mSequencer;
	private Analytics mAnalytics;

	public static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

	private void doTest() {
	}

	// /**
	// * Added in version 2204
	// *
	// * @return true if it's deprecated and should update forcedly
	// */
	// private void showVersionDialogs() {
	// if (mVersionManager.isDeprecated()) {
	// new VersionUtils(this).getDeprecatedDialog().show();
	// } else if (mVersionManager.shouldWarn()) {
	// new VersionUtils(this).getUpdateAvailableDialog().show();
	// }
	// }

	/**
	 * 
	 * @return True if the service is allowed to start
	 */
	private boolean showDialogs() {
		boolean deny = false;

		// Recovery code
		mSequencer.addDialog(Dialogs.getRecoveryCodeDialog(this));

		// Empty password
		deny = Dialogs.addEmptyPasswordDialog(this, mSequencer);

		mSequencer.start();
		return !deny;
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mTitle = title;
		getSupportActionBar().setTitle(title);
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
		if (PrefUtils.isCurrentPasswordEmpty(this)) {
			unlocked = true;
		}
		if (!unlocked) {
			LockService.showCompare(this, getPackageName());
		}
		getIntent().putExtra(EXTRA_UNLOCKED, false);
		// showDialogs();
		mNavigationDrawerFragment.getAdapter().setServiceState(
				AppLockService.isRunning(this));

	}

	@Override
	protected void onPause() {
		super.onPause();
		// mSequencer.stop();
		LockService.hide(this);
	}

	// private void showShareDialog() {
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// final View dialogRoot = getLayoutInflater().inflate(
	// R.layout.share_dialog, null);
	// final EditText etShareText = (EditText) dialogRoot
	// .findViewById(R.id.etShareText);
	// etShareText.setText(R.string.main_share_text);
	// builder.setTitle(R.string.main_share_tit);
	// builder.setView(dialogRoot);
	// builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// mAnalytics.increment(LockerAnalytics.SHARE);
	// // Intent i = new Intent(MainActivity.this,
	// // StagingActivity.class);
	// // i.setAction(StagingActivity.ACTION_SHARE);
	// // i.putExtra(StagingActivity.EXTRA_TEXT, etShareText.getText()
	// // .toString());
	// // startActivity(i);
	// // dialog.cancel();
	// }
	// });
	// builder.setNegativeButton(android.R.string.cancel, null);
	// builder.show();
	// }

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

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private ActionBar mActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		mTitle = getTitle();

		mActionBar = getSupportActionBar();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new AppsFragment()).commit();

		AppLockService.start(this);

		mNavigationDrawerFragment.getAdapter().setServiceState(true);

		mSequencer = new DialogSequencer();
		showDialogs();
	}

	public void setActionBarTitle(int resId) {
		mActionBar.setTitle(resId);
	}

	private boolean mNavPending;
	int mCurrentFragmentType;
	int mNavPendingType;

	@Override
	public boolean onNavigationElementSelected(int type) {
		if (type == NavigationElement.TYPE_TEST) {
			doTest();
			return false;
		} else if (type == NavigationElement.TYPE_STATUS) {
			toggleService();
			return false;
		}
		mNavPending = true;
		mNavPendingType = type;
		return true;
	}

	private void toggleService() {
		boolean newState = false;
		if (AppLockService.isRunning(this)) {
			AppLockService.stop(this);
		} else if (Dialogs.addEmptyPasswordDialog(this, mSequencer)) {
			mSequencer.start();
		} else {
			newState = AppLockService.toggle(this);
		}
		mNavigationDrawerFragment.getAdapter().setServiceState(newState);
	}

	@Override
	public void onDrawerOpened(View drawerView) {
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	public void onDrawerClosed(View drawerView) {
		getSupportActionBar().setTitle(mTitle);
		if (mNavPending) {
			navigateToFragment(mNavPendingType);
			mNavPending = false;
		}
	}

	/**
	 * Open a specific Fragment
	 * 
	 * @param type
	 */
	public void navigateToFragment(int type) {
		if (type == mCurrentFragmentType) {
			// Don't duplicate
			return;
		}
		if (type == NavigationElement.TYPE_CHANGE) {
			Dialogs.getChangePasswordDialog(this).show();
			// Don't change current fragment type
			return;
		}

		Fragment fragment = null;
		switch (type) {
		case NavigationElement.TYPE_APPS:
			fragment = new AppsFragment();
			break;
		case NavigationElement.TYPE_SETTINGS:
			fragment = new SettingsFragment();
			break;
		case NavigationElement.TYPE_STATISTICS:
			fragment = new StatisticsFragment();
			break;
		case NavigationElement.TYPE_PRO:
			fragment = new ProFragment();
			break;
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();
		mCurrentFragmentType = type;
	}

}
