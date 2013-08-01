package com.twinone.locker;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public abstract class Util {
	/**
	 * Gets an {@link ApplicationInfo} object for a package name
	 * 
	 * @param packageName
	 * @param pm
	 * @return The {@link ApplicationInfo} or null if not found
	 */
	public static final ApplicationInfo getAI(String packageName,
			PackageManager pm) {
		ApplicationInfo ai = null;
		try {
			ai = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
		}
		return ai;
	}
}
