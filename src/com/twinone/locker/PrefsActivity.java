package com.twinone.locker;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private static final String TAG = "PrefsActivity";
	protected CheckBoxPreference mNotifPref;

	// private ObserverService mService;

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

			mNotifPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));

		} else {
			loadFragment();
		}
	}

	protected void initialize() {
		mNotifPref.setOnPreferenceChangeListener(this);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mNotifPref.setSummary(R.string.pref_desc_show_notification_v18);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void loadFragment() {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PrefsFragment extends PreferenceFragment {

		private PrefsActivity parent;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			parent = (PrefsActivity) getActivity();
			PreferenceManager pm = getPreferenceManager();
			pm.setSharedPreferencesName(ObserverService.PREF_FILE_DEFAULT);
			pm.setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.prefs);

			parent.mNotifPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			parent.initialize();
		}

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(TAG, "Preference clicked! " + preference.getKey());
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
