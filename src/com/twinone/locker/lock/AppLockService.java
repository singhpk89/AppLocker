package com.twinone.locker.lock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.twinone.locker.AppInfo;
import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.util.PrefUtil;
import com.twinone.util.VersionChecker;

public class AppLockService extends Service {

	public static final String TAG = "Service";
	private static final String LOCKER_CLASS = LockActivity.class.getName();
	private static final int NOTIFICATION_ID = 1337;

	public static final String ACTION_START = "com.twinone.locker.service.action_start";
	/**
	 * Reloads the preferences, and stops the service if it was NOT running
	 * before this call
	 */
	public static final String ACTION_RELOAD_PREFERENCES = "com.twinoen.locker.service.action.reload";

	private ActivityManager mAM;
	private ScheduledExecutorService mScheduledExecutor;
	private BroadcastReceiver mScreenReceiver;
	private HashSet<AppInfo> mTrackedApps;

	private String mLastApp = "";
	private String mLastClass = "";

	private boolean mPrefRelockAfterScreenOff;
	private boolean mPrefDelayUnlockEnabled;
	private long mPrefDelayUnlockMillis;
	private Handler mUnlockHandler;
	private boolean mPrefShowNotification;
	private int mPrefNotificationPriority;
	private HashMap<String, Runnable> mUnlockMap;

	private Intent mUnlockIntent;

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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (VersionChecker.isDeprecated(this)) {
			Log.d(TAG, "Service not started, this version is deprecated");
			stopSelf();
			return START_NOT_STICKY;
		}

		if (intent == null || ACTION_START.equals(intent.getAction())) {
			Log.i(TAG, "Starting service");
			if (!mExplicitStarted) {
				mExplicitStarted = true;
				mUnlockMap = new HashMap<String, Runnable>();
				mUnlockHandler = new Handler();
				loadPreferences();
				restart();
			}
		} else if (ACTION_RELOAD_PREFERENCES.equals(intent.getAction())) {
			if (mExplicitStarted) {
				Log.i(TAG, "Reloading prefs, service was running");
				loadPreferences();
				restart();
			} else {
				Log.i(TAG, "Stopping self, service was not running");
				stopSelf();
				return START_NOT_STICKY;
			}
		} else {
			Log.w(TAG, "no action specified");
		}

		return START_STICKY;
	}

	/**
	 * Use this intent to start the service
	 * 
	 * @param context
	 * @return
	 */
	public static final Intent getStartIntent(final Context context) {
		final Intent intent = new Intent(context, AppLockService.class);
		intent.setAction(ACTION_START);
		return intent;
	}

	/**
	 * This intent can be used to update the service when some configuration
	 * change has happened. If {@link AppLockService} receives this
	 * {@link Intent} and it was not explicitly started before, the service will
	 * exit doing nothing.
	 * 
	 * @param context
	 * @return
	 */
	public static final Intent getReloadIntent(final Context context) {
		final Intent intent = new Intent(context, AppLockService.class);
		intent.setAction(ACTION_RELOAD_PREFERENCES);
		return intent;
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
	@SuppressLint("InlinedApi")
	public void loadPreferences() {

		/*
		 * VARIABLES:
		 */
		SharedPreferences sp = PrefUtil.prefs(this);
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

		mUnlockIntent = LockActivity.getDefaultIntent(this);
		mUnlockIntent.setAction(LockActivity.ACTION_COMPARE);
		mUnlockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Apps
		final Set<String> apps = PrefUtil.getTrackedApps(this);
		mTrackedApps = new HashSet<AppInfo>();
		for (String s : apps) {
			mTrackedApps.add(new AppInfo(s));
		}

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
		// Trigger a change on the first active package
		mLastApp = null;
		mLastClass = null;

		
		// Shutdown first if it's not running
		if (mScheduledExecutor != null) {
			mScheduledExecutor.shutdownNow();
			mScheduledExecutor = null;
		}
		String defaultDelay = getString(R.string.pref_val_perf_normal);
		SharedPreferences sp = PrefUtil.prefs(this);
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
		}
	}

	private final class ScreenReceiver extends BroadcastReceiver {

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
			ComponentName app = mAM.getRunningTasks(1).get(0).topActivity;
			String appName = app.getPackageName();
			String className = app.getClassName();

			boolean appChanged = !appName.equals(mLastApp);
			// boolean classChanged = !className.equals(mLastClass);

			if (appChanged) {
				// Log.d(TAG, "Close: " + mLastClass);
				// Log.d(TAG, "Open: " + className);
				if (!className.equals(LOCKER_CLASS)) {
					Log.d(TAG, "Open: " + className);
					onOpen(appName, className);
					onClose(mLastApp, mLastClass);
				}
			}
			mLastApp = appName;
			mLastClass = className;
		}

		/**
		 * Called each time the monitor has observed a package in the front. The
		 * package can be the same as the previous.
		 * 
		 * @param appName
		 * @param className
		 */
		private void onOpen(final String appName, final String className) {
			// Log.v(TAG, "Package: " + appName);
			AppInfo app = getAppInfo(appName);
			if (app != null) {
				if (mPrefDelayUnlockEnabled) {
					mUnlockHandler.removeCallbacks(mUnlockMap.get(appName));
				}
				if (app.locked) {
					app.className = className;
					// Log.v(TAG,
					// "Show locker for " + app.packageName
					// + app.hashCode());
					showLocker(app);
				}
			}
			// This was here before because onClose was not implemented
			// // lock all other apps because they're not in front anymore
			// lockAppExcept(appName);
		}

		private void onClose(final String appName, final String className) {

			final AppInfo ai = getAppInfo(appName);
			if (ai != null) {
				if (mPrefDelayUnlockEnabled) {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "Closing after delay:" + appName);
							ai.locked = true;
						}
					};
					mUnlockMap.put(appName, r);
					mUnlockHandler.postDelayed(r, mPrefDelayUnlockMillis);
				} else {
					ai.locked = true;
				}
			}

		}
	}

	/**
	 * Locks ALL apps (Useful when screen is turned off)
	 */
	private void lockAll() {
		mUnlockHandler.removeCallbacksAndMessages(null);
		for (AppInfo li : mTrackedApps) {
			if (li.locked == false) {
				Log.v(TAG, "relockAll() matched: " + li.packageName);
				li.locked = true;

			}
		}
	}

	/**
	 * Locks an app in the current list
	 */
	@SuppressWarnings("unused")
	private void lockApp(final String app) {
		for (AppInfo li : mTrackedApps) {
			if (li.packageName.equals(app)) {
				if (!li.locked) {
					Log.v(TAG, "lockApp() " + li.packageName);
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
	public void unlockApp(final String appName) {
		for (AppInfo li : mTrackedApps) {
			if (li.packageName.equals(appName)) {
				if (li.locked) {
					Log.v(TAG, "unlockApp() " + li.packageName);
					li.locked = false;
				}
			}
		}
	}

	private AppInfo getAppInfo(String packageName) {
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
	private void showLocker(AppInfo lockInfo) {

		if (lockInfo.className.equals("com.whatsapp.Conversation")) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.d(TAG, "Starting locker for " + lockInfo.packageName);

		mUnlockIntent.putExtra(LockActivity.EXTRA_PACKAGENAME,
				lockInfo.packageName);
		startActivity(mUnlockIntent);

	}

	/**
	 * Tracks or untracks an app
	 * 
	 * @param packageNames
	 * @param shouldTrack
	 *            True if the new state will be tracking, false if not
	 */
	public final void setTracking(boolean shouldTrack, String... packageNames) {
		SharedPreferences.Editor editor = PrefUtil.appsPrefs(this).edit();
		for (String packageName : packageNames) {
			if (shouldTrack) {
				editor.putBoolean(packageName, true);
			} else {
				editor.remove(packageName);
			}
		}
		PrefUtil.apply(editor);
	}
}
