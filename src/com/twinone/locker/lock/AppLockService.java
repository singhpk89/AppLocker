package com.twinone.locker.lock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.twinone.locker.AppInfo;
import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.UtilPref;

public class AppLockService extends Service {

	public static final String TAG = "Observer";
	private static final String LOCKER_CLASS = LockActivity.class.getName();
	private static final boolean WHATSAPP_WORKAROUND = true; // TODO remove
	private static final long WHATSAPP_WAIT_DELAY = 500;
	private static final int NOTIFICATION_ID = 1337;

	private ActivityManager mAM;
	private ScheduledExecutorService mScheduledExecutor;
	private BroadcastReceiver mScreenReceiver;
	private HashSet<AppInfo> mTrackedApps;

	private String mPassword;
	private String mPattern;

	private String mLastApp = "";
	private String mLastClass = "";

	private boolean mPrefVibrateEnabled;
	private int mPrefLockType = LockActivity.LOCK_TYPE_PASSWORD;
	private boolean mPrefRelockAfterScreenOff;
	private boolean mPrefDelayUnlockEnabled;
	private long mPrefDelayUnlockMillis;
	private Handler mDelayUnlockHandler;
	private boolean mPrefShowNotification;
	private int mPrefNotificationPriority;

	@SuppressWarnings("unused")
	private boolean mScreenOn = true;
	private boolean mExplicitStarted;

	@Override
	public IBinder onBind(Intent i) {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public AppLockService getInstance() {
			return AppLockService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate");
		mAM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mScreenReceiver = new ScreenReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenReceiver, filter);
	}

	/**
	 * Shows the notification and starts the service foreground
	 */
	private void startNotification() {
		// Cancel previous notifications
		// NotificationManager does not work.
		// NotificationManager nm = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		// nm.cancel(NOTIFICATION_ID);
		stopForeground(true);

		if (mPrefShowNotification) {
			Intent i = new Intent(this, MainActivity.class);
			PendingIntent.getActivity(this, 0, i, 0);
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
			nb.setPriority(mPrefNotificationPriority);
			startForeground(NOTIFICATION_ID, nb.build());
		} else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Hack for 4.2 and below to get system priority
			@SuppressWarnings("deprecation")
			Notification n = new Notification(0, null,
					System.currentTimeMillis());
			n.flags |= Notification.FLAG_NO_CLEAR;
			startForeground(NOTIFICATION_ID, n);
		}
	}

	/** Checks whether this service is in foreground (StackOverflow) */
	// private void checkForeground() {
	// ActivityManager am = (ActivityManager) this
	// .getSystemService(ACTIVITY_SERVICE);
	// List<RunningServiceInfo> l = am.getRunningServices(Integer.MAX_VALUE);
	// Iterator<RunningServiceInfo> i = l.iterator();
	// while (i.hasNext()) {
	// RunningServiceInfo rsi = (RunningServiceInfo) i.next();
	// if (rsi.service.getPackageName().equals(this.getPackageName())) {
	// if (rsi.foreground) {
	// Log.d(TAG, "Service is in foreground");
	// return;
	// }
	// }
	// }
	// Log.d(TAG, "Service is NOT in foreground");
	// }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		mExplicitStarted = true;
		loadPreferences();
		restart();

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "onDestroy");
		stopScheduler();
		mAM = null;
		mTrackedApps = null;
		stopForeground(true);
		unregisterReceiver(mScreenReceiver);
	}

	/**
	 * Loads or reloads the preferences at runtime and automatically adapts the
	 * service to match the new preferences.
	 */
	public void loadPreferences() {

		/*
		 * VARIABLES:
		 */
		SharedPreferences sp = UtilPref.prefs(this);
		boolean defaultDelay = Boolean
				.parseBoolean(getString(R.string.pref_def_delay_status));
		boolean delayEnabled = sp.getBoolean(
				getString(R.string.pref_key_delay_status), defaultDelay);
		mPrefDelayUnlockEnabled = delayEnabled;

		String delaySeconds = sp.getString(
				getString(R.string.pref_key_delay_time),
				getString(R.string.pref_def_delay_time));

		if (delaySeconds.length() == 0) {
			delaySeconds = "0";
		}

		mPrefDelayUnlockMillis = (Long.parseLong(delaySeconds) * 1000);

		boolean defaultScreenOffRelock = Boolean
				.parseBoolean(getString(R.string.pref_def_relock_after_screenoff));
		boolean relock = sp.getBoolean(
				getString(R.string.pref_key_relock_after_screenoff),
				defaultScreenOffRelock);
		mPrefRelockAfterScreenOff = relock;

		boolean defaultShowNotification = Boolean
				.parseBoolean(getString(R.string.pref_def_show_notification));
		boolean showNotification = sp.getBoolean(
				getString(R.string.pref_key_show_notification),
				defaultShowNotification);
		mPrefShowNotification = showNotification;

		boolean defaultTransparentNotification = Boolean
				.parseBoolean(getString(R.string.pref_def_transparent_notification));
		boolean transparentNotification = sp.getBoolean(
				getString(R.string.pref_key_transparent_notification),
				defaultTransparentNotification);
		mPrefNotificationPriority = transparentNotification ? Notification.PRIORITY_MIN
				: Notification.PRIORITY_DEFAULT;

		mPassword = UtilPref.getPassword(sp, this);
		mPattern = UtilPref.getPattern(sp, this);

		// String lockType =
		// sp.getString(getString(R.string.pref_key_lock_type),
		// getString(R.string.pref_val_lock_type_password));
		// if (lockType.equals(getString(R.string.pref_val_lock_type_password)))
		// {
		// mPrefLockType = LockActivity.LOCK_TYPE_PASSWORD;
		// } else if (lockType
		// .equals(getString(R.string.pref_val_lock_type_pattern))) {
		// mPrefLockType = LockActivity.LOCK_TYPE_PATTERN;
		// }
		mPrefLockType = UtilPref.getLockTypeInt(sp, this);
		boolean vibdef = Boolean
				.parseBoolean(getString(R.string.pref_def_vibrate_keypress));
		mPrefVibrateEnabled = sp.getBoolean(
				getString(R.string.pref_key_vibrate_keypress), vibdef);

		loadTrackedApps();
	}

	/**
	 * Restarts everything in the service if the service was started manually.
	 * (Notification, Scheduler)
	 */
	public void restart() {
		if (mExplicitStarted) {
			startScheduler();
			startNotification();
		}
	}

	/**
	 * Starts (or restarts) the ScheduledExecutor with the correct peformance
	 * delay.
	 */
	private void startScheduler() {
		// Shutdown first if it's not running
		if (mScheduledExecutor != null) {
			mScheduledExecutor.shutdownNow();
			mScheduledExecutor = null;
		}
		String defaultDelay = getString(R.string.pref_val_perf_normal);
		SharedPreferences sp = UtilPref.prefs(this);
		String s = sp.getString(getString(R.string.pref_key_performance),
				defaultDelay);
		long delay = Long.parseLong(s);
		mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		mScheduledExecutor.scheduleWithFixedDelay(new PackageMonitor(), 0,
				delay, TimeUnit.MILLISECONDS);
	}

	private void stopScheduler() {
		if (mScheduledExecutor != null) {
			mScheduledExecutor.shutdownNow();
			mScheduledExecutor = null;
		} else {
			Log.w(TAG, "Attempted to stop scheduler, but it was already null");
		}
	}

	class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.i("TAG", "Screen ON");
				mScreenOn = true;
				startScheduler();
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i("TAG", "Screen OFF");
				mScreenOn = false;
				stopScheduler();
				if (mPrefRelockAfterScreenOff) {
					lockAll();
				}

			}
		}
	};

	protected int mTest = 0;

	private class PackageMonitor implements Runnable {
		@Override
		public void run() {
			// long mBegin = System.nanoTime();
			// long mBegin = System.currentTimeMillis();

			ComponentName app = mAM.getRunningTasks(1).get(0).topActivity;
			String appName = app.getPackageName();
			String className = app.getClassName();

			boolean appChanged = !appName.equals(mLastApp);
			boolean classChanged = !className.equals(mLastClass);

			if (classChanged || appChanged) {
				Log.d(TAG, "" + appName + " " + className);
			}
			onObserve(appName, className);

			mLastClass = className;
			mLastApp = appName;

			// long mEnd = System.nanoTime();
			// long mEnd = System.currentTimeMillis();
			// if (classChanged || appChanged) {
			// Log.d(TAG, "" + mBegin);
			// Log.d(TAG, "" + mEnd);
			// Log.d(TAG, "" + (double) ((mEnd - mBegin)) + " ms");
			// // Log.d(TAG, "" + (double) ((mEnd - mBegin) / 1000000) +
			// // " ms");
			// Log.d(TAG, "----------------------");
			// }
		}

		/**
		 * Called each time the monitor has observed a package in the front. The
		 * package can be the same as the previous.
		 * 
		 * @param appName
		 * @param className
		 */
		private void onObserve(String appName, String className) {
			// Log.v(TAG, "Package: " + appName);
			if (className.equals(LOCKER_CLASS)) {
				return;
			}
			AppInfo app = getLockInfoByPackageName(appName);
			if (app != null) {
				if (app.locked) {
					app.className = className;
					Log.v(TAG,
							"Show locker for " + app.packageName
									+ app.hashCode());
					showLocker(app);
				}
			}
			// lock all other apps because they're not in front anymore
			lock(appName);
		}
	}

	/**
	 * Locks ALL apps (Useful when screen is turned off)
	 */
	private void lockAll() {
		for (AppInfo li : mTrackedApps) {
			if (li.locked == false) {
				Log.v(TAG, "relockAll() " + li.packageName);
				li.locked = true;

			}
		}
	}

	/**
	 * Locks all apps except the one matching the provided string.
	 * 
	 * @param appToExclude
	 *            The app that must NOT be locked.
	 */
	private void lock(String appToExclude) {
		// TODO timing
		for (AppInfo li : mTrackedApps) {
			if (!li.packageName.equals(appToExclude)) {
				if (li.locked == false) {
					Log.v(TAG, "relock() " + li.packageName);
					li.locked = true;
				}
			}
		}
	}

	/**
	 * Unlock a single application. Should be called by {@link LockActivity}
	 * 
	 * @param appName
	 */
	public void unlock(String appName) {
		Log.d(TAG, "doUnlock called");
		AppInfo li = getLockInfoByPackageName(appName);
		if (li != null) {
			if (li.locked == true) {
				Log.i(TAG, "Unlocked in list: " + li.packageName);
			} else {
				Log.w(TAG, "Tried to unlock " + li.hashCode()
						+ " but was not locked");
			}
			li.locked = false;
		} else {
			Log.w(TAG, "Not unlocked " + appName + ": not in list.");
		}
		if (mPrefDelayUnlockEnabled) {
			stopScheduler();
			if (mDelayUnlockHandler == null) {
				mDelayUnlockHandler = new Handler();
			}
			mDelayUnlockHandler.removeCallbacksAndMessages(null);
			mDelayUnlockHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					startScheduler();
				}
			}, mPrefDelayUnlockMillis);
		}
	}

	private AppInfo getLockInfoByPackageName(String packageName) {
		if (mTrackedApps == null) {
			return null;
		}
		for (AppInfo li : mTrackedApps) {
			if (li.packageName.equals(packageName)) {
				return li;
			}
		}
		return null;
	}

	/**
	 * Display a {@link LockActivity} for this {@link AppInfo}.
	 * 
	 * @param lockInfo
	 *            The {@link AppInfo} to lock.
	 */
	@SuppressLint("NewApi")
	private void showLocker(AppInfo lockInfo) {
		if (mPassword.length() == 0) {
			Log.w(TAG, "Not showing lock for empty password:"
					+ lockInfo.packageName);
			return;
		}
		Log.d(TAG, "Starting locker for " + lockInfo.packageName);
		whatsappWorkaround(lockInfo); // TODO remove when whatsapp fix

		Intent intent = new Intent(AppLockService.this, LockActivity.class);
		intent.setAction(LockActivity.ACTION_COMPARE);
		intent.putExtra(LockActivity.EXTRA_PASSWORD, mPassword);
		intent.putExtra(LockActivity.EXTRA_PATTERN, mPattern);
		intent.putExtra(LockActivity.EXTRA_PACKAGENAME, lockInfo.packageName);
		intent.putExtra(LockActivity.EXTRA_VIEW_TYPE, mPrefLockType);
		intent.putExtra(LockActivity.EXTRA_VIBRATE, mPrefVibrateEnabled);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			Bundle b = ActivityOptions.makeCustomAnimation(this,
					android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
			startActivity(intent, b);
		} else {
			startActivity(intent);
		}

	}

	/**
	 * BugFix for Whatsapp's lose focus.
	 * 
	 * @param li
	 */
	private void whatsappWorkaround(AppInfo li) {
		if (WHATSAPP_WORKAROUND) {
			if (li.className.equals("com.whatsapp.Conversation")) {
				try {
					Log.i(TAG, "Sleeping for whatsapp bug");
					Thread.sleep(WHATSAPP_WAIT_DELAY);
				} catch (InterruptedException e) {
					Log.e(TAG, "Interrupted while whatsapp workaround");
				}
			}
		}
	}

	public final void loadTrackedApps() {
		Set<String> apps = UtilPref.getTrackedApps(UtilPref.appsPrefs(this),
				this);
		mTrackedApps = new HashSet<AppInfo>();
		for (String s : apps) {
			mTrackedApps.add(new AppInfo(s));
		}
	}

	/**
	 * Tracks or untracks an app
	 * 
	 * @param packageNames
	 * @param shouldTrack
	 *            True if the new state will be tracking, false if not
	 */
	public final void setTracking(boolean shouldTrack, String... packageNames) {
		SharedPreferences.Editor editor = UtilPref.appsPrefs(this).edit();
		for (String packageName : packageNames) {
			if (shouldTrack) {
				editor.putBoolean(packageName, true);
			} else {
				editor.remove(packageName);
			}
		}
		boolean commited = editor.commit();
		Log.d(TAG, "Editor.commit: " + commited);
		if (!commited) {
			Log.w(TAG, "Not commited!");
		}
		loadTrackedApps();
	}
}
