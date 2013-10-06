package com.twinone.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.twinone.locker.R;

public class PrefManager {

	public static final Pref START_AT_BOOT = new Pref(R.string.pref_key_start_boot, R.string.pref_def_start_boot);
	

	public static final String PREF_FILE_DEFAULT = "com.twinone.locker.prefs.default";
	// private static final String PREF_FILE_APPS =
	// "com.twinone.locker.prefs.apps";

	private final Context mContext;

	private SharedPreferences getSharedPreferences() {
		return mContext.getSharedPreferences(PREF_FILE_DEFAULT,
				Context.MODE_PRIVATE);
	}

	public PrefManager(Context context) {
		mContext = context;
	}

	public static class Pref {
		public final int key;
		public final int def;

		public Pref(int keyResId, int defaultResId) {
			key = keyResId;
			def = defaultResId;
		}
	}

	public boolean getBoolean(Pref pref) {
		final String key = mContext.getString(pref.key);
		final String defString = mContext.getString(pref.def);
		final boolean def = Boolean.parseBoolean(defString);
		return getSharedPreferences().getBoolean(key, def);
	}

	public int getInt(Pref pref) {
		final String key = mContext.getString(pref.key);
		final String defString = mContext.getString(pref.def);
		final int def = Integer.parseInt(defString);
		return getSharedPreferences().getInt(key, def);
	}

	public float getFloat(Pref pref) {
		final String key = mContext.getString(pref.key);
		final String defString = mContext.getString(pref.def);
		final float def = Float.parseFloat(defString);
		return getSharedPreferences().getFloat(key, def);
	}

	public long getLong(Pref pref) {
		final String key = mContext.getString(pref.key);
		final String defString = mContext.getString(pref.def);
		final long def = Long.parseLong(defString);
		return getSharedPreferences().getLong(key, def);
	}

	public String getString(Pref pref) {
		final String key = mContext.getString(pref.key);
		final String def = mContext.getString(pref.def);
		return getSharedPreferences().getString(key, def);
	}

}
