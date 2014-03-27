package com.twinone.locker.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.twinone.locker.R;
import com.twinone.locker.lock.LockViewService;

/**
 * This class contains utility methods for accessing various preferences in
 * Locker
 * 
 * @author twinone
 * 
 */
public abstract class PrefUtil {

	public static final String PREF_FILE_DEFAULT = "com.twinone.locker.prefs.default";
	private static final String PREF_FILE_APPS = "com.twinone.locker.prefs.apps";

	private static final String ALIAS_CLASSNAME = "com.twinone.locker.MainActivityAlias";

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
			Log.w("PrefUtil", "Error formatting int");
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

	public static final boolean getPasswordVibrate(Context c) {
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

	public static final Set<String> getTrackedApps(Context c) {
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
			return LockViewService.LOCK_TYPE_PASSWORD;
		} else if (lockType.equals(c
				.getString(R.string.pref_val_lock_type_pattern))) {
			return LockViewService.LOCK_TYPE_PATTERN;
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
			return LockViewService.PATTERN_COLOR_GREEN;
		} else if (color.equals(blue)) {
			return LockViewService.PATTERN_COLOR_BLUE;
		} else {
			return LockViewService.PATTERN_COLOR_WHITE;
		}
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
		case LockViewService.LOCK_TYPE_PASSWORD:
			final String password = getPassword(c);
			return password == null || password.length() == 0;
		case LockViewService.LOCK_TYPE_PATTERN:
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

	public static final String generateRecoveryCode() {
		final int min = 10000000;
		final int max = 99999999;
		final Random rand = new Random();
		final int newRandom = rand.nextInt(max - min + 1) + min;
		return "#" + String.valueOf(newRandom);
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

}