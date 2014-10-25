package org.twinone.locker.ui;

import java.util.List;

import org.twinone.locker.lock.AppLockService;
import org.twinone.locker.lock.LockPreferences;
import org.twinone.locker.lock.LockService;
import org.twinone.locker.util.PrefUtils;
import org.twinone.util.ChangeLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twinone.locker.R;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnSharedPreferenceChangeListener,
		OnPreferenceClickListener {

	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	// private static final String TAG = "PrefsActivity";
	// private CheckBoxPreference mShowNotification;
	private CheckBoxPreference mShortExit;
	private EditTextPreference mShortExitTime;
	private CheckBoxPreference mTransparentPref;
	private PreferenceCategory mCatNotif;
	private CheckBoxPreference mStartWithCall;
	private CheckBoxPreference mHideIcon;
	private Preference mChangeLogPref;
	private PreferenceScreen mPrefScreen;
	private Preference mLockTypePref;
	private PreferenceCategory mCatPassword;
	private PreferenceCategory mCatPattern;
	private Preference mRecoveryPref;
	private ListPreference mBackground;

	private EditTextPreference mPatternSize;

	private PrefUtils mPrefUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefUtils = new PrefUtils(getActivity());

		PreferenceManager pm = getPreferenceManager();
		pm.setSharedPreferencesName(PrefUtils.PREF_FILE_DEFAULT);
		pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
		addPreferencesFromResource(R.xml.prefs);

		mPrefs = pm.getSharedPreferences();
		mEditor = pm.getSharedPreferences().edit();
		// mShowNotification = (CheckBoxPreference)
		// findPreference(getString(R.string.pref_key_show_notification));
		mShortExitTime = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
		mShortExit = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
		mCatNotif = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
		mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_notification_icon));
		mStartWithCall = (CheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
		mHideIcon = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
		mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
		mLockTypePref = (Preference) findPreference(getString(R.string.pref_key_lock_type));
		mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
		mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
		mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));
		mRecoveryPref = (Preference) findPreference(getString(R.string.pref_key_recovery_code));
		mBackground = (ListPreference) findPreference(getString(R.string.pref_key_background));
		mPatternSize = (EditTextPreference) findPreference(getString(R.string.pref_key_pattern_size));
		initialize();
	}

	@Override
	public View onCreateView(LayoutInflater paramLayoutInflater,
			ViewGroup paramViewGroup, Bundle paramBundle) {
		getActivity().setTitle(R.string.fragment_title_settings);
		return super.onCreateView(paramLayoutInflater, paramViewGroup,
				paramBundle);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// setupMessagesAndViews();
	}

	protected void initialize() {

		showCategory();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		mShortExitTime.setOnPreferenceChangeListener(this);
		mHideIcon.setOnPreferenceChangeListener(this);
		mStartWithCall.setOnPreferenceChangeListener(this);
		mBackground.setOnPreferenceChangeListener(this);
		mPatternSize.setOnPreferenceChangeListener(this);
		mLockTypePref.setOnPreferenceClickListener(this);
		mChangeLogPref.setOnPreferenceClickListener(this);

		// transparent notification is only available on api level 16+
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			mCatNotif.removePreference(mTransparentPref);
		}

		String code = mPrefUtils.getString(R.string.pref_key_recovery_code);
		if (code != null)
			mRecoveryPref.setSummary(String.format(
					getString(R.string.pref_desc_recovery_code), code));

		// setupMessagesAndViews();
	}

	// When user clicks preference and it has still to be saved
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d("prefs", "Preference change! " + preference.getKey());
		String key = preference.getKey();
		String keyDelayTime = getString(R.string.pref_key_delay_time);
		String background = getString(R.string.pref_key_background);
		String patternSize = getString(R.string.pref_key_pattern_size);

		if (key.equals(keyDelayTime)) {
			String newTime = (String) newValue;
			boolean isZero = (newTime.length() == 0);
			try {
				isZero = (Long.parseLong(newTime) == 0);
			} catch (NumberFormatException e) {
				isZero = true;
			}
			if (isZero) {
				mShortExit.setChecked(false);
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
		} else if (key.equals(patternSize)) {
			int newPatternWidth;
			try {
				newPatternWidth = Integer.parseInt((String) newValue);
			} catch (Exception e) {
				newPatternWidth = Integer.parseInt(getActivity().getString(
						R.string.pref_def_pattern_size));
			}
			LockService.showCreate(getActivity(), LockPreferences.TYPE_PATTERN,
					newPatternWidth);
			return false;
		}
		return true;
	}

	// When the preference changed on disk
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		String keyHideIcon = getString(R.string.pref_key_hide_launcher_icon);
		mPrefs = sp;

		Log.d("", "SharedPreference changed on disk (key=" + key + ")");
		if (key.equals(keyHideIcon)) {
			boolean shouldHide = mHideIcon.isChecked();
			PrefUtils.setHideApplication(getActivity(), shouldHide);
		} else if (key.equals(mPatternSize.getKey())) {
			mPatternSize.setText(sp.getString(key, null));
		}

		setupMessagesAndViews();

		// Reload service stuff
		// Intent intent = AppLockService.getReloadIntent(this);
		// startService(intent);
		Log.d("", "restating service");
		AppLockService.restart(getActivity());
	}

	private void setupMessagesAndViews() {
		// Dial and hide icon
		final boolean dialCheck = mStartWithCall.isChecked();
		final boolean hideCheck = mHideIcon.isChecked();

		mHideIcon.setEnabled(dialCheck);
		mStartWithCall.setEnabled(!hideCheck);
		if (hideCheck) {
			mHideIcon.setEnabled(true);
		}
		// Password/pattern categories
		showCategory();
	}

	private void showCategory() {
		if (mPrefUtils.getCurrentLockTypeInt() == LockPreferences.TYPE_PASSWORD) {
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
	public void onDestroy() {
		super.onDestroy();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		final String key = preference.getKey();
		final String lockType = getString(R.string.pref_key_lock_type);
		final String changelog = getString(R.string.pref_key_changelog);
		final String patternSize = getString(R.string.pref_key_pattern_size);

		if (key.equals(changelog)) {
			ChangeLog cl = new ChangeLog(getActivity());
			cl.show(true);
		} else if (key.equals(lockType)) {
			getChangePasswordDialog(getActivity()).show();
		} else if (key.equals(patternSize)) {
			mPatternSize.setText(mPrefs.getString(patternSize, null));
		}
		return false;
	}

	private void backgroundFromGallery() {
		Log.d("", "background");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		List<ResolveInfo> ris = getActivity().getPackageManager()
				.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
		if (ris.size() > 0) {
			startActivityForResult(Intent.createChooser(intent, null),
					IMG_REQ_CODE);
		} else {
			Toast.makeText(getActivity(), "Error - No gallery app(?)",
					Toast.LENGTH_SHORT).show();
		}
	}

	private static final int IMG_REQ_CODE = 0;

	@Override
	public void onActivityResult(int req, int res, Intent data) {
		Log.d("", "onActivityResult");
		if (req == IMG_REQ_CODE && res == Activity.RESULT_OK) {
			String image = data.getData().toString();
			mPrefUtils.put(R.string.pref_key_background, image).apply();
		}
		Toast.makeText(getActivity(), R.string.background_changed,
				Toast.LENGTH_SHORT).show();
		super.onActivityResult(req, res, data);
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
		choose.setTitle(R.string.old_main_choose_lock_type);
		choose.setItems(R.array.lock_type_names, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int lockType = which == 0 ? LockPreferences.TYPE_PASSWORD
						: LockPreferences.TYPE_PATTERN;
				LockService.showCreate(c, lockType);
			}
		});
		return choose.create();
	}

}
