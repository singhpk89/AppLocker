package com.twinone.locker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class ObserverService extends Service {

	public static final String EXTRA_MESSAGE = "com.twinone.locker.extra.Message";
	public static final String EXTRA_TARGET_PACKAGENAME = "com.twinone.locker.extra.Info";
	public static final String TAG = "Observer";

	public static final String PREF_FILE_DEFAULT = "com.twinone.locker.prefs.default";
	private static final String PREF_FILE_APPS = "com.twinone.locker.prefs.apps";

	private static final String LOCKER_CLASS = LockActivity.class.getName();

	private String mPassword;
	private String lastApp = "";
	private String lastClass = "";

	private boolean mScreenOn = true;
	private long mRelockDelay = 10000;

	private ActivityManager am;
	private ScheduledExecutorService mExecutor;
	BroadcastReceiver mScreenStatusReceiver;

	/** In this map information about apps will be */
	private HashSet<LockInfo> trackedApps;

	@Override
	public IBinder onBind(Intent i) {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public ObserverService getInstance() {
			return ObserverService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.w(TAG, "onCreate");

		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mPassword = getPassword(this);
		trackedApps = new HashSet<LockInfo>();

		// // trackedApps.add(new LockInfo("com.whatsapp"));
		// trackedApps.add(new LockInfo("com.twitter.android"));

		// Until API level 17 (Android 4.2) an empty notification can be set
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			@SuppressWarnings("deprecation")
			Notification n = new Notification(0, null,
					System.currentTimeMillis());
			n.flags |= Notification.FLAG_NO_CLEAR;
			startForeground(42, n);
		} else {
			// TODO Find a workaround for this
			// Since android 4.3, a system notification is generated

		}

		mScreenStatusReceiver = new ScreenReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenStatusReceiver, filter);

		updateTrackedApps();

		// Start the monitoring
		startScheduler();
	}

	private void startScheduler() {
		if (mExecutor == null) {
			String defaultDelay = getString(R.string.pref_val_perf_normal);
			long delay;
			SharedPreferences sp = getSharedPreferences(
					ObserverService.PREF_FILE_DEFAULT, Context.MODE_PRIVATE);
			String s = sp.getString(getString(R.string.pref_key_performance),
					defaultDelay);
			delay = Long.parseLong(s);
			mExecutor = Executors.newSingleThreadScheduledExecutor();
			mExecutor.scheduleWithFixedDelay(new PackageMonitor(), 0, delay,
					TimeUnit.MILLISECONDS);
		}

	}

	private void stopScheduler() {
		if (mExecutor == null) {
			mExecutor.shutdownNow();
			mExecutor = null;
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

				SharedPreferences sp = context
						.getSharedPreferences(
								ObserverService.PREF_FILE_DEFAULT,
								Context.MODE_PRIVATE);
				boolean relock = sp.getBoolean(context
						.getString(R.string.pref_key_relock_after_screenoff),
						true);
				if (relock) {
					relockAll();
				}

			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	private class PackageMonitor extends Thread {
		@Override
		public void run() {
			// Avoid battery drain when screen off.

			ComponentName app = am.getRunningTasks(1).get(0).topActivity;

			String appName = app.getPackageName();
			String className = app.getClassName();

			boolean appChanged = !appName.equals(lastApp);
			boolean classChanged = !className.equals(lastClass);

			if (classChanged || appChanged) {
				Log.d(TAG, "" + appName + " " + className);
			}
			onObserve(appName, className);

			lastClass = className;
			lastApp = appName;
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
			// the app that's currently in front
			LockInfo app = getLockInfoByPackageName(appName);
			if (className.equals(LOCKER_CLASS)) {
				// we don't want to show the locker if the locker is
				// currently
				// visible
				return;
			}
			// Only if we're monitoring the app
			if (app != null) {
				// lock app if we should
				if (app.locked) {
					// update last known class name
					// TODO this was added for whatsapp bug, but can be handy
					app.className = className;
					Log.v(TAG,
							"Show locker for " + app.packageName
									+ app.hashCode());
					showLocker(app);
				}
			}
			// lock all other apps because they're not in front anymore
			relock(appName);
		}
	}

	/**
	 * Locks ALL apps (Useful when screen is turned off)
	 */
	private void relockAll() {
		for (LockInfo li : trackedApps) {
			if (li.locked == false) {
				Log.v(TAG, "relockAll() " + li.packageName);
				li.locked = true;

			}
		}
	}

	/**
	 * Locks all apps except the one matching the provided string.
	 * 
	 * @param appName
	 *            The app that must NOT be locked.
	 */
	private void relock(String appName) {
		// TODO timing
		for (LockInfo li : trackedApps) {
			if (!li.packageName.equals(appName)) {
				if (li.locked == false) {
					Log.v(TAG, "relock() " + li.packageName);
					li.locked = true;
				}
			}
		}
	}

	// private void printLockInfos() {
	// Log.d(TAG, "------------");
	// for (LockInfo li : trackedApps) {
	// Log.v(TAG, li.locked + "\t" + li);
	// }
	// Log.d(TAG, "------------");
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "onDestroy");
		// IMPORTANT
		// The executor will happily run if it's not shutdown explicitly
		stopScheduler();

		am = null;
		trackedApps = null;
		unregisterReceiver(mScreenStatusReceiver);
	}

	private LockInfo getLockInfoByPackageName(String packageName) {
		if (trackedApps == null)
			Log.wtf(TAG, "lockList = null");
		for (LockInfo li : trackedApps) {
			if (li.packageName.equals(packageName)) {
				return li;
			}
		}
		return null;
	}

	// public void doLock(String appName) {
	// LockInfo li = getLockInfoByPackageName(appName);
	// if (li != null) {
	// if (li.lock == false) {
	// Log.i(TAG, "Locked in list " + li.packageName);
	// }
	// li.lock = true;
	// return;
	// }
	// Log.w(TAG, "Not locked " + appName + ": not in list.");
	// }

	public void doUnlock(String appName) {
		Log.d(TAG, "doUnlock called");
		LockInfo li = getLockInfoByPackageName(appName);
		if (li != null) {
			if (li.locked == true) {
				Log.i(TAG, "Unlocked in list: " + li.packageName);
			} else {
				Log.w(TAG, "Tried to unlock " + li.hashCode()
						+ " but was not locked");
				// printLockInfos();
			}
			li.locked = false;
			return;
		}
		Log.w(TAG, "Not unlocked " + appName + ": not in list.");
	}

	/**
	 * Locks the current app
	 */
	private void showLocker(LockInfo lockInfo) {
		if (mPassword.length() == 0) {
			Log.w(TAG, "Not showing lock for empty password:"
					+ lockInfo.packageName);
			return;
		}
		Log.d(TAG, "Service " + hashCode() + " Starting locker for "
				+ lockInfo.packageName);
		// TODO Remove this if whatsapp fixes their bug
		whatsappWorkaround(lockInfo);
		Intent intent = new Intent(ObserverService.this, LockActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(EXTRA_TARGET_PACKAGENAME, lockInfo.packageName);
		startActivity(intent);
	}

	/**
	 * BugFix for Whatsapp's lose focus.
	 * 
	 * @param li
	 */
	private void whatsappWorkaround(LockInfo li) {
		if (li.className.equals("com.whatsapp.Conversation")) {
			try {
				Log.i(TAG, "Sleeping 700 because whatsapp bug");
				Thread.sleep(300);
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted while whatsapp workaround");
			}
		}
	}

	/**
	 * Get password from SharedPreferences
	 * 
	 * @param c
	 * @return The password current password or an empty string.
	 */
	public static final String getPassword(Context c) {
		String pwd = c.getSharedPreferences(PREF_FILE_DEFAULT, MODE_PRIVATE)
				.getString(c.getString(R.string.pref_key_passwd), "");
		// Log.d(TAG, "getPassword:" + pwd);
		return pwd;
	}

	/**
	 * Change the lock password and write it to disk
	 * 
	 * @param c
	 * @param password
	 * @return True if success, false on failure
	 */
	public static final boolean setPassword(Context c, String password) {
		SharedPreferences.Editor editor = c.getSharedPreferences(
				PREF_FILE_DEFAULT, MODE_PRIVATE).edit();
		editor.putString(c.getString(R.string.pref_key_passwd), password);
		return editor.commit();
	}

	public static final String getMessage(Context c) {
		return c.getSharedPreferences(PREF_FILE_DEFAULT, MODE_PRIVATE)
				.getString(c.getString(R.string.pref_key_lock_message),
						c.getString(R.string.locker_footer_default));
	}

	public static final boolean setMessage(Context c, String value) {
		SharedPreferences.Editor editor = c.getSharedPreferences(
				PREF_FILE_DEFAULT, MODE_PRIVATE).edit();
		editor.putString(c.getString(R.string.pref_key_lock_message), value);
		boolean commited = editor.commit();
		return commited;
	}

	/**
	 * 
	 * @param context
	 * @return A {@link Set} that is safe to edit and use.
	 */
	public static final Set<String> getTrackedApps(Context c) {
		SharedPreferences sp = c.getSharedPreferences(PREF_FILE_APPS,
				Context.MODE_PRIVATE);
		Set<String> apps = new HashSet<String>(sp.getAll().keySet());
		return apps;
	}

	/**
	 * Tracks or untracks an app
	 * 
	 * @param packageName
	 * @param shouldTrack
	 *            True if the new state will be tracking, false if not
	 */
	public final void setTracking(String packageName, boolean shouldTrack) {
		SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE_APPS,
				Context.MODE_PRIVATE).edit();
		if (shouldTrack) {
			editor.putBoolean(packageName, true);
		} else {
			editor.remove(packageName);
		}
		boolean commited = editor.commit();
		if (!commited) {
			Log.w(TAG, "Not commited!");
		}
		updateTrackedApps();
	}

	// TODO
	public final void updateTrackedApps() {
		Set<String> apps = getTrackedApps(this);
		trackedApps = new HashSet<LockInfo>();
		for (String s : apps) {
			trackedApps.add(new LockInfo(s));
		}

	}

}
