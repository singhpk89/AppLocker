package com.twinone.locker.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;

import com.twinone.locker.R;
import com.twinone.locker.lock.LockActivity;

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

	// public String getPassword() {
	// return getPassword(mSP, mContext);
	// }

	public static final String getPassword(Context c) {
		return getPassword(prefs(c), c);
	}

	public static final String getPassword(SharedPreferences sp, Context c) {
		return sp.getString(c.getString(R.string.pref_key_passwd), "");
	}

	// public boolean getVibrate() {
	// return getVibrate(mSP, mContext);
	// }

	public static final boolean getPasswordVibrate(Context c) {
		return getPasswordVibrate(prefs(c), c);
	}

	public static final boolean getPasswordVibrate(SharedPreferences sp,
			Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_vibrate_password),
				Boolean.parseBoolean(c
						.getString(R.string.pref_def_vibrate_password)));
	}

	public static final boolean getPatternVibrate(Context c) {
		return getPatternVibrate(prefs(c), c);
	}

	public static final boolean getPatternVibrate(SharedPreferences sp,
			Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_vibrate_pattern),
				Boolean.parseBoolean(c
						.getString(R.string.pref_def_vibrate_pattern)));
	}

	// Stealth
	public static final boolean getPasswordStealth(Context c) {
		return getPasswordStealth(prefs(c), c);
	}

	public static final boolean getPasswordStealth(SharedPreferences sp,
			Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_hide_dots),
				Boolean.parseBoolean(c.getString(R.string.pref_def_hide_dots)));
	}

	public static final boolean getPatternStealth(Context c) {
		return getPatternStealth(prefs(c), c);
	}

	public static final boolean getPatternStealth(SharedPreferences sp,
			Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_pattern_stealth),
				Boolean.parseBoolean(c
						.getString(R.string.pref_def_pattern_stealth)));
	}

	// public void setPassword(String password) {
	// setPassword(mEditor, mContext, password);
	// }

	// public static final SharedPreferences.Editor setPassword(Context c,
	// String password) {
	// return setPassword(prefs(c).edit(), c, password);
	// }

	public static final SharedPreferences.Editor setPassword(
			SharedPreferences.Editor editor, Context c, String password) {
		editor.putString(c.getString(R.string.pref_key_passwd), password);
		return editor;
	}

	// public String getPattern() {
	// return getPattern(mSP, mContext);
	// }
	public static final String getPattern(Context c) {
		return getPattern(prefs(c), c);
	}

	public static final String getPattern(SharedPreferences sp, Context c) {
		return sp.getString(c.getString(R.string.pref_key_pattern), "");
	}

	// public void setPattern(String pattern) {
	// setPattern(mEditor, mContext, pattern);
	// }

	public static final SharedPreferences.Editor setPattern(
			SharedPreferences.Editor editor, Context c, String pattern) {
		editor.putString(c.getString(R.string.pref_key_pattern), pattern);
		return editor;
	}

	// public String getMessage() {
	// return getMessage(mSP, mContext);
	// }
	public static final String getMessage(Context c) {
		return getMessage(prefs(c), c);
	}

	public static final String getMessage(SharedPreferences sp, Context c) {
		return sp.getString(c.getString(R.string.pref_key_lock_message),
				c.getString(R.string.locker_footer_default));
	}

	// public void setMessage(String message) {
	// setMessage(mEditor, mContext, message);
	// }

	public static final SharedPreferences.Editor setMessage(
			SharedPreferences.Editor editor, Context c, String value) {
		editor.putString(c.getString(R.string.pref_key_lock_message), value);
		return editor;

	}

	// public Set<String> getTrackedApps() {
	// return getTrackedApps(mSP, mContext);
	// }

	public static final Set<String> getTrackedApps(Context c) {
		return getTrackedApps(appsPrefs(c), c);
	}

	public static final Set<String> getTrackedApps(SharedPreferences sp,
			Context c) {
		Set<String> apps = new HashSet<String>(sp.getAll().keySet());
		return apps;
	}

	private static final String getLockType(SharedPreferences sp, Context c) {
		return sp.getString(c.getString(R.string.pref_key_lock_type),
				c.getString(R.string.pref_val_lock_type_password));
	}

	// public int getLockTypeInt() {
	// return getLockTypeInt(mSP, mContext);
	// }

	public static final int getLockTypeInt(Context c) {
		return getLockTypeInt(prefs(c), c);
	}

	/**
	 * @return One of {@link LockActivity#LOCK_TYPE_PASSWORD} or
	 *         {@link LockActivity#LOCK_TYPE_PATTERN} or 0 if none was in
	 *         preferences
	 */
	public static final int getLockTypeInt(SharedPreferences sp, Context c) {
		String lockType = getLockType(sp, c);
		if (lockType.equals(c.getString(R.string.pref_val_lock_type_password))) {
			return LockActivity.LOCK_TYPE_PASSWORD;
		} else if (lockType.equals(c
				.getString(R.string.pref_val_lock_type_pattern))) {
			return LockActivity.LOCK_TYPE_PATTERN;
		} else {
			return 0;
		}

	}

	public static final SharedPreferences.Editor setLockType(
			SharedPreferences.Editor editor, Context c, String value) {
		editor.putString(c.getString(R.string.pref_key_lock_type), value);
		return editor;
	}

	public static final boolean isCurrentPasswordEmpty(Context c) {
		return isCurrentPasswordEmpty(prefs(c), c);
	}

	public static final boolean isCurrentPasswordEmpty(SharedPreferences sp,
			Context c) {
		final int lockType = getLockTypeInt(sp, c);
		switch (lockType) {
		case LockActivity.LOCK_TYPE_PASSWORD:
			return (PrefUtil.getPassword(sp, c).length() == 0);
		case LockActivity.LOCK_TYPE_PATTERN:
			return (PrefUtil.getPattern(sp, c).length() == 0);
		default:
			return true;
		}
	}

	public static final boolean getPasswordSwitchButtons(Context c) {
		return getPasswordSwitchButtons(prefs(c), c);
	}

	public static final boolean getPasswordSwitchButtons(SharedPreferences sp,
			Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_switch_buttons),
				Boolean.parseBoolean(c
						.getString(R.string.pref_def_switch_buttons)));
	}

	public static final int getLockOrientation(Context c) {
		return getLockOrientation(prefs(c), c);
	}

	public static final int getLockOrientation(SharedPreferences sp, Context c) {
		final String ori = sp.getString(
				c.getString(R.string.pref_key_orientation),
				c.getString(R.string.pref_val_orientation_system));
		if (ori.equals(c.getString(R.string.pref_val_orientation_system))) {
			return ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		} else if (ori.equals(c
				.getString(R.string.pref_val_orientation_landscape))) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
				return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			else
				return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		} else if (ori.equals(c
				.getString(R.string.pref_val_orientation_portrait))) {
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}
		return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	}

	public static final String getRecoveryCode(Context c) {
		return getRecoveryCode(prefs(c), c);
	}

	public static final String getRecoveryCode(SharedPreferences sp, Context c) {
		return sp.getString(c.getString(R.string.pref_key_recovery_code), null);
	}

	public static final String generateNewCode() {
		final int min = 10000000;
		final int max = 99999999;
		final Random rand = new Random();
		final int newRandom = rand.nextInt((max - min) + 1) + min;
		return "#" + String.valueOf(newRandom);
	}

	public static final void apply(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

}