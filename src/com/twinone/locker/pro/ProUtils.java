package com.twinone.locker.pro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.twinone.locker.R;

public class ProUtils {
	private static final String PREFS_FILENAME = "com.twinone.locker.pro";
	/** One of PRO_TYPE_ADS, PRO_TYPE_FREE or PRO_TYPE_PAID */
	private static final String KEY_PRO_ENABLED = "com.twinone.locker.pro_enabled";

	public static final int TYPE_FREE = 0x221200ff;
	public static final int TYPE_ADS = 0x1110af;
	public static final int TYPE_PAID = 0x2038ffdf;

	private Context mContext;

	public ProUtils(Context c) {
		mContext = c;
	}

	/**
	 * @return true if the current pro setting is one of {@link #TYPE_ADS}
	 *         or {@link #TYPE_PAID}
	 */
	public boolean proFeaturesEnabled() {
		final int type = getProType();
		return type == TYPE_ADS || (type == TYPE_PAID && validatePro());
	}

	/**
	 * Use this method to validate in app purchases from GPlay
	 * 
	 * @return true if the pro key from Google Play was correct
	 */
	public boolean validatePro() {
		return false;
	}

	/**
	 * 
	 * @return true if ads mode is enabled
	 */
	public boolean showAds() {
		return getProType() == TYPE_ADS;
	}

	/**
	 * 
	 * @return one of {@link #TYPE_ADS} {@link #TYPE_FREE} or
	 *         {@link #TYPE_PAID}
	 */
	public int getProType() {
		return prefs().getInt(KEY_PRO_ENABLED, TYPE_FREE);
	}

	/**
	 * @param value
	 *            Use one of {@link #TYPE_ADS} {@link #TYPE_FREE} or
	 *            {@link #TYPE_PAID}
	 */
	@SuppressLint("CommitPrefEdits")
	public void setProType(int value) {
		SharedPreferences.Editor edit = prefs().edit().putInt(KEY_PRO_ENABLED,
				value);
		applyCompat(edit);
	}

	private SharedPreferences prefs() {
		return mContext.getSharedPreferences(PREFS_FILENAME,
				Context.MODE_PRIVATE);
	}

	public AlertDialog getProRequiredDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setMessage(R.string.pro_required);
		ab.setPositiveButton(android.R.string.ok, new ToProActivityListener());
		ab.setNegativeButton(android.R.string.cancel, null);
		return ab.create();
	}

	/** When the user clicks this button he will be sent to the play store */
	private class ToProActivityListener implements OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			toProActivity();
		}
	}

	/** Show the dialog for Pro options if pro features are not enabled */
	public void showProDialogIfNotEnabled() {
		if (!proFeaturesEnabled()) {
			getProRequiredDialog().show();
		}
	}

	private void toProActivity() {
		Intent intent = new Intent(mContext, ProActivity.class);
		// allow from service
		if (!(mContext instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		mContext.startActivity(intent);
	}

	@SuppressLint("NewApi")
	private static void applyCompat(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
}
