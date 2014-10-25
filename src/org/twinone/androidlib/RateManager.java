package org.twinone.androidlib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.twinone.locker.R;

public class RateManager {

	private static final String PREF_FILE = "org.twinone.androidlib.share";
	private static final String PREF_KEY_COUNT = "org.twinone.androidlib.rate.count";
	/**
	 * After how many times should the share dialog be shown for the first time?
	 */
	private static final String PREF_KEY_OFFSET = "org.twinone.androidlib.rate.offset";
	/** After the first time, each how many times should the dialog show? */
	private static final String PREF_KEY_REPEAT = "org.twinone.androidlib.rate.repeat";
	private static final int OFFSET_DEFAULT = 7;
	private static final int REPEAT_DEFAULT = 5;

	private static final String PREF_KEY_NEVER = "org.twinone.androidlib.rate.never";

	private static SharedPreferences getPrefs(Context c) {
		return c.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
	}

	public static void show(Context c, String text) {
		show(c, text, true);
	}

	public static void show(Context c, String text, boolean hasNeverButton) {
		int current = getPrefs(c).getInt(PREF_KEY_COUNT, 0) + 1;
		int offset = getPrefs(c).getInt(PREF_KEY_OFFSET, OFFSET_DEFAULT);
		int repeat = getPrefs(c).getInt(PREF_KEY_REPEAT, REPEAT_DEFAULT);
		boolean never = getPrefs(c).getBoolean(PREF_KEY_NEVER, false);

		if (!never) {
			if (current == offset || current > offset
					&& ((current - offset) % repeat) == 0) {
				getShareEditDialog(c, text, hasNeverButton).show();
			}
		}
		getPrefs(c).edit().putInt(PREF_KEY_COUNT, current).commit();
	}

	public static void setNever(Context c, boolean never) {
		getPrefs(c).edit().putBoolean(PREF_KEY_NEVER, never).commit();
	}

	public static AlertDialog.Builder getShareEditDialog(final Context c,
			final String promoText) {
		return getShareEditDialog(c, promoText, true);
	}

	public static AlertDialog.Builder getShareEditDialog(final Context c,
			final String promoText, boolean hasNeverButton) {
		final AlertDialog.Builder ab = new AlertDialog.Builder(c);

		ab.setCancelable(false);
		ab.setTitle(R.string.lib_rate_dlg_tit);
		ab.setMessage(R.string.lib_rate_dlg_msg);
		if (hasNeverButton) {
			ab.setNegativeButton(R.string.lib_share_never,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							setNever(c, true);
						}
					});
		}
		ab.setNeutralButton(R.string.lib_share_later, null);
		ab.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				showMarket(c);
				setNever(c, true);
			}
		});

		return ab;
	}

	private static void showMarket(Context c) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + c.getPackageName()));
		if (c.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
			c.startActivity(intent);
		}
	}
}
