package com.twinone.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

import com.twinone.locker.MainActivity;

public class Analytics {

	public static final String PREF_ANALYTICS_FILE = "com.twinone.analytics";
	public static final String PREF_PERSISTENT_FILE = "com.twinone.analytics.prefs";
	public static final String PREF_KEY_ENABLE_ANALYTICS = "_ALLOW_ANALYTICS_SEND_USAGE_STATISTICS";

	public static final String PREF_KEY_ANALYTICS_URL = "com.twinone.analytics.url";
	public static final String TAG = "Analytics";

	public static final String ANALYTIC_UID = "uid";

	// private Context mContext;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEditor;
	private final boolean mAutoSave = true;
	private final boolean mEnableAnalytics;
	private Context mContext;

	public Analytics(Context c) {
		mContext = c;
		mPrefs = c.getSharedPreferences(PREF_ANALYTICS_FILE,
				Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
		mEnableAnalytics = getEnableAnalytics();
		// TODO
		if (!mEnableAnalytics) {
			mPrefs = null;
			mEditor = null;
		}
	}

	/**
	 * Utility method for when the user decides to allow or decline analytics.
	 * You should respect the preference of the user.
	 * 
	 * @param c
	 * @param allow
	 */
	@SuppressLint("CommitPrefEdits")
	public final void setEnableAnalytics(boolean enable) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				PREF_PERSISTENT_FILE, Context.MODE_PRIVATE).edit();
		editor.putBoolean(PREF_KEY_ENABLE_ANALYTICS, enable);
		save(editor);
	}

	public boolean getEnableAnalytics() {
		final SharedPreferences prefs = mContext.getSharedPreferences(
				PREF_PERSISTENT_FILE, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREF_KEY_ENABLE_ANALYTICS, false)
				|| MainActivity.DEBUG;
	}

	public long increment(String key) {
		if (mEnableAnalytics) {
			long value = mPrefs.getLong(key, 0);
			value++;
			mEditor.putLong(key, value);
			autoSave();
			return value;
		}
		return -1;
	}

	public long decrement(String key) {
		if (mEnableAnalytics) {
			long value = mPrefs.getLong(key, 0);
			value--;
			mEditor.putLong(key, value);
			autoSave();
			return value;
		}
		return -1;
	}

	/**
	 * Returns all analytics, never null
	 * 
	 * @return
	 */
	public Map<String, String> getAll() {
		Map<String, String> result = new HashMap<String, String>();
		if (mEnableAnalytics) {
			for (Map.Entry<String, ?> e : mPrefs.getAll().entrySet()) {
				result.put(e.getKey(), String.valueOf(e.getValue()));
			}
			result.put(ANALYTIC_UID, getDeviceId());
		}
		return result;
	}

	public void setEnabled(String key, boolean enabled) {
		if (mEnableAnalytics) {
			mEditor.putBoolean(key, enabled);
			autoSave();
		}
	}

	public void putString(String key, String value) {
		if (mEnableAnalytics) {
			mEditor.putString(key, value);
			autoSave();
		}
	}

	private void autoSave() {
		if (mEnableAnalytics && mAutoSave) {
			save();
		}
	}

	public final void save() {
		save(mEditor);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static final void save(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

	public static interface AnalyticsListener {
		public void onServerResponse(String response);
	}

	public void queryServer() {
		queryServer((AnalyticsListener) null);
	}

	/**
	 * Primary method that should be called when you want to know something
	 * about the device's version (async, so a listener is needed)
	 */
	public void queryServer(AnalyticsListener listener) {
		queryServer(listener, null);
	}

	public void queryServer(Map<String, String> params) {
		queryServer(null, params);
	}

	/**
	 * 
	 * @param listener
	 * @param params
	 *            Additional parameters to be appended to the GET request
	 */
	public void queryServer(AnalyticsListener listener,
			Map<String, String> params) {
		Log.d(TAG, "queryserver!");
		if (!mEnableAnalytics) {
			Log.w(TAG, "Analytics are not enabled for this device");
			return;
		}
		Uri.Builder ub = getUrl().buildUpon();
		// Add all analytics
		Map<String, String> analytics = getAll();
		// Put params -> analytics (hashmap) to avoid duplicates
		if (params != null) {
			for (Map.Entry<String, String> e : params.entrySet()) {
				analytics.put(e.getKey(), e.getValue());
			}
		}
		for (Map.Entry<String, String> e : analytics.entrySet()) {
			ub.appendQueryParameter(e.getKey(), e.getValue());
		}

		Uri uri = ub.build();
		if (uri == null) {
			Log.w(TAG, "You should provide a URL with setUrlOnce()");
			return;
		}
		new QueryServerTask(listener).execute(uri);
	}

	private class QueryServerTask extends AsyncTask<Uri, Void, String> {

		private final AnalyticsListener mListener;

		public QueryServerTask(AnalyticsListener listener) {
			mListener = listener;
		}

		@Override
		protected String doInBackground(Uri... params) {
			Log.d(TAG, "doInBackground");
			final Uri url = params[0];
			return queryServerImpl(url);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "onPostExecut");
			if (result != null) {
				if (mListener != null) {
					mListener.onServerResponse(result);
				}
			}
		}
	}

	private String queryServerImpl(Uri uri) {
		try {
			Log.d(TAG, "Querying " + uri.toString());
			URL url = new URL(uri.toString());
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder data = new StringBuilder();
			String tmp;
			while ((tmp = br.readLine()) != null) {
				data.append(tmp);
			}
			urlConnection.disconnect();
			return data.toString();
		} catch (Exception e) {
			Log.w(TAG, "Query to server failed ", e);
			return null;
		}
	}

	@SuppressLint("CommitPrefEdits")
	public void setUrlOnce(String url) {
		// update when different manifest versions or when there is no url set
		// yet
		if (getUrl() == null) {
			setUrl(url);
		}
	}

	@SuppressLint("CommitPrefEdits")
	private void setUrl(String newUrl) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				PREF_PERSISTENT_FILE, Context.MODE_PRIVATE).edit();
		editor.putString(PREF_KEY_ANALYTICS_URL, newUrl);
		applyCompat(editor);
	}

	/**
	 * Return the current URL, or null if it was not yet set.<br>
	 * This will also append the ?v=versionCode to the URL
	 */
	private Uri getUrl() {
		try {
			String url = mContext.getSharedPreferences(PREF_PERSISTENT_FILE,
					Context.MODE_PRIVATE).getString(PREF_KEY_ANALYTICS_URL,
					null);
			return Uri.parse(url);
		} catch (Exception e) {
			return null;
		}
	}

	public String getDeviceId() {
		String id = Secure.getString(mContext.getContentResolver(),
				Secure.ANDROID_ID);
		if (id == null || id.equals("9774d56d682e549c")
				|| id.equals("0000000000000000")) {
			return "";
		}
		return id;
	}

	@SuppressLint("NewApi")
	private void applyCompat(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

}
