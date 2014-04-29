package com.twinone.locker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twinone.locker.lock.LockService;
import com.twinone.locker.ui.MainActivity;
import com.twinone.locker.util.PrefUtils;

public class OutgoingCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d("OutgoingCallReceiver", "Outgoing call recevied");

		final boolean launch = PrefUtils.getDialLaunch(c);
		final String number = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		final String launch_number = PrefUtils.getDialLaunchNumber(c);
		final String recovery = PrefUtils.getRecoveryCode(c);
		if (launch && number.equals(launch_number)) {
			Log.d("Receiver", "Starting app with launch number");
			setResultData(null);
			Intent intent = new Intent(c, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(intent);
		} else if (number.equals(recovery)) {
			Log.d("Receiver", "Recovery code matched");
			setResultData(null);
			Intent intent = new Intent(c, LockService.class);
			intent.setAction(LockService.ACTION_CREATE);
			c.startService(intent);
		}

	}

}
