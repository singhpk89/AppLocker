package com.twinone.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class Analytics {

	public static final String PREF_FILE = "com.twinone.analytics";
	public static final String PREF_KEY_ENABLE_ANALYTICS = "_ALLOW_ANALYTICS_SEND_USAGE_STATISTICS";

	// private Context mContext;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEditor;
	private final boolean mAutoSave = true;
	private final boolean mEnableAnalytics;

	public Analytics(Context c) {
		// mContext = c;
		mPrefs = c.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		mEnableAnalytics = mPrefs.getBoolean(PREF_KEY_ENABLE_ANALYTICS, true);
		if (!mEnableAnalytics) {
			mPrefs = null;
			mEditor = null;
		}
	}
// #57288504
	/**
	 * Utility method for when the user decides to allow or decline analytics.
	 * You should respect the preference of the user.
	 * 
	 * @param c
	 * @param allow
	 */
	@SuppressLint("CommitPrefEdits")
	public static final void setEnableAnalytics(Context c, boolean enable) {
		SharedPreferences.Editor editor = c.getSharedPreferences(PREF_FILE,
				Context.MODE_PRIVATE).edit();
		editor.putBoolean(PREF_KEY_ENABLE_ANALYTICS, enable);
		save(editor);
	}

	public void increment(String key) {
		if (mEnableAnalytics) {
			long value = mPrefs.getLong(key, 0);
			value++;
			mEditor.putLong(key, value);
			autoSave();
		}
	}

	public void decrement(String key) {
		if (mEnableAnalytics) {
			long value = mPrefs.getLong(key, 0);
			value--;
			mEditor.putLong(key, value);
			autoSave();
		}
	}

	public void setEnabled(String key, boolean enabled) {
		if (mEnableAnalytics) {
			mEditor.putBoolean(key, enabled);
			autoSave();
		}
	}

	public void putString(String key, String value) {
		if (mEnableAnalytics) {
			mEditor.putString(key, value);
			autoSave();
		}
	}

	private void autoSave() {
		if (mEnableAnalytics && mAutoSave) {
			save();
		}
	}

	public final void save() {
		save(mEditor);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static final void save(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
}
