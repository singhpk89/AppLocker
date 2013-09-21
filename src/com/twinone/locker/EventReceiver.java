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
import com.twinone.locker.lock.LockActivity;
import com.twinone.locker.util.PrefUtil;

public class EventReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		Log.w("", "Received: " + i.getAction());
		String a = i.getAction();
		// SharedPreferences sp = PrefUtil.prefs(c);
		if (Intent.ACTION_BOOT_COMPLETED.equals(a)) {
			if (PrefUtil.getStartAtBoot(c)) {
				Intent startServiceIntent = new Intent(c, AppLockService.class);
				c.startService(startServiceIntent);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(a)) {
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
				final Intent lockIntent = LockActivity.getDefaultIntent(c);
				lockIntent.setAction(LockActivity.ACTION_CREATE);
				lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				c.startActivity(lockIntent);
			}
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(a)) {
			processConnectivityEvent(i, c);
		}
	}

	private static final void processConnectivityEvent(Intent intent, Context c) {
		SharedPreferences sp = c.getSharedPreferences("beta",
				Context.MODE_PRIVATE);
		final boolean should = sp.getBoolean(
				c.getString(R.string.pref_key_temp_wifi_unlock_state), false);
		if (!should)
			return;
		String savedSSID = sp.getString(
				c.getString(R.string.pref_key_temp_wifi_ssid), "");
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
					String ssid = wi.getSSID();
					Log.d("RECEIVER", "SSID: " + ssid + " saved: " + savedSSID);
					if (ssid != null && ssid.length() != 0) {
						// 4.2+ have quotes, remove them if it has!
						if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
							ssid = ssid.substring(1, ssid.length() - 1);
						}
						if (savedSSID.startsWith("\"")
								&& savedSSID.endsWith("\"")) {
							savedSSID = savedSSID.substring(1,
									savedSSID.length() - 1);
						}
						if (ssid.equals(savedSSID)) {
							Log.d("RECEIVER", "Stopping service, ssid matched");
							Intent i = new Intent(c, AppLockService.class);
							c.stopService(i);
						}
					}
					break;
				}
			}
		}
	}

}
