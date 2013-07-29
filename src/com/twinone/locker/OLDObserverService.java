package com.twinone.locker;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class OLDObserverService extends Service {

	public static final String EXTRA_APPINFO = "com.twinone.locker.extraInfo";
	public static final String TAG = "Observer";
	public static final String PREF_PASSWD_KEY = "com.twinone.locker.pref.passwd";
	public static final String PREF_PASSWD_DEF = "";
	private static final String LOCKER_CLASS = AppLockActivity.class.getName();
	// avoid too large delay, because we could miss a package change if the user
	// goes forth and back very quickly
	private static final long DELAY = 200;

	private String mPassword;
	private String lastApp = "";
	private String lastClass = "";
	// private String lastClass = "";

	private ActivityManager am;
	private ScheduledExecutorService mExecutor;

	/** In this map information about apps will be */
	private ArrayList<LockInfo> lockList;

	@Override
	public IBinder onBind(Intent i) {
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public OLDObserverService getInstance() {
			return OLDObserverService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Load preferences
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mPassword = getPassword(this);
		lockList = new ArrayList<LockInfo>();

		// Add whatsapp for testing
		lockList.add(new LockInfo("com.whatsapp", mPassword));
		lockList.add(new LockInfo(getApplicationInfo().packageName, mPassword));
		// empty notification hack for a foreground service without notification
		@SuppressWarnings("deprecation")
		Notification n = new Notification(0, null, System.currentTimeMillis());
		n.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(42, n);

		// Start the monitoring
		if (mExecutor != null) {
			mExecutor.shutdown();
		}
		mExecutor = Executors.newSingleThreadScheduledExecutor();
		mExecutor.scheduleWithFixedDelay(new MyMonitor(), 0, DELAY,
				TimeUnit.MILLISECONDS);

		return START_STICKY;
	}

	/**
	 * Get password from SharedPreferences
	 * 
	 * @param c
	 * @return
	 */
	public static final String getPassword(Context c) {
		return c.getSharedPreferences("default", MODE_PRIVATE).getString(
				PREF_PASSWD_KEY, PREF_PASSWD_DEF);
	}

	/**
	 * Change the lock password and write it to disk
	 * 
	 * @param c
	 * @param password
	 * @return True if success, false on failure
	 */
	public static final boolean setPassword(Context c, String password) {
		SharedPreferences.Editor editor = c.getSharedPreferences("default",
				MODE_PRIVATE).edit();
		editor.putString(OLDObserverService.PREF_PASSWD_KEY, password);
		return editor.commit();
	}

	private class MyMonitor extends Thread {
		@Override
		public void run() {
			ComponentName app = am.getRunningTasks(1).get(0).topActivity;
			String appName = app.getPackageName();
			String className = app.getClassName();

			boolean appChanged = !appName.equals(lastApp);
			boolean classChanged = !className.equals(lastClass);
			boolean locked = false;

			if (appChanged) {
				Log.v(TAG, "APP " + className);

				// LockInfo lockInfo = getLockInfoByPackageName(appName);
				// Lock the new app

				attemptLock(appName, className);
				// Relock all apps except currently on top
				for (LockInfo li : lockList) {
					if (!li.packageName.equals(appName)) {
						if (li.lock == false) {
							Log.i(TAG, "Relocked in list: " + li.packageName);
							li.lock = true;

						}
					}
				}
			} else if (classChanged) {
				Log.v(TAG, "CLS " + className);
				// Class changed inside one app
				// If we're in our app but not at the locker itself, try to
				// lock
				if (appName.equals(getApplicationInfo().packageName)
						&& !className.equals(LOCKER_CLASS) && !locked) {
					boolean lockedByClass = attemptLock(appName, className);
					if (lockedByClass) {
						// probably this means that the user switched in less
						// than DELAY
						Log.w(TAG, "Locked own app by class");
					}
				}
			}
			lastClass = className;
			lastApp = appName;
		}

		/**
		 * Attempts to lock an application if it should be locked.
		 * 
		 * @return True if the app was locked
		 */
		private boolean attemptLock(String appName, String className) {
			LockInfo lockInfo = getLockInfoByPackageName(appName);
			// Lock the new app
			// We can't lock the locker
			if (lockInfo != null && lockInfo.lock
					&& !className.equals(LOCKER_CLASS)) {
				lockInfo.className = className;
				// Log.i(TAG, "Lock triggered");
				showLocker(lockInfo);
				return true;
			}
			return false;
		}
	}

	/**
	 * What happens when a package changes
	 * 
	 * @param appName
	 * @param className
	 * @return Whether to update the lastApp and lastClass values.
	 */
	public void onPackageChanged(String appName, String className) {
		// lookup the app in the matcher and if its locked launch the locker
		// activity.

		// lock packages different than this package

	}

	private LockInfo getLockInfoByPackageName(String packageName) {
		for (LockInfo li : lockList) {
			if (li.packageName.equals(packageName)) {
				return li;
			}
		}
		return null;
	}

	public void doLock(String appName) {
		LockInfo li = getLockInfoByPackageName(appName);
		if (li != null) {
			if (li.lock == false) {
				Log.i(TAG, "Locked in list " + li.packageName);
			}
			li.lock = true;
			return;
		}
		Log.w(TAG, "Not locked " + appName + ": not in list.");
	}

	public void doUnlock(String appName) {
		LockInfo li = getLockInfoByPackageName(appName);
		if (li != null) {
			// if (li.lock == true) {
			// // Log.i(TAG, "Unlocked in list: " + li.packageName);
			// }
			li.lock = false;
			return;
		}
		Log.w(TAG, "Not unlocked " + appName + ": not in list.");
	}

	/**
	 * Locks the current app
	 */
	private void showLocker(LockInfo lockInfo) {
		if (mPassword.isEmpty()) {
			Log.w(TAG, "Not showing lock for empty password:"
					+ lockInfo.packageName);
			return;
		}
		// Log.d(TAG, "Starting locker for " + lockInfo.packageName);
		Intent intent = new Intent(OLDObserverService.this, AppLockActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(EXTRA_APPINFO, lockInfo);
		startActivity(intent);
	}

}
