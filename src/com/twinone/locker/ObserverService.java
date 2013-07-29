package com.twinone.locker;

import java.util.ArrayList;
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
import android.os.IBinder;
import android.util.Log;

public class ObserverService extends Service {

	public static final String EXTRA_APPINFO = "com.twinone.locker.extraInfo";
	public static final String TAG = "Observer";
	public static final String PREF_PASSWD_KEY = "com.twinone.locker.pref.passwd";
	public static final String PREF_PASSWD_DEF = "";
	private static final String LOCKER_CLASS = LockActivity.class.getName();

	private long DELAY = 150;

	//
	// private static final long DELAY_PERFORMANCE = 50;
	// private static final long DELAY_FAST = 75;
	// private static final long DELAY_NORMAL = 100;
	// private static final long DELAY_SLOW = 150;
	// private static final long DELAY_POWERSAVE = 200;

	private String mPassword;
	// private String lastApp = "";
	// private String lastClass = "";

	private ActivityManager am;
	private ScheduledExecutorService mExecutor;
	BroadcastReceiver mBroadcastReceiver;

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
		super.onCreate();
		Log.w(TAG, "onCreate");

		// TODO Load applications from preferences
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		mPassword = getPassword(this);
		lockList = new ArrayList<LockInfo>();

		// Testing apps
		// -------------------------------

		lockList.add(new LockInfo("com.whatsapp", mPassword));
		lockList.add(new LockInfo("com.twitter.android", mPassword));
		// lockList.add(new LockInfo(getApplicationInfo().packageName,
		// mPassword)
		// .setLock(false));

		// ------------------------------

		// Set an empty notification
		@SuppressWarnings("deprecation")
		Notification n = new Notification(0, null, System.currentTimeMillis());
		n.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(42, n);

		// Start the monitoring
		mExecutor = Executors.newSingleThreadScheduledExecutor();
		mExecutor.scheduleWithFixedDelay(new ActivePackageMonitor(), 0, DELAY,
				TimeUnit.MILLISECONDS);

		mBroadcastReceiver = new ScreenReceiver();
		registerReceiver(mBroadcastReceiver, new IntentFilter(
				Intent.ACTION_SCREEN_ON));

	}

	class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.i("TAG", "Screen ON");
				relockAll();
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.i("TAG", "Screen OFF");
				relockAll();
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	/**
	 * Get password from SharedPreferences
	 * 
	 * @param c
	 * @return The password current password or an empty string.
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

	private class ActivePackageMonitor extends Thread {
		@Override
		public void run() {
			ComponentName app = am.getRunningTasks(1).get(0).topActivity;
			String appName = app.getPackageName();
			String className = app.getClassName();

			// boolean appChanged = !appName.equals(lastApp);
			// boolean classChanged = !className.equals(lastClass);

			// if (classChanged || appChanged)
			onObserve(appName, className);

			// lastClass = className;
			// lastApp = appName;
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
				if (app.lock) {
					// printLockInfos();
					// TODO FIXME Temporary test: Unlock the app here and relock
					// it in the LockActivity to avoid doubling lockactivity
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
		for (LockInfo li : lockList) {
			if (li.lock == false) {
				Log.v(TAG, "relockAll() " + li.packageName);
				li.lock = true;

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
		for (LockInfo li : lockList) {
			if (!li.packageName.equals(appName)) {
				if (li.lock == false) {
					Log.v(TAG, "relock() " + li.packageName);
					li.lock = true;
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
				printLockInfos();
			}
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
		Intent intent = new Intent(ObserverService.this, LockActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(EXTRA_APPINFO, lockInfo);
		startActivity(intent);
	}

}
