package com.twinone.locker.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
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

import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.version.VersionUtils;
import com.twinone.locker.version.VersionManager;

public class AlarmService extends Service {

	private static final int REQUEST_CODE = 0x1234AF;
	private static final int NOTIFICATION_ID = 0xABCDEF;
	private static final String TAG = "AlarmService";

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
		public AlarmService getInstance() {
			return AlarmService.this;
		}
	}

	private final class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.i(TAG, "Screen ON");
				mLastPackageName = null;
				startAlarm(AlarmService.this);
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i(TAG, "Screen OFF");
				stopAlarm(AlarmService.this);
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
	private void init() {
		Log.d(TAG, "init");
		if (VersionManager.isDeprecated(this)) {
			Log.i(TAG, "Not starting AlarmService for deprecated version");
			new VersionUtils(this).showDeprecatedNotification();
			stop(this);
			return;
		}

		mExplicitStarted = true;

		mHandler = new Handler();
		mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mUnlockMap = new HashMap<String, Runnable>();
		mLockedPackages = new HashMap<String, Boolean>();
		mScreenReceiver = new ScreenReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenReceiver, filter);

		final Set<String> apps = PrefUtil.getTrackedApps(this);
		for (String s : apps) {
			mLockedPackages.put(s, true);
		}
		SharedPreferences sp = PrefUtil.prefs(this);

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (ACTION_START.equals(intent.getAction())) {
			if (!mExplicitStarted) {
				init();
			}
			checkPackageChanged();
		} else if (ACTION_RESTART.equals(intent.getAction())) {
			if (mExplicitStarted) {
				Log.d(TAG, "ACTION_RESTART");
				init();
			}
		} else if (ACTION_STOP.equals(intent.getAction())) {
			Log.d(TAG, "ACTION_STOP");
			doStopSelf();
		}
		return START_NOT_STICKY;
	}

	private void checkPackageChanged() {
		final String packageName = getTopPackageName();
		if (!packageName.equals(mLastPackageName)) {
			onAppClose(mLastPackageName);
			onAppOpen(packageName);
			Log.d(TAG, packageName);
		}

		// prepare for next call
		mLastPackageName = packageName;
	}

	private void onAppOpen(String packageName) {
		// Log.v(TAG, "open " + packageName);
		if (mLockedPackages.containsKey(packageName)) {
			onLockedAppOpen(packageName);
		}
	}

	private void onLockedAppOpen(String packageName) {
		boolean locked = mLockedPackages.get(packageName);
		if (locked) {
			showLocker(packageName);
		}
		removeRelockTimer(packageName);
	}

	private void showLocker(String packageName) {
		Intent intent = LockViewService.getDefaultIntent(this);
		intent.setAction(LockViewService.ACTION_COMPARE);
		intent.putExtra(LockViewService.EXTRA_PACKAGENAME, packageName);
		startService(intent);

	}

	private void onAppClose(String packageName) {
		if (mLockedPackages.containsKey(packageName)) {
			onLockedAppClose(packageName);
		}
	}

	private void onLockedAppClose(String packageName) {
		setRelockTimer(packageName);
		if (!getPackageName().equals(packageName)) {
			LockViewService.hide(this);
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
		SharedPreferences sp = PrefUtil.prefs(this);
		boolean defaultShowNotification = Boolean
				.parseBoolean(getString(R.string.pref_def_show_notification));
		boolean showNotification = sp.getBoolean(
				getString(R.string.pref_key_show_notification),
				defaultShowNotification);
		mShowNotification = showNotification;

		if (mShowNotification) {
			showNotification();
		} else {
			hideNotification();
		}
	}

	@SuppressLint("InlinedApi")
	private void showNotification() {
		Log.d(TAG, "showNotification");
		SharedPreferences sp = PrefUtil.prefs(this);
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
		nm.notify(NOTIFICATION_ID, nb.build());

	}

	private void hideNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	public static final void start(Context c) {
		startAlarm(c);
	}

	/** Starts the service */
	private static final void startAlarm(Context c) {
		AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		PendingIntent pi = getRunIntent(c);
		SharedPreferences sp = PrefUtil.prefs(c);
		String defaultPerformance = c.getString(R.string.pref_val_perf_normal);
		String s = sp.getString(c.getString(R.string.pref_key_performance),
				defaultPerformance);
		if (s.length() == 0)
			s = "0";
		long interval = Long.parseLong(s);
		long startTime = SystemClock.elapsedRealtime();
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, startTime, interval, pi);
	}

	private static final PendingIntent getRunIntent(Context c) {
		Intent i = new Intent(c, AlarmService.class);
		i.setAction(ACTION_START);
		PendingIntent pi = PendingIntent.getService(c, REQUEST_CODE, i, 0);
		return pi;
	}

	private static final void stopAlarm(Context c) {
		AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		am.cancel(getRunIntent(c));
	}

	/** Stop this service, also stopping the alarm */
	public static final void stop(Context c) {
		stopAlarm(c);
		Intent i = new Intent(c, AlarmService.class);
		i.setAction(ACTION_STOP);
		c.startService(i);
	}

	/**
	 * Re-initialize everything.<br>
	 * This has only effect if the service was explicitly started using
	 * {@link #start(Context)}
	 */

	public static final void restart(Context c) {
		Intent i = new Intent(c, AlarmService.class);
		i.setAction(ACTION_RESTART);
		c.startService(i);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "onDestroy (allowed=" + mAllowDestroy + ")");
		if (!mAllowDestroy) {
			Log.d(TAG, "Destroy not allowed, restarting service");
			start(this);
		}
		if (mScreenReceiver != null)
			unregisterReceiver(mScreenReceiver);
		if (mShowNotification)
			hideNotification();
	}

	private void doStopSelf() {
		mAllowDestroy = true;
		stopSelf();
	}

}
