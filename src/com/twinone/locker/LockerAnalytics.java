package com.twinone.locker;

public abstract class LockerAnalytics {

	// MainActivity
	public static final String MAIN_OPENED = "main_opened";
	public static final String MAIN_START = "main_start_service";
	public static final String MAIN_SELECT_APPS = "main_select_apps";
	public static final String MAIN_LAUNCH_PREFS = "main_launch_prefs";
	public static final String MAIN_BETA = "main_beta";

	public static final String SHARE = "main_shared";
	public static final String RATE = "main_rated";

	// Locker
	/**
	 * How many times any app has been blocked
	 */
	public static final String LOCK_COUNT = "lock_count";

	/**
	 * How many times a specific package has been blocked.<br>
	 * This key should be extended with the desired package name
	 */
	public static final String COUNT_PKG = "lock_package_";

	// Other
	public static final String START_BOOT = "start_boot";
	
	public static final String AD_LOAD_SUCCEEDED = "ad_load_succeeded";
	
	public static final String AD_CLICKED = "ad_clicked";
	
}
