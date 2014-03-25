//package com.twinone.locker.lock;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//import android.annotation.SuppressLint;
//import android.app.ActivityManager;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.os.Binder;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//import com.twinone.locker.AppInfo;
//import com.twinone.locker.MainActivity;
//import com.twinone.locker.R;
//import com.twinone.locker.util.PrefUtil;
//import com.twinone.locker.version.VersionManager;
//
//public class AppLockService extends Service {
//
//	public static final String TAG = "AppLockService";
//	public static final int NOTIFICATION_ID = 1337;
//
//	public static final String ACTION_START = "com.twinone.locker.service.action_start";
//	/**
//	 * Reloads the preferences, and stops the service if it was NOT running
//	 * before this call
//	 */
//	public static final String ACTION_RELOAD_PREFERENCES = "com.twinone.locker.service.action.reload";
//	public static final String ACTION_STOP = "com.twinone.locker.service.action.stop_service";
//	private ActivityManager mAM;
//	private ScheduledExecutorService mExecutor;
//	private BroadcastReceiver mScreenReceiver;
//	private HashSet<AppInfo> mTrackedApps;
//
//	private String mLastPackageName = "";
//	private String mLastClassName = "";
//
//	private boolean mPrefRelockAfterScreenOff;
//	private boolean mPrefDelayUnlockEnabled;
//	private long mPrefDelayUnlockMillis;
//	private Handler mUnlockHandler;
//	private boolean mPrefShowNotification;
//	private int mPrefNotificationPriority;
//	private HashMap<String, Runnable> mUnlockMap;
//
//	@SuppressWarnings("unused")
//	private boolean mScreenOn = true;
//	private boolean mExplicitStarted;
//
//	@Override
//	public IBinder onBind(Intent i) {
//		return new LocalBinder();
//	}
//
//	public class LocalBinder extends Binder {
//		public AppLockService getInstance() {
//			return AppLockService.this;
//		}
//	}
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		Log.v(TAG, "onCreate");
//		mAM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//		mScreenReceiver = new ScreenReceiver();
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Intent.ACTION_SCREEN_ON);
//		filter.addAction(Intent.ACTION_SCREEN_OFF);
//		registerReceiver(mScreenReceiver, filter);
//	}
//
//	/**
//	 * Shows the notification and starts the service foreground
//	 */
//	private void startNotification() {
//		// Cancel previous notifications
//		// NotificationManager does not work.
//		// NotificationManager nm = (NotificationManager)
//		// getSystemService(NOTIFICATION_SERVICE);
//		// nm.cancel(NOTIFICATION_ID);
//		stopForeground(true);
//
//		if (mPrefShowNotification) {
//			Intent i = new Intent(this, MainActivity.class);
//			PendingIntent.getActivity(this, 0, i, 0);
//			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
//			String title = getString(R.string.notification_title);
//			String content = getString(R.string.notification_state_locked);
//			NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
//			nb.setSmallIcon(R.drawable.ic_launcher);
//			nb.setContentTitle(title);
//			nb.setContentText(content);
//			nb.setWhen(System.currentTimeMillis());
//			nb.setContentIntent(pi);
//			nb.setOngoing(true);
//			nb.setPriority(mPrefNotificationPriority);
//			startForeground(NOTIFICATION_ID, nb.build());
//		} else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//			// Hack for 4.2 and below to get system priority
//			@SuppressWarnings("deprecation")
//			Notification n = new Notification(0, null,
//					System.currentTimeMillis());
//			n.flags |= Notification.FLAG_NO_CLEAR;
//			startForeground(NOTIFICATION_ID, n);
//		}
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.d(TAG, "onStartCommand");
//		if (VersionManager.isDeprecated(this)) {
//			Intent i = new Intent(this, MainActivity.class);
//			PendingIntent.getActivity(this, 0, i, 0);
//			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
//			NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
//			nb.setSmallIcon(R.drawable.ic_launcher);
//			nb.setContentTitle(getString(R.string.update_needed));
//			nb.setContentText(getString(R.string.update_needed_msg));
//			nb.setWhen(System.currentTimeMillis());
//			nb.setOngoing(false);
//			nb.setContentIntent(pi);
//			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//			nm.notify(0, nb.build());
//			stopSelf();
//			return START_NOT_STICKY;
//		}
//		if (intent == null || ACTION_START.equals(intent.getAction())) {
//			Log.i(TAG, "Starting service");
//			if (!mExplicitStarted) {
//				mExplicitStarted = true;
//				mUnlockMap = new HashMap<String, Runnable>();
//				mUnlockHandler = new Handler();
//				loadPreferences();
//				restart();
//			}
//		} else if (ACTION_RELOAD_PREFERENCES.equals(intent.getAction())) {
//			if (mExplicitStarted) {
//				Log.d(TAG, "Reloading prefs, service was already running");
//				loadPreferences();
//				restart();
//			} else {
//				Log.v(TAG, "Got RELOAD_PREFERENCES but service was not started");
//				stopSelf();
//			}
//		} else if (ACTION_STOP.equals(intent.getAction())) {
//			stopSelf();
//		} else {
//			Log.w(TAG, "no action specified");
//		}
//		super.onStartCommand(intent, flags, startId);
//		return START_STICKY;
//	}
//
//	/**
//	 * Use this intent to start the service
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static final Intent getStartIntent(final Context context) {
//		final Intent intent = new Intent(context, AppLockService.class);
//		intent.setAction(ACTION_START);
//		return intent;
//	}
//
//	/**
//	 * This intent can be used to update the service when some configuration
//	 * change has happened. If {@link AppLockService} receives this
//	 * {@link Intent} and it was not explicitly started before, the service will
//	 * exit doing nothing.
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static final Intent getReloadIntent(final Context context) {
//		final Intent intent = new Intent(context, AppLockService.class);
//		intent.setAction(ACTION_RELOAD_PREFERENCES);
//		return intent;
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Log.w(TAG, "onDestroy");
//		stopScheduler();
//		mAM = null;
//		mTrackedApps = null;
//		stopForeground(true);
//		unregisterReceiver(mScreenReceiver);
//	}
//
//	/**
//	 * Loads or reloads the preferences at runtime and automatically adapts the
//	 * service to match the new preferences.
//	 */
//	@SuppressLint("InlinedApi")
//	public void loadPreferences() {
//
//		/*
//		 * VARIABLES:
//		 */
//		SharedPreferences sp = PrefUtil.prefs(this);
//		boolean defaultDelay = Boolean
//				.parseBoolean(getString(R.string.pref_def_delay_status));
//		boolean delayEnabled = sp.getBoolean(
//				getString(R.string.pref_key_delay_status), defaultDelay);
//		mPrefDelayUnlockEnabled = delayEnabled;
//
//		String delaySeconds = sp.getString(
//				getString(R.string.pref_key_delay_time),
//				getString(R.string.pref_def_delay_time));
//
//		if (delaySeconds.length() == 0) {
//			delaySeconds = "0";
//		}
//
//		mPrefDelayUnlockMillis = (Long.parseLong(delaySeconds) * 1000);
//
//		boolean defaultScreenOffRelock = Boolean
//				.parseBoolean(getString(R.string.pref_def_relock_after_screenoff));
//		boolean relock = sp.getBoolean(
//				getString(R.string.pref_key_relock_after_screenoff),
//				defaultScreenOffRelock);
//		mPrefRelockAfterScreenOff = relock;
//
//		boolean defaultShowNotification = Boolean
//				.parseBoolean(getString(R.string.pref_def_show_notification));
//		boolean showNotification = sp.getBoolean(
//				getString(R.string.pref_key_show_notification),
//				defaultShowNotification);
//		mPrefShowNotification = showNotification;
//
//		boolean defHideNotifIcon = Boolean
//				.parseBoolean(getString(R.string.pref_def_hide_notification_icon));
//		boolean hideNotifIcon = sp.getBoolean(
//				getString(R.string.pref_key_hide_notification_icon),
//				defHideNotifIcon);
//		mPrefNotificationPriority = hideNotifIcon ? Notification.PRIORITY_MIN
//				: Notification.PRIORITY_DEFAULT;
//
//		// Apps
//		final Set<String> apps = PrefUtil.getTrackedApps(this);
//		mTrackedApps = new HashSet<AppInfo>();
//		for (String s : apps) {
//			mTrackedApps.add(new AppInfo(s));
//		}
//
//	}
//
//	/**
//	 * Restarts everything in the service if the service was started manually.
//	 * (Notification, Scheduler)
//	 */
//	public void restart() {
//		if (mExplicitStarted) {
//			startScheduler();
//			startNotification();
//		}
//	}
//
//	/**
//	 * Starts (or restarts) the ScheduledExecutor with the correct peformance
//	 * delay.
//	 */
//	private void startScheduler() {
//		// Trigger a change on the first active package
//		mLastPackageName = null;
//		mLastClassName = null;
//
//		// Shutdown first if it's not running
//		if (mExecutor != null) {
//			mExecutor.shutdownNow();
//			mExecutor = null;
//		}
//		String defaultDelay = getString(R.string.pref_val_perf_normal);
//		SharedPreferences sp = PrefUtil.prefs(this);
//		String s = sp.getString(getString(R.string.pref_key_performance),
//				defaultDelay);
//		long delay = Long.parseLong(s);
//		mExecutor = Executors.newSingleThreadScheduledExecutor();
//		mExecutor.scheduleWithFixedDelay(new PackageMonitor(), 0, delay,
//				TimeUnit.MILLISECONDS);
//	}
//
//	private void stopScheduler() {
//		if (mExecutor != null) {
//			mExecutor.shutdownNow();
//			mExecutor = null;
//		}
//	}
//
//	private final class ScreenReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//				Log.i("TAG", "Screen ON");
//				mScreenOn = true;
//				startScheduler();
//
//			}
//			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//				Log.i("TAG", "Screen OFF");
//				mScreenOn = false;
//				stopScheduler();
//				if (mPrefRelockAfterScreenOff) {
//					lockAll();
//				}
//
//			}
//		}
//	};
//
//	protected int mTest = 0;
//
//	private class PackageMonitor implements Runnable {
//		@Override
//		public void run() {
//			ComponentName app = mAM.getRunningTasks(1).get(0).topActivity;
//			String packageName = app.getPackageName();
//			String className = app.getClassName();
//
//			boolean appChanged = !packageName.equals(mLastPackageName);
//			// boolean classChanged = !className.equals(mLastClass);
//
//			if (appChanged) {
//				Log.d(TAG, "Open: " + packageName + " (" + className + ")");
//				onAppOpen(packageName, className);
//				onAppClose(mLastPackageName, mLastClassName);
//			}
//			mLastPackageName = packageName;
//			mLastClassName = className;
//		}
//	}
//
//	/**
//	 * Called each time the monitor has observed a package in the front. The
//	 * package can be the same as the previous.
//	 * 
//	 * @param packageName
//	 * @param className
//	 */
//	private void onAppOpen(final String packageName, final String className) {
//		AppInfo app = getLocalAppInfo(packageName);
//		if (app != null) {
//			Log.d(TAG, "test");
//			if (mPrefDelayUnlockEnabled) {
//				mUnlockHandler.removeCallbacks(mUnlockMap.get(packageName));
//			}
//			if (app.locked) {
//				Log.d(TAG, "locked");
//				app.className = className;
//				showLocker(app);
//			} else {
//				Log.d(TAG, "not locked");
//				notifyPackageChanged(this, packageName);
//			}
//		} else {
//			// this app is not locked, notify LockViewService so it can hide
//			// it's view
//			notifyPackageChanged(this, packageName);
//		}
//	}
//
//	private void onAppClose(final String packageName, final String className) {
//
//		final AppInfo ai = getLocalAppInfo(packageName);
//		if (ai != null) {
//			if (mPrefDelayUnlockEnabled) {
//				Runnable r = new Runnable() {
//					@Override
//					public void run() {
//						Log.i(TAG, "Closing after delay:" + packageName);
//						ai.locked = true;
//					}
//				};
//				mUnlockMap.put(packageName, r);
//				mUnlockHandler.postDelayed(r, mPrefDelayUnlockMillis);
//			} else {
//				ai.locked = true;
//			}
//		}
//
//	}
//
//	/**
//	 * Locks ALL apps (Useful when screen is turned off)
//	 */
//	private void lockAll() {
//		mUnlockHandler.removeCallbacksAndMessages(null);
//		for (AppInfo li : mTrackedApps) {
//			if (li.locked == false) {
//				Log.v(TAG, "relockAll() matched: " + li.packageName);
//				li.locked = true;
//
//			}
//		}
//	}
//
//	/**
//	 * Locks an app in the current list
//	 */
//	@SuppressWarnings("unused")
//	private void lockApp(final String app) {
//		for (AppInfo li : mTrackedApps) {
//			if (li.packageName.equals(app)) {
//				if (!li.locked) {
//					Log.v(TAG, "lockApp() " + li.packageName);
//					li.locked = true;
//				}
//			}
//		}
//	}
//
//	/**
//	 * Unlock a single application. Should be called by {@link LockActivity}
//	 * 
//	 * @param appName
//	 */
//	public void unlockApp(final String appName) {
//		if (mTrackedApps == null) {
//			return;
//		}
//		for (AppInfo li : mTrackedApps) {
//			if (li.packageName.equals(appName)) {
//				if (li.locked) {
//					Log.v(TAG, "unlockApp() " + li.packageName);
//					li.locked = false;
//				}
//			}
//		}
//	}
//
//	/**
//	 * Returns an app info from mTrackedApps if the app is locked.
//	 * 
//	 * @param packageName
//	 * @return
//	 */
//	private AppInfo getLocalAppInfo(String packageName) {
//		if (mTrackedApps == null) {
//			return null;
//		}
//		for (AppInfo li : mTrackedApps) {
//			if (li.packageName.equals(packageName)) {
//				return li;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Display a {@link LockActivity} for this {@link AppInfo}.
//	 * 
//	 * @param lockInfo
//	 *            The {@link AppInfo} to lock.
//	 */
//	private void showLocker(AppInfo lockInfo) {
//
//		Log.d(TAG, "Starting LockView for " + lockInfo.packageName);
//
//		Intent intent = LockViewService.getDefaultIntent(this);
//		intent.setAction(LockViewService.ACTION_COMPARE);
//		intent.putExtra(LockViewService.EXTRA_PACKAGENAME, lockInfo.packageName);
//		startService(intent);
//
//	}
//
//	public static void notifyPackageChanged(Context c, String packageName) {
//		Log.d(TAG, "notifyPackageChanged");
//
//		Intent intent = new Intent(c, LockViewService.class);
//		intent.setAction(LockViewService.ACTION_NOTIFY_PACKAGE_CHANGED);
//		intent.putExtra(LockViewService.EXTRA_PACKAGENAME, packageName);
//		c.startService(intent);
//	}
//
//
//
//	public static Intent getStopIntent(Context c) {
//		Intent i = new Intent(c, AppLockService.class);
//		i.setAction(ACTION_STOP);
//		return i;
//
//	}
//}
