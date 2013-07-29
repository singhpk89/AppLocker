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

public class ObserverService extends Service {

	public static final String EXTRA_APPINFO = "com.twinone.locker.extraInfo";
	public static final String TAG = "Observer";
	public static final String PREF_PASSWD_KEY = "com.twinone.locker.pref.passwd";
	public static final String PREF_PASSWD_DEF = "";
	private static final String LOCKER_CLASS = AppLockActivity.class.getName();
	// avoid too large delay, because we could miss a package change if the user
	// goes forth and back very quickly
	private long DELAY = DELAY_NORMAL;

	//
	private static final long DELAY_PERFORMANCE = 50;
	private static final long DELAY_FAST = 75;
	private static final long DELAY_NORMAL = 100;
	private static final long DELAY_SLOW = 150;
	private static final long DELAY_POWERSAVE = 200;

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
		public ObserverService getInstance() {
			return ObserverService.this;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.w(TAG, "onCreate");

		// TODO Load preferences
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mPassword = getPassword(this);
		lockList = new ArrayList<LockInfo>();

		// Add whatsapp for testing
		lockList.add(new LockInfo("com.whatsapp", mPassword));
		lockList.add(new LockInfo("com.twitter.android", mPassword));
		// NEW: Own app will call the locker itself, no need to check in
		// service.
		// This provides a way to disable the service and still lock own app.
		// // Don't lock own app because it's the first time and the password
		// has
		// // just been set.
		lockList.add(new LockInfo(getApplicationInfo().packageName, mPassword)
				.setLock(false));
		// empty notification hack for a foreground service without notification
		@SuppressWarnings("deprecation")
		Notification n = new Notification(0, null, System.currentTimeMillis());
		n.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(42, n);

		// Start the monitoring
		mExecutor = Executors.newSingleThreadScheduledExecutor();
		mExecutor.scheduleWithFixedDelay(new MyMonitor(), 0, DELAY,
				TimeUnit.MILLISECONDS);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
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
		editor.putString(ObserverService.PREF_PASSWD_KEY, password);
		return editor.commit();
	}

	private class MyMonitor extends Thread {
		@Override
		public void run() {
			ComponentName app = am.getRunningTasks(1).get(0).topActivity;
			String appName = app.getPackageName();
			String className = app.getClassName();

			// boolean appChanged = !appName.equals(lastApp);
			// boolean classChanged = !className.equals(lastClass);

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
				if (app.lock) {
					printLockInfos();
					Log.v(TAG,
							"Show locker for " + app.packageName
									+ app.hashCode());
					showLocker(app);
				}
			}
			for (LockInfo li : lockList) {
				// lock all other apps because they're not in front anymore
				if (!li.packageName.equals(appName)) {
					if (li.lock == false) {
						Log.v(TAG, "Relocking " + li.packageName);
						li.lock = true;
					}
				}
			}
		}
	}

	private void printLockInfos() {
		Log.d(TAG, "------------");
		for (LockInfo li : lockList) {
			Log.v(TAG, li.lock + "\t" + li);
		}
		Log.d(TAG, "------------");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "onDestroy");
		// IMPORTANT
		// The executor will happily run if it's not shutdown explicitly
		mExecutor.shutdownNow();
		mExecutor = null;
		am = null;
		lockList = null;
	}

	private LockInfo getLockInfoByPackageName(String packageName) {
		if (lockList == null)
			Log.wtf(TAG, "lockList = null");
		for (LockInfo li : lockList) {
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
			if (li.lock == true) {
				Log.i(TAG, "Unlocked in list: " + li.packageName);
			} else {
				Log.w(TAG, "Tried to unlock " + li.hashCode()
						+ " but was not locked");
			}
			li.lock = false;
			printLockInfos();
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
		Intent intent = new Intent(ObserverService.this, AppLockActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(EXTRA_APPINFO, lockInfo);
		startActivity(intent);
	}

}
