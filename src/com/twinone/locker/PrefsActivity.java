package com.twinone.locker;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockActivity;
import com.twinone.locker.util.PrefUtil;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnSharedPreferenceChangeListener,
		OnPreferenceClickListener {
	private static final String ALIAS_CLASSNAME = "com.twinone.locker.MainActivityAlias";

	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	// private static final String TAG = "PrefsActivity";
	protected CheckBoxPreference mNotifOnPref;
	protected CheckBoxPreference mDelayStatusPref;
	protected EditTextPreference mDelayTimePref;
	protected CheckBoxPreference mTransparentPref;
	protected PreferenceCategory mCatNotif;
	protected CheckBoxPreference mDialStatusPref;
	protected CheckBoxPreference mHideIconPref;
	protected Preference mChangeLogPref;
	protected PreferenceScreen mPrefScreen;
	protected ListPreference mLockTypePref;
	protected PreferenceCategory mCatPassword;
	protected PreferenceCategory mCatPattern;

	private Handler mHandler = new Handler();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Workaround for older versions
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			PreferenceManager pm = getPreferenceManager();
			pm.setSharedPreferencesName(PrefUtil.PREF_FILE_DEFAULT);
			pm.setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.prefs);

			mPrefs = pm.getSharedPreferences();
			mEditor = pm.getSharedPreferences().edit();
			mNotifOnPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
			mCatNotif = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
			mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_transparent_notification));
			mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
			mLockTypePref = (ListPreference) findPreference(getString(R.string.pref_key_lock_type));
			mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
			mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
			mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));

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
			pm.setSharedPreferencesName(PrefUtil.PREF_FILE_DEFAULT);
			pm.setSharedPreferencesMode(MODE_PRIVATE);
			addPreferencesFromResource(R.xml.prefs);

			parent.mPrefs = pm.getSharedPreferences();
			parent.mEditor = pm.getSharedPreferences().edit();
			parent.mNotifOnPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
			parent.mDelayTimePref = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
			parent.mDelayStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
			parent.mCatNotif = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
			parent.mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_transparent_notification));
			parent.mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			parent.mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			parent.mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
			parent.mLockTypePref = (ListPreference) findPreference(getString(R.string.pref_key_lock_type));
			parent.mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
			parent.mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
			parent.mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));

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
		// Add the warning that the system may kill if not foreground
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mNotifOnPref.setSummary(R.string.pref_desc_show_notification_v18);
		}

		// If less than API Level 16, remove the "Transparent Notification"
		// feature.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mCatNotif.removePreference(mTransparentPref);
		}
		setupMessagesAndViews();
	}

	// when the user clicks something
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

	// When the preference changed on disk
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		String keyHideIcon = getString(R.string.pref_key_hide_launcher_icon);
		// String keyDial = getString(R.string.pref_key_dial_launch);
		String keyLockType = getString(R.string.pref_key_lock_type);
		if (key.equals(keyHideIcon)) {
			Log.w("Prefs", "Enabling or disabling component");
			// boolean shouldHideDef = Boolean
			// .parseBoolean(getString(R.string.pref_def_hide_launcher_icon));
			// boolean shouldHide = sp.getBoolean(
			// getString(R.string.pref_key_hide_launcher_icon),
			// shouldHideDef);
			boolean shouldHide = mHideIconPref.isChecked();
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
		} else if (key.equals(keyLockType)) {
			if (mLockTypePref.getValue().equals(
					getString(R.string.pref_val_lock_type_password))) {
				Log.d("TAG", "password");
				Intent i = LockActivity.getDefaultIntent(this);
				i.setAction(LockActivity.ACTION_CREATE);
				i.putExtra(LockActivity.EXTRA_VIEW_TYPE,
						LockActivity.LOCK_TYPE_PASSWORD);
				startActivity(i);
			} else {
				Log.d("TAG", "pattern");
				Intent i = LockActivity.getDefaultIntent(this);
				i.setAction(LockActivity.ACTION_CREATE);
				i.putExtra(LockActivity.EXTRA_VIEW_TYPE,
						LockActivity.LOCK_TYPE_PATTERN);
				startActivity(i);
			}

		}
		// else if (key.equals(keyDial)) {
		// }
		setupMessagesAndViews();

		// Reload service stuff
		Intent intent = AppLockService.getReloadIntent(this);
		startService(intent);

	}

	private void setupMessagesAndViews() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				doSetupMessagesAndViews();
			}

			private void doSetupMessagesAndViews() {
				// Dial and hide icon
				final boolean dialCheck = mDialStatusPref.isChecked();
				final boolean hideCheck = mHideIconPref.isChecked();

				mHideIconPref.setEnabled(dialCheck);
				mDialStatusPref.setEnabled(!hideCheck);
				if (hideCheck) {
					mHideIconPref.setEnabled(true);
				}
				// Password/pattern categories
				if (mLockTypePref.getValue().equals(
						getString(R.string.pref_val_lock_type_password))) {
					mLockTypePref
							.setSummary(R.string.pref_list_lock_type_password);
					mPrefScreen.removePreference(mCatPattern);
					mPrefScreen.addPreference(mCatPassword);
				} else {
					mLockTypePref
							.setSummary(R.string.pref_list_lock_type_pattern);
					mPrefScreen.removePreference(mCatPassword);
					mPrefScreen.addPreference(mCatPattern);
				}

			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.pref_key_changelog))) {
			Intent i = new Intent(PrefsActivity.this, StagingActivity.class);
			i.setAction(StagingActivity.ACTION_CHANGELOG_FORCE);
			startActivity(i);
		}
		return false;
	}
}
