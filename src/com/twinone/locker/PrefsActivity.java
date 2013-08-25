package com.twinone.locker;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	SharedPreferences.Editor mEditor;
	// private static final String TAG = "PrefsActivity";
	protected CheckBoxPreference mNotifPref;
	protected CheckBoxPreference mDelayStatusPref;
	protected EditTextPreference mDelayTimePref;

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
			mEditor = pm.getSharedPreferences().edit();
			mNotifPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));

		} else {
			loadFragment();
		}
	}

	protected void initialize() {
		mDelayTimePref.setOnPreferenceChangeListener(this);
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
			parent.mEditor = pm.getSharedPreferences().edit();

			parent.mNotifPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			parent.mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
			parent.mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			parent.initialize();
		}

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// Log.d(TAG, "Preference clicked! " + preference.getKey());
		String prefKey = preference.getKey();
		String delayTimeKey = getString(R.string.pref_key_delay_time);
		if (prefKey.equals(delayTimeKey)) {
			String newTime = (String) newValue;
			boolean isZero = (newTime.length() == 0);
			try {
				isZero = (Long.parseLong(newTime) == 0);
				// TODO Remove leading zeros
			} catch (NumberFormatException e) {
				isZero = true;
			}
			if (isZero) {
				mDelayStatusPref.setChecked(false);
				mEditor.putBoolean(getString(R.string.pref_key_delay_status),
						false);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
					mEditor.commit();
				} else {
					mEditor.apply();
				}
			}
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
