package com.twinone.locker.version;

import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.twinone.locker.LockerAnalytics;
import com.twinone.locker.pro.ProUtils;
import com.twinone.locker.util.PrefUtil;
import com.twinone.util.Analytics;

/**
 * Receives boot and alarm events for {@link VersionManager}
 * 
 * @author twinone
 * 
 */
public class Receiver extends BroadcastReceiver {

	public static final String ACTION_QUERY_SERVER = "com.twinone.locker.version.intent.action.query_server";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_QUERY_SERVER.equals(intent.getAction())) {
			onAlarmReceived(context);
		} else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			scheduleAlarm(context);
		}
	}

	public static void scheduleAlarm(Context c) {
		Log.d("", "Scheduling alarm");
		Intent i = new Intent(c, Receiver.class);
		i.setAction(ACTION_QUERY_SERVER);

		PendingIntent pi = PendingIntent.getBroadcast(c, 0, i, 0);

		// schedule an alarm for in 5 seconds, that repeats every 12 hours
		AlarmManager am = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		long startTime = SystemClock.elapsedRealtime();
		long interval = AlarmManager.INTERVAL_HALF_DAY;
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime,
				interval, pi);
	}

	private void onAlarmReceived(final Context c) {
		Log.d("", "Querying from alarm");
		new VersionManager(c).queryServer(null);

		// analytics
		Analytics analytics = new Analytics(c);
		ProUtils proUtils = new ProUtils(c);
		Map<String, String> data = analytics.getAll();
		data.put(LockerAnalytics.PRO_TYPE, proUtils.getProTypeString());
		data.put(LockerAnalytics.LOCKED_APPS_COUNT,
				String.valueOf(PrefUtil.getLockedApps(c).size()));
		analytics.queryServer(data);

	}

}
