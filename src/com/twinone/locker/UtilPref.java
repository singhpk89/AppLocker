package com.twinone.locker;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.twinone.locker.lock.LockActivity;

/**
 * This class contains utility methods for accessing various preferences in
 * Locker
 * 
 * @author twinone
 * 
 */
public abstract class UtilPref {

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

	public static final boolean getVibrate(Context c) {
		return getVibrate(prefs(c), c);
	}

	public static final boolean getVibrate(SharedPreferences sp, Context c) {
		return sp.getBoolean(c.getString(R.string.pref_key_vibrate_keypress),
				Boolean.parseBoolean(c
						.getString(R.string.pref_def_vibrate_keypress)));
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

	//
	// public static final SharedPreferences.Editor setLockType(
	// SharedPreferences.Editor editor, Context c, String value) {
	// editor.putString(c.getString(R.string.pref_key_lock_type), value);
	// return editor;
	// }

	public static final boolean isPasswordEmpty(Context c) {
		return isPasswordEmpty(prefs(c), c);
	}

	public static final boolean isPasswordEmpty(SharedPreferences sp, Context c) {
		if (getLockTypeInt(sp, c) == LockActivity.LOCK_TYPE_PASSWORD) {
			return (UtilPref.getPassword(sp, c).length() == 0);
		} else {
			return (UtilPref.getPattern(sp, c).length() == 0);
		}
	}
}