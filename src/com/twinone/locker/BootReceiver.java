package com.twinone.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = context.getSharedPreferences(
				ObserverService.PREF_FILE_DEFAULT, Context.MODE_PRIVATE);
		boolean startAtBoot = sp.getBoolean(
				context.getString(R.string.pref_key_start_boot), true);

		if (startAtBoot) {
			Intent startServiceIntent = new Intent(context,
					ObserverService.class);
			context.startService(startServiceIntent);
		}
	}

}
