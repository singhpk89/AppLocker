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
import com.twinone.locker.lock.LockService;

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

	public Editor editor() {
		if (mEditor != null) {
			mEditor = mPrefs.edit();
		}
		return mEditor;
	}

	public SharedPreferences apps() {
		if (mApps != null) {
			mApps = mContext.getSharedPreferences(PREF_FILE_APPS,
					Context.MODE_PRIVATE);
		}
		return mApps;
	}

	public void put(int keyResId, Object value) {
		final String key = mContext.getString(keyResId);
		if (key == null) {

		}
		if (value instanceof String)
			editor().putString(key, (String) value);
		else if (value instanceof Integer)
			editor().putInt(key, (Integer) value);
		else if (value instanceof Boolean)
			editor().putBoolean(key, (Boolean) value);
		else if (value instanceof Float)
			editor().putFloat(key, (Float) value);
		else if (value instanceof Long)
			editor().putLong(key, (Long) value);
		else
			throw new IllegalArgumentException("Unknown data type");
	}

	public String getString(int keyResId) {
		try {
			return mPrefs.getString(mContext.getString(keyResId), null);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public Integer getInt(int keyResId) {
		try {
			return mPrefs.getInt(mContext.getString(keyResId), (Integer) null);
		} catch (NullPointerException e) {
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

	public Boolean getBoolean(int keyResId) {
		try {
			return mPrefs.getBoolean(mContext.getString(keyResId),
					(Boolean) null);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public Float getFloatOrNull(int keyResId) {
		try {
			return mPrefs.getFloat(mContext.getString(keyResId), (Float) null);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public Long getLongOrNull(int keyResId) {
		try {
			return mPrefs.getLong(mContext.getString(keyResId), (Long) null);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public String getString(int keyResId, int defResId) {
		final String value = getString(keyResId);
		return (value != null) ? value : mContext.getString(defResId);
	}

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

	private static final int parseInt(Context c, int prefKeyResId,
			int prefDefResId) {
		try {
			int ret = Integer.parseInt(prefs(c).getString(
					c.getString(prefKeyResId), c.getString(prefDefResId)));
			return ret;
		} catch (Exception e) {
			Log.w("PrefUtil", "Error parsing int");
			return 0;
		}
	}

	public static final int getAnimShowMillis(Context c) {
		return parseInt(c, R.string.pref_key_anim_show_millis,
				R.string.pref_def_anim_show_millis);
	}

	public static final int getAnimHideMillis(Context c) {
		return parseInt(c, R.string.pref_key_anim_hide_millis,
				R.string.pref_def_anim_hide_millis);
	}

	public static final String getAnimShowType(Context c) {
		return getString(c, R.string.pref_key_anim_show_type,
				R.string.pref_val_anim_none);
	}

	public static final String getAnimHideType(Context c) {
		return getString(c, R.string.pref_key_anim_hide_type,
				R.string.pref_val_anim_fade);
	}

	private static final boolean getBoolean(Context c, int prefKeyResId,
			int prefDefResId) {
		return prefs(c).getBoolean(c.getString(prefKeyResId),
				Boolean.parseBoolean(c.getString(prefDefResId)));
	}

	private static final String getString(Context c, int prefKeyResId,
			int prefDefResId) {
		return prefs(c).getString(c.getString(prefKeyResId),
				c.getString(prefDefResId));
	}

	private static final String getStringOrNull(Context c, int prefKeyResId) {
		return prefs(c).getString(c.getString(prefKeyResId), null);
	}

	private static final float getFloat(Context c, int prefKeyResId,
			int prefDefResId) {
		return prefs(c).getFloat(c.getString(prefKeyResId),
				Float.parseFloat(c.getString(prefDefResId)));
	}

	private static final long parseLong(Context c, int prefKeyResId,
			int prefDefResId) {
		return Long.parseLong(prefs(c).getString(c.getString(prefKeyResId),
				c.getString(prefDefResId)));
	}

	public static final boolean getStartAtBoot(Context c) {
		return getBoolean(c, R.string.pref_key_start_boot,
				R.string.pref_def_start_boot);
	}

	public static final String getPassword(Context c) {
		return getStringOrNull(c, R.string.pref_key_passwd);
	}

	public static final String getPattern(Context c) {
		return getStringOrNull(c, R.string.pref_key_pattern);
	}

	public static final boolean getVibrate(Context c) {
		return getBoolean(c, R.string.pref_key_vibrate,
				R.string.pref_def_vibrate);
	}

	public static final boolean getAds(Context c) {
		return getBoolean(c, R.string.pref_key_ads, R.string.pref_def_ads);
	}

	public static final boolean getPasswordStealth(Context c) {
		return getBoolean(c, R.string.pref_key_hide_dots,
				R.string.pref_def_hide_dots);
	}

	public static final boolean getPatternStealth(Context c) {
		return getBoolean(c, R.string.pref_key_pattern_stealth,
				R.string.pref_def_pattern_stealth);
	}

	// SETTERS

	public static final SharedPreferences.Editor setPassword(
			SharedPreferences.Editor editor, Context c, String password) {
		editor.putString(c.getString(R.string.pref_key_passwd), password);
		return editor;
	}

	public static final SharedPreferences.Editor setPattern(
			SharedPreferences.Editor editor, Context c, String pattern) {
		editor.putString(c.getString(R.string.pref_key_pattern), pattern);
		return editor;
	}

	public static final String getMessage(Context c) {
		return getString(c, R.string.pref_key_lock_message,
				R.string.locker_footer_default);
	}

	public static final boolean getDialLaunch(Context c) {
		return getBoolean(c, R.string.pref_key_dial_launch,
				R.string.pref_def_dial_launch);
	}

	public static final String getDialLaunchNumber(Context c) {
		return getString(c, R.string.pref_key_dial_launch_number,
				R.string.pref_def_dial_launch_number);
	}

	public static final SharedPreferences.Editor setMessage(
			SharedPreferences.Editor editor, Context c, String value) {
		editor.putString(c.getString(R.string.pref_key_lock_message), value);
		return editor;

	}

	public static final Set<String> getLockedApps(Context c) {
		SharedPreferences sp = appsPrefs(c);
		Set<String> apps = new HashSet<String>(sp.getAll().keySet());
		return apps;
	}

	private static final String getLockType(Context c) {
		return getString(c, R.string.pref_key_lock_type,
				R.string.pref_val_lock_type_password);
	}

	/**
	 * @return One of {@link LockActivity#LOCK_TYPE_PASSWORD} or
	 *         {@link LockActivity#LOCK_TYPE_PATTERN} or 0 if none was in
	 *         preferences
	 */
	public static final int getLockTypeInt(Context c) {
		String lockType = getLockType(c);
		if (lockType.equals(c.getString(R.string.pref_val_lock_type_password))) {
			return LockService.LOCK_TYPE_PASSWORD;
		} else if (lockType.equals(c
				.getString(R.string.pref_val_lock_type_pattern))) {
			return LockService.LOCK_TYPE_PATTERN;
		} else {
			return 0;
		}
	}

	/**
	 * @return
	 */
	public static final int getPatternCircleColor(Context c) {
		String color = getString(c, R.string.pref_key_pattern_color,
				R.string.pref_val_pattern_color_white);
		String blue = c.getString(R.string.pref_val_pattern_color_blue);
		String green = c.getString(R.string.pref_val_pattern_color_green);
		if (color.equals(green)) {
			return LockService.PATTERN_COLOR_GREEN;
		} else if (color.equals(blue)) {
			return LockService.PATTERN_COLOR_BLUE;
		} else {
			return LockService.PATTERN_COLOR_WHITE;
		}
	}

	public static final int getPatternWidth(Context c) {
		String width = getString(c, R.string.pref_key_pattern_size,
				R.string.pref_def_pattern_size);
		try {
			return Integer.parseInt(width);
		} catch (Exception e) {
		}
		return Integer.parseInt(c.getString(R.string.pref_def_pattern_size));
	}

	/**
	 * May be null
	 * 
	 * @param c
	 * @return
	 */
	public static final String getLockerBackground(Context c) {
		return getString(c, R.string.pref_key_background,
				R.string.pref_val_bg_default);
	}

	public static final SharedPreferences.Editor setLockerBackground(
			SharedPreferences.Editor editor, Context c, String value) {
		editor.putString(c.getString(R.string.pref_key_background), value);
		return editor;
	}

	public static final SharedPreferences.Editor setLockType(
			SharedPreferences.Editor editor, Context c, String value) {
		editor.putString(c.getString(R.string.pref_key_lock_type), value);
		return editor;
	}

	public static final boolean isCurrentPasswordEmpty(Context c) {
		final int lockType = getLockTypeInt(c);
		switch (lockType) {
		case LockService.LOCK_TYPE_PASSWORD:
			final String password = getPassword(c);
			return password == null || password.length() == 0;
		case LockService.LOCK_TYPE_PATTERN:
			final String pattern = getPattern(c);
			return pattern == null || pattern.length() == 0;
		default:
			return true;
		}
	}

	public static final boolean getPasswordSwitchButtons(Context c) {
		return getBoolean(c, R.string.pref_key_switch_buttons,
				R.string.pref_def_switch_buttons);
	}

	public static final String getLockOrientation(Context c) {
		return getString(c, R.string.pref_key_orientation,
				R.string.pref_val_orientation_system);
	}

	public static final String getRecoveryCode(Context c) {
		return getStringOrNull(c, R.string.pref_key_recovery_code);
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