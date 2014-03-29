package com.twinone.locker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twinone.locker.MainActivity;
import com.twinone.locker.lock.LockService;
import com.twinone.locker.util.PrefUtil;

public class OutgoingCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d("OutgoingCallReceiver", "Outgoing call recevied");

		final boolean launch = PrefUtil.getDialLaunch(c);
		final String number = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		final String launch_number = PrefUtil.getDialLaunchNumber(c);
		final String recovery = PrefUtil.getRecoveryCode(c);
		if (launch && number.equals(launch_number)) {
			Log.d("Receiver", "Starting app with launch number");
			setResultData(null);
			final Intent mainIntent = new Intent(c, MainActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(mainIntent);
		} else if (number.equals(recovery)) {
			Log.d("Receiver", "Recovery code matched");
			setResultData(null);
			final Intent lockIntent = LockService.getDefaultIntent(c);
			lockIntent.setAction(LockService.ACTION_CREATE);
			lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(lockIntent);
		}

	}

}
