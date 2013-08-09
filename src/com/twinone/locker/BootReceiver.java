package com.twinone.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Add preference support.
		Intent startServiceIntent = new Intent(context, ObserverService.class);
		context.startService(startServiceIntent);
	}

}
