package com.twinone.locker.version;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.twinone.locker.R;
import com.twinone.locker.ui.MainActivity;

public class VersionUtils {
	private final Context mContext;

	public VersionUtils(Context c) {
		mContext = c;
	}

	public AlertDialog getUpdateAvailableDialog() {
		int days = new VersionManager(mContext).getDaysLeft();
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setTitle(R.string.update_available);
		ab.setMessage(mContext.getString(R.string.update_available_msg, days));
		ab.setPositiveButton(R.string.update_button, new ToPlayStoreListener());
		ab.setNegativeButton(R.string.update_button_cancel, null);
		return ab.create();
	}

	public Notification showUpdateAvailableNotification() {
		Intent i = new Intent(mContext, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
		String title = mContext.getString(R.string.update_available);
		String content = mContext.getString(R.string.update_available_msg);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setContentTitle(title);
		nb.setContentText(content);
		nb.setWhen(System.currentTimeMillis());
		nb.setContentIntent(pi);
		nb.setOngoing(false);

		return nb.build();
	}

	public AlertDialog getDeprecatedDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
		ab.setTitle(R.string.update_needed);
		ab.setCancelable(false);
		ab.setMessage(R.string.update_needed_msg);
		ab.setPositiveButton(R.string.update_button, new ToPlayStoreListener());
		return ab.create();
	}

	public void showDeprecatedNotification() {
		Intent i = new Intent(mContext, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
		String title = mContext.getString(R.string.update_needed);
		String content = mContext.getString(R.string.update_needed_msg);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setContentTitle(title);
		nb.setContentText(content);
		nb.setWhen(System.currentTimeMillis());
		nb.setContentIntent(pi);
		nb.setOngoing(false);
		NotificationManager nm = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(13011, nb.build());
	}

	/** When the user clicks this button he will be sent to the play store */
	private class ToPlayStoreListener implements OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			toPlayStore();
		}
	}

	private void toPlayStore() {
		String str = "https://play.google.com/store/apps/details?id="
				+ mContext.getPackageName();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
		mContext.startActivity(intent);
	}

}
