package com.twinone.locker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("", "INTENT RECEIVED: " + intent.getAction());
		// processConnectivityEvent(intent, context);
	}

	// private String unescapeSsid(String ssid) {
	// if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
	// ssid = ssid.substring(1, ssid.length() - 1);
	// }
	// return ssid;
	// }

	// Old method, do not use.

	// private static final void processConnectivityEvent(Intent intent, Context
	// c) {
	// String a = WifiManager.NETWORK_STATE_CHANGED_ACTION;
	// SharedPreferences sp = c.getSharedPreferences("beta",
	// Context.MODE_PRIVATE);
	// // TODO bug here
	// final boolean should = sp.getBoolean(
	// c.getString(R.string.pref_key_temp_wifi_unlock_state), false);
	// if (!should)
	// return;
	//
	// String savedSSID = sp.getString(
	// c.getString(R.string.pref_key_temp_wifi_ssid), "");
	// NetworkInfo ni = (NetworkInfo) intent
	// .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	//
	// if (ni.isConnected()) {
	// switch (ni.getType()) {
	// case ConnectivityManager.TYPE_WIFI:
	// WifiManager wm = (WifiManager) c
	// .getSystemService(Context.WIFI_SERVICE);
	// WifiInfo wi = wm.getConnectionInfo();
	// String ssid = wi.getSSID();
	//
	// Log.d("RECEIVER", "SSID: " + ssid + " saved: " + savedSSID);
	// if (ssid != null && ssid.length() != 0) {
	// // 4.2+ have quotes, remove them if it has!
	// if (ssid.equals(savedSSID)) {
	// Log.d("RECEIVER", "Stopping service, ssid matched");
	// Intent i = new Intent(c, AppLockService.class);
	// c.stopService(i);
	// }
	// }
	// break;
	// }
	// }
	// }

}
