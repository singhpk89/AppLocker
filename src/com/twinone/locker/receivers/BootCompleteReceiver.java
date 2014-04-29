package com.twinone.locker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.util.PrefUtils;

/**
 * Starts the Service at boot if it's specified in the preferences.
 * 
 * @author twinone
 * 
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d("BootCompleteReceiver", "bootcomplete recevied");

		boolean startAtBoot = PrefUtils.getStartAtBoot(c);
		if (startAtBoot) {
			Log.d("BootCompleteReceiver", "Starting service");
			AppLockService.start(c);
		}
	}

}
