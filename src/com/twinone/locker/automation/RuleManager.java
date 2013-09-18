package com.twinone.locker.automation;

import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class RuleManager {

	public static final String PREF_FILE_RULES = "com.twinone.locker.prefs.rules";

	public class Action {
		public static final String START_SERVICE = "action_start_service";
		public static final String STOP_SERVICE = "action_stop_service";
	}

	public class Rule {
		public final Action action;
		public final Event event;

		public Rule(Event event, Action action) {
			this.event = event;
			this.action = action;
		}
	}

	private static final SharedPreferences getPrefs(Context c) {
		return c.getSharedPreferences(PREF_FILE_RULES, Context.MODE_PRIVATE);
	}

	public static final Map<String, ?> getRules(Context c) {
		return getRules(getPrefs(c), c);
	}

	public static final Map<String, ?> getRules(SharedPreferences sp, Context c) {
		return sp.getAll();
	}

	public static final boolean setRules(Context c, Map<String, String> values) {
		return setRules(getPrefs(c), c, values);
	}

	public static final boolean setRules(SharedPreferences sp, Context c,
			Map<String, String> values) {
		SharedPreferences.Editor editor = sp.edit();
		Iterator<Map.Entry<String, String>> it = values.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it
					.next();
			editor.putString(entry.getKey(), entry.getValue());
		}
		return editor.commit();
	}
}
