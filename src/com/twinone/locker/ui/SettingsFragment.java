/*
 * Copyright 2014 Luuk Willemsen (Twinone)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.twinone.locker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
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
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.LockService;
import com.twinone.locker.pro.ProUtils;
import com.twinone.locker.pro.pref.ProCheckBoxPreference;
import com.twinone.locker.pro.pref.ProEditTextPreference;
import com.twinone.locker.pro.pref.ProListPreference;
import com.twinone.locker.pro.pref.ProPreference;
import com.twinone.locker.util.PrefUtils;
import com.twinone.util.ChangeLog;

public class SettingsFragment extends PreferenceFragment implements
		OnPreferenceChangeListener, OnSharedPreferenceChangeListener,
		OnPreferenceClickListener {

	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;
	// private static final String TAG = "PrefsActivity";
	private ProCheckBoxPreference mShowNotification;
	private ProCheckBoxPreference mShortExit;
	private EditTextPreference mShortExitTime;
	private CheckBoxPreference mTransparentPref;
	private PreferenceCategory mCatNotif;
	private ProCheckBoxPreference mStartWithCall;
	private ProCheckBoxPreference mHideIcon;
	private Preference mChangeLogPref;
	private PreferenceScreen mPrefScreen;
	private Preference mLockTypePref;
	private PreferenceCategory mCatPassword;
	private PreferenceCategory mCatPattern;
	private Preference mRecoveryPref;
	private ProListPreference mBackground;

	private ProEditTextPreference mPatternSize;

	private ProUtils mProUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mProUtils = new ProUtils(getActivity());

		PreferenceManager pm = getPreferenceManager();
		pm.setSharedPreferencesName(PrefUtils.PREF_FILE_DEFAULT);
		pm.setSharedPreferencesMode(Context.MODE_PRIVATE);
		addPreferencesFromResource(R.xml.prefs);

		mPrefs = pm.getSharedPreferences();
		mEditor = pm.getSharedPreferences().edit();
		mShowNotification = (ProCheckBoxPreference) findPreference(getString(R.string.pref_key_show_notification));
		mShortExitTime = (EditTextPreference) findPreference(getString(R.string.pref_key_delay_time));
		mShortExit = (ProCheckBoxPreference) findPreference(getString(R.string.pref_key_delay_status));
		mCatNotif = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_notification));
		mTransparentPref = (CheckBoxPreference) findPreference(getString(R.string.pref_key_hide_notification_icon));
		mStartWithCall = (ProCheckBoxPreference) findPreference(getString(R.string.pref_key_dial_launch));
		mHideIcon = (ProCheckBoxPreference) findPreference(getString(R.string.pref_key_hide_launcher_icon));
		mChangeLogPref = (Preference) findPreference(getString(R.string.pref_key_changelog));
		mLockTypePref = (Preference) findPreference(getString(R.string.pref_key_lock_type));
		mPrefScreen = (PreferenceScreen) findPreference(getString(R.string.pref_key_screen));
		mCatPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_password));
		mCatPattern = (PreferenceCategory) findPreference(getString(R.string.pref_key_cat_pattern));
		mRecoveryPref = (Preference) findPreference(getString(R.string.pref_key_recovery_code));
		mBackground = (ProListPreference) findPreference(getString(R.string.pref_key_background));
		mPatternSize = (ProEditTextPreference) findPreference(getString(R.string.pref_key_pattern_size));
		initialize();
	}

	@Override
	public View onCreateView(LayoutInflater paramLayoutInflater,
			ViewGroup paramViewGroup, Bundle paramBundle) {
		getActivity().setTitle(R.string.fragment_title_settings);
		// ((MainActivity) getActivity())
		// .setActionBarTitle(R.string.main_preferences);
		return super.onCreateView(paramLayoutInflater, paramViewGroup,
				paramBundle);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupMessagesAndViews();
	}

	/**
	 * Defines what should happen once the {@link PreferenceActivity} is
	 * displayed to the user.
	 */
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

		String code = PrefUtils.getRecoveryCode(getActivity());
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
			Intent i = LockService.getDefaultIntent(getActivity());
			i.setAction(LockService.ACTION_CREATE);
			i.putExtra(LockService.EXTRA_PATTERN_WIDTH, newPatternWidth);
			getActivity().startService(i);
			return false;
		}
		return true;
	}

	// When the preference changed on disk
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		String keyHideIcon = getString(R.string.pref_key_hide_launcher_icon);

		if (key.equals(keyHideIcon)) {
			boolean shouldHide = mHideIcon.isChecked();
			PrefUtils.setHideApplication(getActivity(), shouldHide);
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
		for (int i = 0; i < getListView().getCount(); i++) {
			Preference p = (Preference) getListView().getItemAtPosition(i);
			if (p instanceof ProPreference) {

				((ProPreference) p).getHelper().setProUtils(mProUtils);
				// ((IProPreference) p).getHelper().updateProFlag();
			}
		}

	}

	private void showCategory() {
		if (PrefUtils.getLockTypeInt(getActivity()) == LockService.LOCK_TYPE_PASSWORD) {
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
		String key = preference.getKey();
		String lockType = getString(R.string.pref_key_lock_type);
		String changelog = getString(R.string.pref_key_changelog);

		if (key.equals(changelog)) {
			ChangeLog cl = new ChangeLog(getActivity());
			cl.show(true);
		} else if (key.equals(lockType)) {
			getChangePasswordDialog(getActivity()).show();
		}
		return false;
	}

	private void backgroundFromGallery() {
		Log.d("", "background");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(Intent.createChooser(intent, null), IMG_REQ_CODE);

	}

	private static final int IMG_REQ_CODE = 0;

	@Override
	public void onActivityResult(int req, int res, Intent data) {
		Log.d("", "onActivityResult");
		if (req == IMG_REQ_CODE && res == Activity.RESULT_OK) {
			Uri image = data.getData();
			SharedPreferences.Editor editor = PrefUtils.prefs(getActivity())
					.edit();
			PrefUtils.setLockerBackground(editor, getActivity(),
					image.toString());
			PrefUtils.apply(editor);
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
				int lockType = which == 0 ? LockService.LOCK_TYPE_PASSWORD
						: LockService.LOCK_TYPE_PATTERN;
				Intent i = LockService.getDefaultIntent(c);
				i.setAction(LockService.ACTION_CREATE);
				i.putExtra(LockService.EXTRA_VIEW_TYPE, lockType);
				c.startService(i);
			}
		});
		return choose.create();
	}

}
