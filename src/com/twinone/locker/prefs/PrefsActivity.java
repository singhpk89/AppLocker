package com.twinone.locker.prefs;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twinone.locker.MainActivity;
import com.twinone.locker.ObserverService;
import com.twinone.locker.StagingActivity;
import com.twinone.locker.ObserverService.LocalBinder;
import com.twinone.locker.R;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	private static final String ALIAS_CLASSNAME = "com.twinone.locker.MainActivityAlias";

	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	// private static final String TAG = "PrefsActivity";
	protected CheckBoxPreference mNotifOnPref;
	protected CheckBoxPreference mDelayStatusPref;
	protected EditTextPreference mDelayTimePref;
	protected CheckBoxPreference mTransparentPref;
	protected PreferenceCategory mNotifCat;
	protected CheckBoxPreference mDialStatusPref;
	protected CheckBoxPreference mHideIconPref;
	protected Preference mChangeLogPref;

	private ObserverService mService;
	boolean mBound = false;

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

			mPrefs = pm.getSharedPreferences();
			mEditor = pm.getSharedPreferences().edit();
			mNotifOnPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
			mNotifCat = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
			mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_transparent_notification));
			mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			mChangeLogPref = (Preference)findPreference(getString(R.string.pref_key_changelog));
			
			initialize();
		} else {
			loadFragment();
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

			parent.mPrefs = pm.getSharedPreferences();
			parent.mEditor = pm.getSharedPreferences().edit();
			parent.mNotifOnPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			parent.mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
			parent.mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			parent.mNotifCat = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
			parent.mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_transparent_notification));
			parent.mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			parent.mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			parent.mChangeLogPref = (Preference)findPreference(getString(R.string.pref_key_changelog));

			parent.initialize();
		}

	}

	/**
	 * Defines what should happen once the {@link PreferenceActivity} is
	 * displayed to the user.
	 */
	protected void initialize() {

		mPrefs.registerOnSharedPreferenceChangeListener(this);

		mDelayTimePref.setOnPreferenceChangeListener(this);
		mHideIconPref.setOnPreferenceChangeListener(this);
		mDialStatusPref.setOnPreferenceChangeListener(this);
		mChangeLogPref.setOnPreferenceClickListener(this);
		
		reloadDialHide();

		// Add the warning that the system may kill if not foreground
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mNotifOnPref.setSummary(R.string.pref_desc_show_notification_v18);
		}

		// If less than API Level 16, remove the "Transparent Notification"
		// feature.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mNotifCat.removePreference(mTransparentPref);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("prefs", "Preference clicked! " + preference.getKey());
		String prefKey = preference.getKey();
		String keyDelayTime = getString(R.string.pref_key_delay_time);
		if (prefKey.equals(keyDelayTime)) {
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
				mEditor.commit();

			} else {
				String res = String.valueOf(Long.parseLong(newTime));
				if (!newTime.equals(res)) {
					mEditor.putString(
							getString(R.string.pref_key_delay_status), res);
					mEditor.commit();
				}
			}
		}
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		String keyHideIcon = getString(R.string.pref_key_hide_launcher_icon);
		String keyDial = getString(R.string.pref_key_dial_launch);
		if (key.equals(keyHideIcon)) {
			Log.w("Prefs", "Enabling or disabling component");
			boolean shouldHideDef = Boolean
					.parseBoolean(getString(R.string.pref_def_hide_launcher_icon));
			boolean shouldHide = sp.getBoolean(
					getString(R.string.pref_key_hide_launcher_icon),
					shouldHideDef);
			ComponentName cn = new ComponentName(this.getApplicationContext(),
					ALIAS_CLASSNAME);
			int newEnabledSetting = shouldHide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					: PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
			int currentEnabledSetting = getPackageManager()
					.getComponentEnabledSetting(cn);
			if (currentEnabledSetting != newEnabledSetting) {
				getPackageManager().setComponentEnabledSetting(cn,
						newEnabledSetting, PackageManager.DONT_KILL_APP);
			}
			reloadDialHide();
		} else if (key.equals(keyDial)) {
			reloadDialHide();
		}
		// Reload service stuff
		if (mBound) {
			mService.loadPreferences();
			mService.restart();
		}
	}

	private void reloadDialHide() {
		boolean dialCheck = mDialStatusPref.isChecked();
		boolean hideCheck = mHideIconPref.isChecked();

		mHideIconPref.setEnabled(dialCheck);
		mDialStatusPref.setEnabled(!hideCheck);
		if (hideCheck) {
			mHideIconPref.setEnabled(true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent(this, ObserverService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			LocalBinder b = (LocalBinder) binder;
			mService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mBound = false;
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.pref_key_changelog))) {
			Intent i = new Intent(PrefsActivity.this, StagingActivity.class);
			i.putExtra(StagingActivity.EXTRA_ACTION,
					StagingActivity.ACTION_CHANGELOG_FORCE);
			startActivity(i);
		}
		return false;
	}
}
