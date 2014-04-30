package com.twinone.locker.util;

import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

import com.twinone.locker.R;
import com.twinone.locker.lock.LockPreferences;

/**
 * This class contains utility methods for accessing various preferences in
 * Locker
 * 
 * @author twinone
 * 
 */
public class PrefUtils {

	public static final String PREF_FILE_DEFAULT = "com.twinone.locker.prefs.default";
	private static final String PREF_FILE_APPS = "com.twinone.locker.prefs.apps";

	private static final String ALIAS_CLASSNAME = "com.twinone.locker.MainActivityAlias";

	private Context mContext;

	private SharedPreferences mPrefs;
	private SharedPreferences mApps;
	private Editor mEditor;

	/**
	 * Manages everything relative to preferences
	 * 
	 * @param c
	 */
	public PrefUtils(Context c) {
		mContext = c;
		mPrefs = mContext.getSharedPreferences(PREF_FILE_DEFAULT,
				Context.MODE_PRIVATE);
	}

	public Editor editor() {
		if (mEditor == null) {
			mEditor = mPrefs.edit();
		}
		return mEditor;
	}

	public SharedPreferences prefs() {
		return mPrefs;
	}

	public SharedPreferences apps() {
		if (mApps == null) {
			mApps = mContext.getSharedPreferences(PREF_FILE_APPS,
					Context.MODE_PRIVATE);
		}
		return mApps;
	}

	public Editor put(int keyResId, Object value) {
		final String key = mContext.getString(keyResId);
		if (key == null) {
			throw new IllegalArgumentException(
					"No resource matched key resource id");
		}
		Log.d("", "putting (key=" + key + ",value=" + value + ")");
		final Editor editor = editor();
		if (value instanceof String)
			editor.putString(key, (String) value);
		else if (value instanceof Integer)
			editor.putInt(key, (Integer) value);
		else if (value instanceof Boolean)
			editor.putBoolean(key, (Boolean) value);
		else if (value instanceof Float)
			editor.putFloat(key, (Float) value);
		else if (value instanceof Long)
			editor.putLong(key, (Long) value);
		else
			throw new IllegalArgumentException("Unknown data type");
		return editor;
	}

	/**
	 * Put a String by resource id
	 * 
	 * @param keyResId
	 *            the key of the preference
	 * @param valueResId
	 *            the res id of the value string
	 */
	public Editor putString(int keyResId, int valueResId) {
		final Editor editor = editor();
		editor.putString(mContext.getString(keyResId),
				mContext.getString(valueResId));
		return editor;
	}

	/**
	 * Get a string by res id
	 * 
	 * @param keyResId
	 * @return
	 */
	public String getString(int keyResId) {
		return mPrefs.getString(mContext.getString(keyResId), null);
	}

	/**
	 * Get a string preference
	 * 
	 * @param keyResId
	 * @param defResId
	 *            the res id of the string that should be returned if the
	 *            preference was not set (note that this should be the resid of
	 *            a string, not the resid of a preference key)
	 * @return
	 */
	public String getString(int keyResId, int defResId) {
		final String key = mContext.getString(keyResId);
		return (mPrefs.contains(key)) ? mPrefs.getString(key, null) : mContext
				.getString(defResId);
	}

	/**
	 * Get a string preference
	 * 
	 * @param keyResId
	 * @param defValue
	 *            The string to return if the preference was not set
	 * @return
	 */
	public String getString(int keyResId, String defValue) {
		return mPrefs.getString(mContext.getString(keyResId), defValue);
	}

	public Integer getInt(int keyResId) {
		try {
			return mPrefs.getInt(mContext.getString(keyResId), (Integer) null);
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Parse an Integer that is stored as a string
	 * 
	 * @return The Integer or null if there was an error
	 */
	public Integer parseInt(int keyResId) {
		try {
			return Integer.parseInt(getString(keyResId));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse an Integer that is stored as a String or get the default value
	 * which is also stored as a String
	 * 
	 * @param keyResId
	 *            The resId of the stored String
	 * @param defResId
	 *            The resId of the default String
	 * @return
	 */
	public Integer parseInt(int keyResId, int defResId) {
		final Integer result = parseInt(keyResId);
		return (result != null) ? result : Integer.parseInt(mContext
				.getString(defResId));
	}

	/**
	 * Same as {@link #parseInt(int)}
	 * 
	 * @return The Long or null if there was an error
	 */
	public Integer parseLong(int keyResId) {
		try {
			return Integer.parseInt(getString(keyResId));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Same as {@link #parseInt(int)}
	 */
	public Long parseLong(int keyResId, int defResId) {
		final Integer result = parseInt(keyResId);
		return (result != null) ? result : Long.parseLong(mContext
				.getString(defResId));
	}

	/**
	 * 
	 * @param keyResId
	 * @param defResId
	 *            a res id of a boolean resource (in {@link R.bool}
	 * @return
	 */
	public boolean getBoolean(int keyResId, int defResId) {
		final Boolean result = getBooleanOrNull(keyResId);
		return result != null ? result : mContext.getResources().getBoolean(
				defResId);
	}

	private Boolean getBooleanOrNull(int keyResId) {
		final String key = mContext.getString(keyResId);
		return (mPrefs.contains(key)) ? mPrefs.getBoolean(key, false) : null;
	}

	public Float getFloatOrNull(int keyResId) {
		final String key = mContext.getString(keyResId);
		return (mPrefs.contains(key)) ? mPrefs.getFloat(key, 0) : null;
	}

	public Long getLongOrNull(int keyResId) {
		final String key = mContext.getString(keyResId);
		return (mPrefs.contains(key)) ? mPrefs.getLong(key, 0) : null;
	}

	/**
	 * After applying, call {@link #editor()} again.
	 */
	public void apply() {
		apply(editor());
		mEditor = null;
	}

	/***************************************************************************
	 * OLD
	 * 
	 */
	// private SharedPreferences mSP;
	// private SharedPreferences.Editor mEditor;
	// private Context mContext;
	//
	// // public UtilPref(Context c) {
	// // mContext = c;
	// // mSP = prefs(c);
	// // mEditor = mSP.edit();
	// // }

	/**
	 * Get the default shared preferences where everything is stored except the
	 * apps
	 */
	public static final SharedPreferences prefs(Context c) {
		return c.getSharedPreferences(PREF_FILE_DEFAULT, Context.MODE_PRIVATE);
	}

	public static final SharedPreferences appsPrefs(Context c) {
		return c.getSharedPreferences(PREF_FILE_APPS, Context.MODE_PRIVATE);
	}

	public static final Set<String> getLockedApps(Context c) {
		SharedPreferences sp = appsPrefs(c);
		Set<String> apps = new HashSet<String>(sp.getAll().keySet());
		return apps;
	}

	/**
	 * Never null
	 * 
	 * @return
	 */
	public String getCurrentLockType() {
		return getString(R.string.pref_key_lock_type,
				R.string.pref_def_lock_type);
	}

	public int getCurrentLockTypeInt() {
		final String type = getCurrentLockType();
		if (type.equals(mContext
				.getString(R.string.pref_val_lock_type_password)))
			return LockPreferences.TYPE_PASSWORD;
		else if (type.equals(mContext
				.getString(R.string.pref_val_lock_type_pattern)))
			return LockPreferences.TYPE_PATTERN;
		return 0;

	}

	/**
	 * Returns the current password or pattern
	 * 
	 * @return
	 */
	public String getCurrentPassword() {
		int lockType = getCurrentLockTypeInt();
		String password = null;
		switch (lockType) {
		case LockPreferences.TYPE_PASSWORD:
			password = getString(R.string.pref_key_password);
		case LockPreferences.TYPE_PATTERN:
			password = getString(R.string.pref_key_pattern);
		}
		return password;
	}

	public boolean isCurrentPasswordEmpty() {
		String password = getCurrentPassword();
		return password == null || password.isEmpty();
	}

	@SuppressLint("NewApi")
	public static final void apply(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

	public static void setHideApplication(Context c, boolean hide) {
		ComponentName cn = new ComponentName(c.getApplicationContext(),
				ALIAS_CLASSNAME);
		int setting = hide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				: PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		int current = c.getPackageManager().getComponentEnabledSetting(cn);
		if (current != setting) {
			c.getPackageManager().setComponentEnabledSetting(cn, setting,
					PackageManager.DONT_KILL_APP);
		}
	}

	/**
	 * Generates a recovery code based on Android ID
	 * 
	 * @param context
	 * @return
	 */
	public static String generateRecoveryCode(Context context) {
		String androidId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		if ((androidId == null) || (androidId.equals("9774d56d682e549c"))
				|| (androidId.equals("0000000000000000"))) {
			androidId = "fdaffadfaedfaedf827382164762349787adadfebcbc";
		}
		return String.valueOf(androidId.hashCode());
	}

}