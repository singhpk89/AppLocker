package com.twinone.locker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.twinone.locker.lock.AlarmService;
import com.twinone.locker.lock.LockViewService;
import com.twinone.locker.util.PrefUtil;
import com.twinone.locker.version.VersionManager;
import com.twinone.util.ChangeLog;

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
	protected Preference mLockTypePref;
	protected PreferenceCategory mCatPassword;
	protected PreferenceCategory mCatPattern;
	protected Preference mRecoveryPref;
	protected ListPreference mLockerBackgroundPref;
	protected CheckBoxPreference mAdsPref;

	// private Handler mHandler = new Handler();

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
			mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_notification_icon));
			mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
			mLockTypePref = (Preference) findPreference(getString(R.string.pref_key_lock_type));
			mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
			mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
			mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));
			mRecoveryPref = (Preference) findPreference(getString(R.string.pref_key_recovery_code));
			mLockerBackgroundPref = (ListPreference) findPreference(getString(R.string.pref_key_background));
			mAdsPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_ads));

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
			parent.mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_notification_icon));
			parent.mDialStatusPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
			parent.mHideIconPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
			parent.mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
			parent.mLockTypePref = (Preference) findPreference(getString(R.string.pref_key_lock_type));
			parent.mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
			parent.mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
			parent.mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));
			parent.mRecoveryPref = (Preference) findPreference(getString(R.string.pref_key_recovery_code));
			parent.mLockerBackgroundPref = (ListPreference) findPreference(getString(R.string.pref_key_background));
			parent.mAdsPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_ads));

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
		mLockerBackgroundPref.setOnPreferenceChangeListener(this);

		mLockTypePref.setOnPreferenceClickListener(this);
		mChangeLogPref.setOnPreferenceClickListener(this);

		// transparent notification is only available on api level 16+
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mCatNotif.removePreference(mTransparentPref);
		}

		Boolean ads = Boolean.parseBoolean(VersionManager.getValue(this,
				VersionKeys.ENABLE_ADS, "false"));
		if (ads) {
			mAdsPref.setChecked(true);
			mAdsPref.setEnabled(false);
			mAdsPref.setSummary(R.string.pref_desc_ads_forced);
		}
		String code = PrefUtil.getRecoveryCode(this);
		if (code != null)
			mRecoveryPref.setSummary(String.format(
					getString(R.string.pref_desc_recovery_code), code));
		setupMessagesAndViews();
	}

	// When user clicks preference and it has still to be saved
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("prefs", "Preference change! " + preference.getKey());
		String key = preference.getKey();
		String keyDelayTime = getString(R.string.pref_key_delay_time);
		String background = getString(R.string.pref_key_background);

		if (key.equals(keyDelayTime)) {
			String newTime = (String) newValue;
			boolean isZero = (newTime.length() == 0);
			try {
				isZero = (Long.parseLong(newTime) == 0);
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
		} else if (key.equals(background)) {
			Log.d("", "newValue:" + newValue);
			if (newValue.equals(getString(R.string.pref_val_bg_gallery))) {
				backgroundFromGallery();
			}
		}
		return true;
	}

	// When the preference changed on disk
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		String keyHideIcon = getString(R.string.pref_key_hide_launcher_icon);

		if (key.equals(keyHideIcon)) {
			boolean shouldHide = mHideIconPref.isChecked();
			ComponentName cn = new ComponentName(this.getApplicationContext(),
					ALIAS_CLASSNAME);
			int enabled = shouldHide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					: PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
			int current = getPackageManager().getComponentEnabledSetting(cn);
			if (current != enabled) {
				getPackageManager().setComponentEnabledSetting(cn, enabled,
						PackageManager.DONT_KILL_APP);
			}
		}

		setupMessagesAndViews();

		// Reload service stuff
		// Intent intent = AppLockService.getReloadIntent(this);
		// startService(intent);
		Log.d("", "restating service");
		AlarmService.restart(this);
	}

	private void setupMessagesAndViews() {
		// Dial and hide icon
		final boolean dialCheck = mDialStatusPref.isChecked();
		final boolean hideCheck = mHideIconPref.isChecked();

		mHideIconPref.setEnabled(dialCheck);
		mDialStatusPref.setEnabled(!hideCheck);
		if (hideCheck) {
			mHideIconPref.setEnabled(true);
		}
		// Password/pattern categories
		if (PrefUtil.getLockTypeInt(this) == LockViewService.LOCK_TYPE_PASSWORD) {
			mLockTypePref.setSummary(R.string.pref_list_lock_type_password);
			mPrefScreen.removePreference(mCatPattern);
			mPrefScreen.addPreference(mCatPassword);
		} else {
			mLockTypePref.setSummary(R.string.pref_list_lock_type_pattern);
			mPrefScreen.removePreference(mCatPassword);
			mPrefScreen.addPreference(mCatPattern);
		}

	}

	// });
	// }

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
		String key = preference.getKey();
		String lockType = getString(R.string.pref_key_lock_type);
		String changelog = getString(R.string.pref_key_changelog);

		if (key.equals(changelog)) {
			ChangeLog cl = new ChangeLog(this);
			cl.show(true);
		} else if (key.equals(lockType)) {
			getChangePasswordDialog(this).show();
		}
		return false;
	}

	private void backgroundFromGallery() {
		mPermitOnPause = true;
		Log.d("", "background");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(Intent.createChooser(intent, null), IMG_REQ_CODE);

	}

	private static final int IMG_REQ_CODE = 0;

	@Override
	protected void onActivityResult(int req, int res, Intent data) {
		Log.d("", "onActivityResult");
		if (req == IMG_REQ_CODE && res == Activity.RESULT_OK) {
			Uri image = data.getData();
			SharedPreferences.Editor editor = PrefUtil.prefs(this).edit();
			PrefUtil.setLockerBackground(editor, this, image.toString());
			PrefUtil.apply(editor);
		}
		Toast.makeText(this, R.string.background_changed, Toast.LENGTH_SHORT)
				.show();
		super.onActivityResult(req, res, data);
	}

	private boolean mPermitOnPause = false;

	@Override
	protected void onResume() {
		mPermitOnPause = false;
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!mPermitOnPause) {
			finish();
		}
		LockViewService.hide(this);
		super.onPause();
	}

	/**
	 * The dialog that allows the user to select between password and pattern
	 * options
	 * 
	 * @param c
	 * @return
	 */
	public static AlertDialog getChangePasswordDialog(final Context c) {
		final AlertDialog.Builder choose = new AlertDialog.Builder(c);
		choose.setTitle(R.string.main_choose_lock_type);
		choose.setItems(R.array.lock_type_names, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int lockType = which == 0 ? LockViewService.LOCK_TYPE_PASSWORD
						: LockViewService.LOCK_TYPE_PATTERN;
				Intent i = LockViewService.getDefaultIntent(c);
				i.setAction(LockViewService.ACTION_CREATE);
				i.putExtra(LockViewService.EXTRA_VIEW_TYPE, lockType);
				c.startService(i);
			}
		});
		return choose.create();
	}
}
