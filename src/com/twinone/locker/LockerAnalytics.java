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

}
