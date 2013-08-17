package com.twinone.locker;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Workaround for older versions
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			PreferenceManager pm = getPreferenceManager();
			pm.setSharedPreferencesName(ObserverService.PREF_FILE_DEFAULT);
			pm.setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.prefs);
		} else {
			loadFragment();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void loadFragment() {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager pm = getPreferenceManager();
			pm.setSharedPreferencesName(ObserverService.PREF_FILE_DEFAULT);
			pm.setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.prefs);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
