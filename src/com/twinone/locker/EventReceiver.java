package com.twinone.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		String a = i.getAction();
		SharedPreferences sp = c.getSharedPreferences(
				ObserverService.PREF_FILE_DEFAULT, Context.MODE_PRIVATE);
		if (Intent.ACTION_BOOT_COMPLETED.equals(a)) {
			boolean startAtBoot = sp.getBoolean(
					c.getString(R.string.pref_key_start_boot), true);
			if (startAtBoot) {
				Intent startServiceIntent = new Intent(c, ObserverService.class);
				c.startService(startServiceIntent);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(a)) {
			boolean dialLaunchDef = Boolean.parseBoolean(c
					.getString(R.string.pref_def_dial_launch));
			boolean dialLaunch = sp.getBoolean(
					c.getString(R.string.pref_key_dial_launch), dialLaunchDef);
			if (dialLaunch
					&& i.getStringExtra(Intent.EXTRA_PHONE_NUMBER).equals(
							"#" + ObserverService.getPassword(c))) {
				Log.d("Receiver", "OUTGOING CALL MATCHED");
				setResultData(null);
				// MainActivity.showWithoutPassword(c);
				Intent main = new Intent(c, MainActivity.class);
				main.putExtra(MainActivity.EXTRA_UNLOCKED, true);
				main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				c.startActivity(main);
			}
		}

		// else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(a)) {
		// ConnectivityManager cm = (ConnectivityManager) c
		// .getSystemService(Context.CONNECTIVITY_SERVICE);
		// WifiManager wm = (WifiManager) c
		// .getSystemService(Context.WIFI_SERVICE);
		// NetworkInfo ni = cm.getActiveNetworkInfo();
		// if (ni != null && ni.isConnected()) {
		// switch (ni.getType()) {
		// case ConnectivityManager.TYPE_WIFI:
		// WifiInfo wi = wm.getConnectionInfo();
		// Log.d("RECEIVER", "SSID: " + wi.getSSID());
		// break;
		// }
		// }
		// }
	}

}
