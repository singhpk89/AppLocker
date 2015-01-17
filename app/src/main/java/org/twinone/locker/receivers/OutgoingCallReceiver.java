package org.twinone.locker.receivers;

import org.twinone.locker.lock.LockService;
import org.twinone.locker.ui.MainActivity;
import org.twinone.locker.util.PrefUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twinone.locker.R;

public class OutgoingCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.d("OutgoingCallReceiver", "Outgoing call recevied");

		PrefUtils prefs = new PrefUtils(c);
		boolean launch = prefs.getBoolean(R.string.pref_key_dial_launch,
				R.bool.pref_def_dial_launch);
		String number = i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		String launchNumber = prefs.getString(
				R.string.pref_key_dial_launch_number,
				R.string.pref_def_dial_launch_number);
		String recovery = prefs.getString(R.string.pref_key_recovery_code);

		if (launch && number.equals(launchNumber)) {
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
