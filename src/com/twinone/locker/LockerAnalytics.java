package com.twinone.locker;

public abstract class LockerAnalytics {

	private static final String ANALYTICS_PRD = "https://twinone.org/apps/locker/dbg-analytics.php";
	private static final String ANALYTICS_DBG = "https://twinone.org/apps/locker/analytics.php";
	// The default url to send analytics to the server. This can be changed from
	// the server itself
	public static final String URL = Constants.DEBUG ? ANALYTICS_DBG
			: ANALYTICS_PRD;

	// MainActivity
	public static final String SHARE = "main_shared";
	public static final String RATE = "main_rated";

	// How many times the user unlocked an app, entered a wrong pass, and
	// canceled
	public static final String UNLOCK_SUCCESS = "unlock_success";
	public static final String UNLOCK_ERROR = "unlock_error";
	public static final String UNLOCK_CANCEL = "unlock_cancel";

	public static final String PRO_TYPE = "pro_type";
	public static final String LOCKED_APPS_COUNT = "locked_apps_count";

	// activities
	public static final String OPEN_MAIN = "open_main";

	public static final String SERVICE_START = "service_start";
	public static final String SERVICE_STOP = "service_stop";

	// In inch, float
	public static final String FINGER_DISTANCE = "finger_distance";

	// In ms (long)
	public static final String TIME_SPENT_INTERACTING = "time_spent_interacting";

	// In ms (long)
	public static final String TIME_SPENT_IN_LOCKSCREEN = "time_spent_in_lockscreen";

}
