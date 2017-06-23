package org.twinone.locker.version;

import java.util.HashMap;
import java.util.Map;

import org.twinone.locker.util.PrefUtils;
import org.twinone.util.Analytics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;


/**
 * Receives boot and alarm events for {@link VersionManager}
 * 
 * @author twinone
 * 
 */
public class Receiver extends BroadcastReceiver {

	private static final String ACTION_QUERY_SERVER = "com.twinone.locker.version.intent.action.query_server";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_QUERY_SERVER.equals(intent.getAction())) {
			onAlarmReceived(context);
		} else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			scheduleAlarm(context);
		}
	}

	private static void scheduleAlarm(Context c) {
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
	}

}
