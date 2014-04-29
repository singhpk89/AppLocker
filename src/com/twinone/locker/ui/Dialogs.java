/*
 * Copyright 2014 Luuk Willemsen (Twinone)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.twinone.locker.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;

import com.twinone.locker.R;
import com.twinone.locker.lock.LockService;
import com.twinone.locker.util.PrefUtils;
import com.twinone.util.DialogSequencer;

public class Dialogs {

	/**
	 * The dialog that allows the user to select between password and pattern
	 * options
	 * 
	 * @param c
	 * @return
	 */
	public static AlertDialog getChangePasswordDialog(final Context c) {
		final AlertDialog.Builder choose = new AlertDialog.Builder(c);
		choose.setTitle(R.string.old_main_choose_lock_type);
		choose.setItems(R.array.lock_type_names, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int lockType = which == 0 ? LockService.LOCK_TYPE_PASSWORD
						: LockService.LOCK_TYPE_PATTERN;
				Intent i = LockService.getDefaultIntent(c);
				i.setAction(LockService.ACTION_CREATE);
				i.putExtra(LockService.EXTRA_VIEW_TYPE, lockType);
				c.startService(i);
			}
		});
		return choose.create();
	}

	// private void showVersionDialogs() {
	// if (mVersionManager.isDeprecated()) {
	// new VersionUtils(this).getDeprecatedDialog().show();
	// } else if (mVersionManager.shouldWarn()) {
	// new VersionUtils(this).getUpdateAvailableDialog().show();
	// }
	// }

	/**
	 * 
	 * @param c
	 * @param ds
	 * @return True if the dialog was added
	 */
	public static boolean addEmptyPasswordDialog(Context c,
			final DialogSequencer ds) {
		final boolean empty = PrefUtils.isCurrentPasswordEmpty(c);
		if (empty) {
			ds.addDialog(getChangePasswordDialog(c));
			return true;
		}
		return false;
	}

	// private static AlertDialog getEmptyPasswordDialog(Context c,
	// final DialogSequencer ds) {
	//
	// final AlertDialog.Builder msg = new AlertDialog.Builder(c);
	// msg.setTitle(R.string.main_setup);
	// msg.setMessage(R.string.main_no_password);
	// msg.setCancelable(false);
	// msg.setPositiveButton(android.R.string.ok, null);
	// msg.setNegativeButton(android.R.string.cancel, new OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// ds.removeNext(dialog);
	// }
	// });
	// return msg.create();
	// }

	public static AlertDialog getRecoveryCodeDialog(final Context c) {
		if (PrefUtils.getRecoveryCode(c) != null) {
			return null;
		}
		String code = PrefUtils.getRecoveryCode(c);
		if (code == null) {
			code = PrefUtils.generateRecoveryCode(c);
			// save it directly to avoid it to change
			final SharedPreferences.Editor editor = PrefUtils.prefs(c).edit();
			editor.putString(c.getString(R.string.pref_key_recovery_code), code);
			PrefUtils.apply(editor);
		}
		final String finalcode = code;
		AlertDialog.Builder ab = new AlertDialog.Builder(c);
		ab.setCancelable(false);
		ab.setNeutralButton(R.string.recovery_code_send_button,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(
								android.content.Intent.ACTION_SEND);
						i.setType("text/plain");
						i.putExtra(Intent.EXTRA_TEXT, "Locker: " + finalcode);
						c.startActivity(Intent.createChooser(i,
								c.getString(R.string.main_share_tit)));
					}
				});
		ab.setPositiveButton(android.R.string.ok, null);
		ab.setTitle(R.string.recovery_tit);
		ab.setMessage(String.format(c.getString(R.string.recovery_dlgmsg),
				finalcode));
		return ab.create();
	}

}
