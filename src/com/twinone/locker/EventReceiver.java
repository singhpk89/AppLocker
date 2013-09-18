package com.twinone.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.util.PrefUtil;

public class EventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.w("", "Received: " + i.getAction());
		String a = i.getAction();
		SharedPreferences sp = PrefUtil.prefs(c);
		if (Intent.ACTION_BOOT_COMPLETED.equals(a)) {
			boolean startAtBoot = sp.getBoolean(
					c.getString(R.string.pref_key_start_boot), true);
			if (startAtBoot) {
				Intent startServiceIntent = new Intent(c, AppLockService.class);
				c.startService(startServiceIntent);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(a)) {
			boolean dialLaunchDef = Boolean.parseBoolean(c
					.getString(R.string.pref_def_dial_launch));
			boolean dialLaunch = sp.getBoolean(
					c.getString(R.string.pref_key_dial_launch), dialLaunchDef);
			if (dialLaunch
					&& i.getStringExtra(Intent.EXTRA_PHONE_NUMBER).equals(
							"#" + PrefUtil.getPassword(PrefUtil.prefs(c), c))) {
				// TODO very important change to custom number because we've
				// implemented a pattern now.
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

		else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(a)) {
			processConnectivityEvent(i, c);
		}
	}

	private static final void processConnectivityEvent(Intent intent, Context c) {
		boolean disconnected = intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (!disconnected) {
			ConnectivityManager cm = (ConnectivityManager) c
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null && ni.isConnected()) {
				switch (ni.getType()) {
				case ConnectivityManager.TYPE_WIFI:
					WifiManager wm = (WifiManager) c
							.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wi = wm.getConnectionInfo();
					Log.d("RECEIVER", "SSID: " + wi.getSSID());
					break;
				}
			}
		}
	}

}
