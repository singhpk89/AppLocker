package com.twinone.locker;

public abstract class LockerAnalytics {

	// MainActivity
	public static final String SHARE = "main_shared";
	public static final String RATE = "main_rated";

	// User enters correct password
	public static final String PASSWORD_SUCCESS = "password_success";
	// User enters incorrect password
	public static final String PASSWORD_FAILED = "password_failed";
	// User enters correct password
	public static final String PATTERN_SUCCESS = "pattern_success";
	// User enters incorrect password
	public static final String PATTERN_FAILED = "pattern_failed";
	// User cancels the lock screen (back or home)
	public static final String UNLOCK_CANCEL = "unlock_cancel";

	public static final String AD_CLICKED = "ad_clicked";

	public static final String PRO_TYPE = "pro_type";
	public static final String LOCKED_APPS_COUNT = "locked_apps_count";

	// activities
	public static final String OPEN_MAIN = "open_main";
	public static final String OPEN_SELECT = "open_apps";
	public static final String OPEN_PREFS = "open_prefs";

	public static final String SERVICE_START = "service_start";
	public static final String SERVICE_STOP = "service_stop";

}
