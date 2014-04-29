package com.twinone.locker.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.twinone.locker.LockerAnalytics;
import com.twinone.locker.R;
import com.twinone.locker.ui.MainActivity;
import com.twinone.locker.util.PrefUtils;
import com.twinone.locker.version.VersionManager;
import com.twinone.locker.version.VersionUtils;
import com.twinone.util.Analytics;

public class AppLockService extends Service {

	private static final int REQUEST_CODE = 0x1234AF;
	public static final int NOTIFICATION_ID = 0xABCD32;
	private static final String TAG = "AppLockService";

	/** Use this action to stop the intent */
	private static final String ACTION_STOP = "com.twinone.locker.intent.action.stop_lock_service";
	/** Starts the alarm */
	private static final String ACTION_START = "com.twinone.locker.intent.action.start_lock_service";
	/**
	 * When specifying this action, the service will initialize everything
	 * again.<br>
	 * This has only effect if the service was explicitly started using
	 * {@link #getRunIntent(Context)}
	 */
	public static final String ACTION_RESTART = "com.twinone.locker.intent.action.restart_lock_service";

	private ActivityManager mActivityManager;

	private String mLastPackageName;

	/** 0 for disabled */
	private long mShortExitMillis;

	private boolean mRelockScreenOff;
	private boolean mShowNotification;

	private boolean mExplicitStarted;
	private boolean mAllowDestroy;
	private boolean mAllowRestart;
	private Handler mHandler;
	private BroadcastReceiver mScreenReceiver;

	/**
	 * This map contains locked apps in the form<br>
	 * <PackageName, ShortExitEndTime>
	 */
	private Map<String, Boolean> mLockedPackages;
	private Map<String, Runnable> mUnlockMap;

	@Override
	public IBinder onBind(Intent i) {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public AppLockService getInstance() {
			return AppLockService.this;
		}
	}

	private final class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.i(TAG, "Screen ON");
				// Trigger package again
				mLastPackageName = null;
				startAlarm(AppLockService.this);
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(TAG, "Screen OFF");
				stopAlarm(AppLockService.this);
				if (mRelockScreenOff) {
					lockAll();
				}
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
	}

	/** Starts everything, including notification and repeating alarm */
	private boolean init() {
		Log.d(TAG, "init");
		if (new VersionManager(this).isDeprecated()) {
			Log.i(TAG, "Not starting AlarmService for deprecated version");
			new VersionUtils(this).showDeprecatedNotification();
			stop(this);
			return false;
		}

		mHandler = new Handler();
		mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mUnlockMap = new HashMap<String, Runnable>();
		mLockedPackages = new HashMap<String, Boolean>();
		mScreenReceiver = new ScreenReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenReceiver, filter);

		final Set<String> apps = PrefUtils.getLockedApps(this);
		for (String s : apps) {
			mLockedPackages.put(s, true);
		}
		SharedPreferences sp = PrefUtils.prefs(this);

		boolean defaultDelay = Boolean
				.parseBoolean(getString(R.string.pref_def_delay_status));
		boolean delayEnabled = sp.getBoolean(
				getString(R.string.pref_key_delay_status), defaultDelay);

		if (delayEnabled) {
			String delaySeconds = sp.getString(
					getString(R.string.pref_key_delay_time),
					getString(R.string.pref_def_delay_time));
			if (delaySeconds.length() == 0)
				delaySeconds = "0";
			mShortExitMillis = Long.parseLong(delaySeconds) * 1000;
		}

		boolean defaultScreenOffRelock = Boolean
				.parseBoolean(getString(R.string.pref_def_relock_after_screenoff));
		boolean relock = sp.getBoolean(
				getString(R.string.pref_key_relock_after_screenoff),
				defaultScreenOffRelock);
		mRelockScreenOff = relock;

		startNotification();
		startAlarm(this);

		return true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || ACTION_START.equals(intent.getAction())) {
			if (intent == null) {
				Log.d(TAG, "onStartCommand intent==null");
			}
			if (!mExplicitStarted) {
				Log.d(TAG, "explicitStarted = false");
				if (init() == false)
					;
				mExplicitStarted = true;
				// return START_NOT_STICKY;
			}
			checkPackageChanged();
		} else if (ACTION_RESTART.equals(intent.getAction())) {
			if (mExplicitStarted) {
				Log.d(TAG, "ACTION_RESTART");
				// init();
				doRestartSelf(); // not allowed, so service will restart
			} else {
				doStopSelf();
			}
		} else if (ACTION_STOP.equals(intent.getAction())) {
			Log.d(TAG, "ACTION_STOP");
			doStopSelf();
		}
		// With start_not_sticky we have the auto-close bug
		// It looks like With start_sticky too (?)
		// Start sticky causes multiple receivers
		return START_STICKY;
	}

	private void checkPackageChanged() {
		final String packageName = getTopPackageName();
		if (!packageName.equals(mLastPackageName)) {
			onAppClose(mLastPackageName, packageName);
			onAppOpen(packageName, mLastPackageName);
			Log.d(TAG, mLastPackageName + " > " + packageName);
		}

		// prepare for next call
		mLastPackageName = packageName;
	}

	private void onAppOpen(String open, String close) {
		// Log.v(TAG, "open " + packageName);
		if (mLockedPackages.containsKey(open)) {
			onLockedAppOpen(open, close);
		}
	}

	private void onLockedAppOpen(String open, String close) {
		boolean locked = mLockedPackages.get(open);
		if (locked) {
			showLocker(open);
		}
		removeRelockTimer(open);
	}

	private void showLocker(String packageName) {
		Intent intent = LockService.getDefaultIntent(this);
		intent.setAction(LockService.ACTION_COMPARE);
		intent.putExtra(LockService.EXTRA_PACKAGENAME, packageName);
		startService(intent);

	}

	private void onAppClose(String close, String open) {
		if (mLockedPackages.containsKey(close)) {
			onLockedAppClose(close, open);
		}
	}

	private void onLockedAppClose(String close, String open) {
		setRelockTimer(close);
		if (!getPackageName().equals(close) && !getPackageName().equals(open)) {
			LockService.hide(this);
		}
	}

	private void setRelockTimer(String packageName) {
		boolean locked = mLockedPackages.get(packageName);
		if (!locked) {
			if (mShortExitMillis != 0) {
				Runnable r = new RelockRunnable(packageName);
				mHandler.postDelayed(r, mShortExitMillis);
				mUnlockMap.put(packageName, r);
			} else {
				lockApp(packageName);
			}
		}
	}

	private void removeRelockTimer(String packageName) {
		// boolean locked = mLockedPackages.get(packageName);
		// if (!locked) {
		if (mUnlockMap.containsKey(packageName)) {
			mHandler.removeCallbacks(mUnlockMap.get(packageName));
			mUnlockMap.remove(packageName);
		}
	}

	/** This class will re-lock an app */
	private class RelockRunnable implements Runnable {
		private final String mPackageName;

		public RelockRunnable(String packageName) {
			mPackageName = packageName;
		}

		@Override
		public void run() {
			lockApp(mPackageName);
		}
	}

	private String getTopPackageName() {
		return mActivityManager.getRunningTasks(1).get(0).topActivity
				.getPackageName();
	}

	/**
	 * Unlock a single application. Should be called by {@link LockActivity}
	 * 
	 * @param appName
	 */
	public void unlockApp(String packageName) {
		if (mLockedPackages.containsKey(packageName)) {
			mLockedPackages.put(packageName, false);
		}
	}

	private void lockAll() {
		for (Map.Entry<String, Boolean> entry : mLockedPackages.entrySet()) {
			entry.setValue(true);
		}
	}

	public void lockApp(String packageName) {
		if (mLockedPackages.containsKey(packageName)) {
			mLockedPackages.put(packageName, true);
		}
	}

	private void startNotification() {
		SharedPreferences sp = PrefUtils.prefs(this);
		boolean defaultShowNotification = Boolean
				.parseBoolean(getString(R.string.pref_def_show_notification));
		boolean showNotification = sp.getBoolean(
				getString(R.string.pref_key_show_notification),
				defaultShowNotification);
		mShowNotification = showNotification;
		showNotification();
		if (!mShowNotification) {
			NotificationRemover.start(this);
		}
	}

	@SuppressLint("InlinedApi")
	private void showNotification() {
		Log.d(TAG, "showNotification");
		SharedPreferences sp = PrefUtils.prefs(this);
		boolean defHideNotifIcon = Boolean
				.parseBoolean(getString(R.string.pref_def_hide_notification_icon));
		boolean hideNotifIcon = sp.getBoolean(
				getString(R.string.pref_key_hide_notification_icon),
				defHideNotifIcon);
		int priority = hideNotifIcon ? Notification.PRIORITY_MIN
				: Notification.PRIORITY_DEFAULT;
		Intent i = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		String title = getString(R.string.notification_title);
		String content = getString(R.string.notification_state_locked);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setContentTitle(title);
		nb.setContentText(content);
		nb.setWhen(System.currentTimeMillis());
		nb.setContentIntent(pi);
		nb.setOngoing(true);
		nb.setPriority(priority);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);

		startForeground(NOTIFICATION_ID, nb.build());
	}

	public static final void start(Context c) {
		new Analytics(c).increment(LockerAnalytics.SERVICE_START);
		startAlarm(c);
	}

	/**
	 * 
	 * @param c
	 * @return The new state for the service, true for running, false for not
	 *         running
	 */
	public static boolean toggle(Context c) {
		if (isRunning(c)) {
			stop(c);
			return false;
		} else {
			start(c);
			return true;
		}

	}

	public static boolean isRunning(Context c) {
		ActivityManager manager = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (AppLockService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/** Starts the service */
	private static final void startAlarm(Context c) {
		AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		PendingIntent pi = getRunIntent(c);
		SharedPreferences sp = PrefUtils.prefs(c);
		String defaultPerformance = c.getString(R.string.pref_val_perf_normal);
		String s = sp.getString(c.getString(R.string.pref_key_performance),
				defaultPerformance);
		if (s.length() == 0)
			s = "0";
		long interval = Long.parseLong(s);
		long startTime = SystemClock.elapsedRealtime();
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, startTime, interval, pi);
	}

	private static PendingIntent running_intent;

	private static final PendingIntent getRunIntent(Context c) {
		if (running_intent == null) {
			Intent i = new Intent(c, AppLockService.class);
			i.setAction(ACTION_START);
			running_intent = PendingIntent.getService(c, REQUEST_CODE, i, 0);
		}
		return running_intent;
	}

	private static final void stopAlarm(Context c) {
		AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		am.cancel(getRunIntent(c));
	}

	/** Stop this service, also stopping the alarm */
	public static final void stop(Context c) {
		stopAlarm(c);
		new Analytics(c).increment(LockerAnalytics.SERVICE_STOP);
		Intent i = new Intent(c, AppLockService.class);
		i.setAction(ACTION_STOP);
		c.startService(i);
	}

	/**
	 * Re-initialize everything.<br>
	 * This has only effect if the service was explicitly started using
	 * {@link #start(Context)}
	 */

	public static final void restart(Context c) {
		Intent i = new Intent(c, AppLockService.class);
		i.setAction(ACTION_RESTART);
		c.startService(i);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAllowRestart) {
			start(this);
			mAllowRestart = false;
			return;
		}

		Log.i(TAG, "onDestroy (allowed=" + mAllowDestroy + ")");
		if (!mAllowDestroy) {
			Log.d(TAG, "Destroy not allowed, restarting service");
			start(this);
		}
		if (mScreenReceiver != null)
			unregisterReceiver(mScreenReceiver);
		if (mShowNotification)
			mAllowDestroy = false;
	}

	private void doStopSelf() {
		mAllowDestroy = true;
		stopSelf();
	}

	private void doRestartSelf() {
		mAllowRestart = true;
		stopSelf();
	}

}
