package com.twinone.locker.automation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.twinone.locker.util.PrefUtils;


public class RulesActivity extends Activity {
	
	public static final String ACTION_ON = "com.twinone.locker.rules.action.ON";
	public static final String ACTION_OFF = "com.twinone.locker.rules.action.OFF";
	public static final String TRIGGER_WIFI_ON = "com.twinone.locker.rules.trigger_wifi_on";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public final void addRule(String condition, String action) {
		SharedPreferences.Editor editor = PrefUtils.appsPrefs(this).edit();
		// for (String packageName : packageNames) {
		// if (shouldTrack) {
		// editor.putBoolean(packageName, true);
		// } else {
		// editor.remove(packageName);
		// }
		// }
		// boolean commited = editor.commit();
		// Log.d(TAG, "Editor.commit: " + commited);
		// if (!commited) {
		// Log.w(TAG, "Not commited!");
		// }
	}
	
}
