package org.twinone.locker.version;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Manages versions of your application.<br>
 * It uses versionCode in AndroidManifest.xml
 * <p/>
 * <br>
 * Requires permissions:<br>
 * INTERNET<br>
 *
 * @author twinone
 */
public class VersionManager {

    private static final String TAG = "VersionManager";
    private static final String PREFS_FILENAME = "com.twinone.update";

    private static final String PREFS_VERSION_MATCHED = "com.twinone.update.version";
    /**
     * This version is OR WILL BE deprecated
     */
    private static final String PREFS_DEPRECATED = "com.twinone.update.deprecated";
    /**
     * This version's deprecation time
     */
    private static final String PREFS_DEPRECATION_TIME = "com.twinone.update.deprecation_time";
    /**
     * If server time >= warning time, the user should get warned
     */
    private static final String PREFS_WARN_TIME = "com.twinone.update.warn_before_time";
    /**
     * Current server time
     */
    private static final String PREFS_SERVER_TIME = "com.twinone.update.server_time";
    /**
     * Custom prefix to be added to user-defined objects
     */
    private static final String PREFS_VALUES_PREFIX = "com.twinone.update.values.custom.";

    // Prefs in persistent file will not be wiped when a new version is applied
    // from server
    private static final String PERSISTENT_FILENAME = "com.twinone.update.pers";
    /**
     * The url to query for version info
     */
    private static final String PREFS_URL = "com.twinone.update.url";

    /**
     * This indicates the old version for #isJustUpdated()
     */
    private static final String PREFS_OLD_VERSION = "com.twinone.update.values.old_version";

    private final Context mContext;

    public VersionManager(Context c) {
        mContext = c;
    }

    /**
     * Gets the versionCode from AndroidManifest.xml<br>
     * This is the current version installed on the device.
     *
     * @return
     */
    final int getManifestVersion() {
        int ver = 0;
        try {
            ver = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
        }
        return ver;
    }

    public static interface VersionListener {
        public void onServerResponse();
    }

    public void queryServer() {
        queryServer(null);
    }

    /**
     * Primary method that should be called when you want to know something
     * about the device's version (async, so a listener is needed)
     */
    void queryServer(VersionListener listener) {
        Uri url = getUrl();
        if (url == null) {
            Log.w(TAG, "You should provide a URL with setUrlOnce()");
            return;
        }
        new LoadVersionsTask(listener).execute(url);
    }

    /**
     * Call this method in a onCreate or so<br>
     * <p/>
     * This will set the default url of your app if it's not yet set. The url
     * can be also changed from the server without needing to update the app
     * itself. use the new_url parameter for it<br>
     * It will also update the url when this app's version is newer as the
     * stored version Note: ?v=versionCode will be appended to the Url, so the
     * PHP backend can return different items based on the version of this
     * installation. Please configure your server accordingly
     */
    @SuppressLint("CommitPrefEdits")
    public void setUrlOnce(String url) {
        // update when different manifest versions or when there is no url set
        // yet
        if (getPrefsVersion() != getManifestVersion() || getUrl() == null) {
            setUrl(url);
        }
    }

    /**
     * Return the current URL, or null if it was not yet set.<br>
     * This will also append the ?v=versionCode to the URL
     */
    private Uri getUrl() {
        try {
            String url = mContext.getSharedPreferences(PERSISTENT_FILENAME,
                    Context.MODE_PRIVATE).getString(PREFS_URL, null);
            Uri.Builder ub = Uri.parse(url).buildUpon();
            int manifestVersion = getManifestVersion();
            String mVersion = String.valueOf(manifestVersion);
            ub.appendQueryParameter("v", mVersion);

            return ub.build();
        } catch (Exception e) {
            Log.w(TAG, "Error parsing url");
            return null;
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void setUrl(String newUrl) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(
                PERSISTENT_FILENAME, Context.MODE_PRIVATE).edit();
        editor.putString(PREFS_URL, newUrl);
        applyCompat(editor);
    }

    private int getPrefsVersion() {
        return mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE).getInt(PREFS_VERSION_MATCHED, 0);
    }

    private class LoadVersionsTask extends AsyncTask<Uri, Void, VersionInfo> {

        private final VersionListener mListener;

        public LoadVersionsTask(VersionListener listener) {
            mListener = listener;
        }

        @Override
        protected VersionInfo doInBackground(Uri... params) {
            final Uri url = params[0];
            return queryServerImpl(url);
        }

        @Override
        protected void onPostExecute(VersionInfo result) {
            if (result != null) {
                saveToStorage(result);
                if (mListener != null) {
                    mListener.onServerResponse();
                }
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private void saveToStorage(VersionInfo vi) {
        // Get if this version is deprecated, and if it is, the most restrictive
        // deprecation time
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // save the url!
        editor.clear();

        editor.putInt(PREFS_VERSION_MATCHED, getManifestVersion());
        if (vi.deprecated != null)
            editor.putBoolean(PREFS_DEPRECATED, vi.deprecated);
        if (vi.deprecation_time != null)
            editor.putLong(PREFS_DEPRECATION_TIME, vi.deprecation_time);
        if (vi.warn_time != null)
            editor.putLong(PREFS_WARN_TIME, vi.warn_time);
        if (vi.server_time != null)
            editor.putLong(PREFS_SERVER_TIME, vi.server_time);
        if (vi.values != null) {
            for (Map.Entry<String, String> entry : vi.values.entrySet()) {
                String key = PREFS_VALUES_PREFIX + entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null)
                    editor.putString(key, value);
            }
        }

        if (vi.new_url != null)
            setUrl(vi.new_url);

        applyCompat(editor);
    }

    /**
     * If this is true, you should show a dialog warning the user that he has
     * only some days left to update
     */
    public boolean shouldWarn() {
        if (getPrefsVersion() == getManifestVersion()) {
            if (isMarkedForDeprecation() && !isDeprecated()) {
                SharedPreferences sp = mContext.getSharedPreferences(
                        PREFS_FILENAME, Context.MODE_PRIVATE);
                long warnTime = sp.getLong(PREFS_WARN_TIME, 0);
                long serverTime = sp.getLong(PREFS_SERVER_TIME, 0);
                if (warnTime != 0 && serverTime != 0 && serverTime >= warnTime) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a custom value that was sent from the server in
     * {@link VersionInfo#values} or defValue if the value was not received
     */
    public final String getValue(String key, String defValue) {
        // don't interfeare with older versions
        if (getPrefsVersion() != getManifestVersion()) {
            return defValue;
        }
        return mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE).getString(PREFS_VALUES_PREFIX + key,
                defValue);
    }

    @SuppressLint("NewApi")
    private void applyCompat(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * Returns true if this version is deprecated, independently of whether the
     * deprecation time is before or after current server time
     */
    boolean isMarkedForDeprecation() {
        if (getPrefsVersion() != getManifestVersion()) {
            return false;
        }
        return mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE).getBoolean(PREFS_DEPRECATED, false);
    }

    /**
     * Number of full days the user can still use the app before it will become
     * unusable<br>
     * returns negative value if there are no days left, so check with
     * <br>
     * returns -1 if not applicable
     */
    public int getDaysLeft() {
        if (getPrefsVersion() != getManifestVersion()) {
            return -1;
        }
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE);
        long server_time = sp.getLong(PREFS_SERVER_TIME, 0);
        long deprecation_time = sp.getLong(PREFS_DEPRECATION_TIME, 0);
        if (server_time == 0 || deprecation_time == 0)
            return -1;
        return (int) (deprecation_time - server_time) / 86400;
    }

    /**
     * This app is marked for deprecation and the server time >= deprecation
     * time
     */
    private static final int STATUS_DEPRECATED = -1;
    /**
     * This version has been marked for deprecation but there is still time left
     * to use this version
     */
    private static final int STATUS_MARKED_FOR_DEPRECATION = -2;
    /**
     * This version is not marked for deprecation
     */
    private static final int STATUS_NOT_DEPRECATED = -3;

    /**
     * Returns one of {@link #STATUS_DEPRECATED},
     * {@link #STATUS_MARKED_FOR_DEPRECATION} or {@link #STATUS_NOT_DEPRECATED}
     */
    int getDeprecationStatus() {
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_FILENAME,
                Context.MODE_PRIVATE);
        int matchedVersion = sp.getInt(PREFS_VERSION_MATCHED, 0);
        int manifestVersion = getManifestVersion();

        // avoid false positives after updating the app
        if (matchedVersion == manifestVersion) {
            boolean deprecated = sp.getBoolean(PREFS_DEPRECATED, false);
            long server_time = sp.getLong(PREFS_SERVER_TIME, 0);
            long deprecation_time = sp.getLong(PREFS_DEPRECATION_TIME, 0);
            if (deprecated && server_time != 0 && deprecation_time != 0) {
                return server_time >= deprecation_time ? STATUS_DEPRECATED
                        : STATUS_MARKED_FOR_DEPRECATION;
            }
        }
        return STATUS_NOT_DEPRECATED;
    }

    /**
     * @return true if  =
     * {@link #STATUS_DEPRECATED}
     */
    public boolean isDeprecated() {
        return getDeprecationStatus() == STATUS_DEPRECATED;
    }

    /**
     * Does the loading (blocks until done)
     *
     * @param uri
     * @return
     */
    private VersionInfo queryServerImpl(Uri uri) {
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
            Gson gson = new Gson();
            VersionInfo vi = gson.fromJson(data.toString(), VersionInfo.class);
            Log.d(TAG, "Succesful query to server");
            return vi;
        } catch (Exception e) {
            Log.w(TAG, "Query to server failed" + e.getMessage());
            return null;
        }
    }

    /**
     * This will return true once after every upgrade, returns false at any
     * other time
     */
    @SuppressLint("CommitPrefEdits")
    public boolean isJustUpgraded() {
        SharedPreferences sp = mContext.getSharedPreferences(
                PERSISTENT_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int manifestVersion = getManifestVersion();
        int storedVersion = sp.getInt(PREFS_OLD_VERSION, 0);
        Log.d(TAG, "Comparing versions: stored: " + storedVersion
                + " manifest: " + manifestVersion);
        editor.putInt(PREFS_OLD_VERSION, manifestVersion);
        applyCompat(editor);
        return storedVersion != manifestVersion;
    }

    public static String getUniqueDeviceId() {
        return Settings.Secure.ANDROID_ID;
    }
}
