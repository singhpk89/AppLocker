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
package com.twinone.locker.lock;

import java.io.Serializable;

import android.content.Context;

import com.twinone.locker.R;
import com.twinone.locker.pro.ProUtils;
import com.twinone.locker.util.PrefUtils;

public class LockPreferences implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2334826883469805015L;

	// Common
	public String lockType;
	public String orientation;
	public Boolean vibration;
	public String message;
	public int patternWidth;

	// Pro only
	public String background;
	public String showAnimation;
	public String hideAnimation;
	public int showAnimationMillis;
	public int hideAnimationMiliis;

	// Password only
	public String password;
	public boolean switchButtons;

	// Pattern only
	public String pattern;
	public boolean patternStealth;
	public boolean patternHideError;

	// Pro & pattern only
	public String patternColor;

	private LockPreferences() {
	}

	/**
	 * You should use this constructor which loads all properties into the
	 * object automatically
	 * 
	 * @param c
	 * @return
	 */
	public static LockPreferences getDefault(Context c) {
		LockPreferences result = new LockPreferences();
		boolean pro = new ProUtils(c).proFeaturesEnabled();
		PrefUtils prefs = new PrefUtils(c);
		// Common
		result.lockType = prefs.getString(R.string.pref_key_lock_type);
		result.orientation = prefs.getString(R.string.pref_key_orientation);
		result.vibration = prefs.getBoolean(R.string.pref_key_vibrate);
		result.message = prefs.getString(R.string.pref_key_lock_message);
		result.patternWidth = prefs.parseInt(R.string.pref_key_pattern_size,
				R.string.pref_def_pattern_size);
		if (pro) {
			// Pro only
			result.background = prefs.getString(R.string.pref_key_background,
					R.string.pref_def_background);
			result.showAnimation = prefs.getString(
					R.string.pref_key_anim_show_type,
					R.string.pref_def_anim_show_type);
			result.hideAnimation = prefs.getString(
					R.string.pref_key_anim_hide_type,
					R.string.pref_def_anim_hide_type);
			result.showAnimationMillis = prefs.parseInt(
					R.string.pref_key_anim_show_millis,
					R.string.pref_def_anim_show_millis);
			result.showAnimationMillis = prefs.parseInt(
					R.string.pref_key_anim_show_millis,
					R.string.pref_def_anim_show_millis);
		}
		if (c.getString(R.string.pref_val_lock_type_password).equals(
				result.lockType)) {
			// Passwd only
			result.password = prefs.getString(R.string.pref_key_passwd);
			result.switchButtons = prefs
					.getBoolean(R.string.pref_key_switch_buttons);
		} else {
			// // Pattern only
			result.pattern = prefs.getString(R.string.pref_key_pattern);
			result.patternStealth = prefs
					.getBoolean(R.string.pref_key_pattern_stealth);
			result.patternHideError = prefs
					.getBoolean(R.string.pref_key_pattern_hide_error);
			//
			// // Pro & pattern only
			if (pro) {
				result.patternColor = prefs
						.getString(R.string.pref_key_pattern_color);
			}
		}
		return result;
	}
}
