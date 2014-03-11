package com.twinone.locker.receivers;

import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.util.PrefUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Starts the Service at boot if it's specified in the preferences.
 * @author twinone
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d("BootCompleteReceiver", "bootcomplete recevied");

		boolean startAtBoot = PrefUtil.getStartAtBoot(c);
		if (startAtBoot) {
			Log.d("BootCompleteReceiver", "Starting service");
			Intent startIntent = AppLockService.getStartIntent(c);
			c.startService(startIntent);
		}
	}

}
