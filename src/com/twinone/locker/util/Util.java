package com.twinone.locker.util;

import android.content.SharedPreferences.Editor;
import android.os.Build;

public abstract class Util {

	public static void applyCompat(Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

}
