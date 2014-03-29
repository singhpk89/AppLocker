package com.twinone.locker.appselect;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class PackageUtil {
	private Context mContext;

	public PackageUtil(Context c) {
		mContext = c;
	}

	public ResolveInfo getIncomingCallResolveInfo() {
		final PackageManager pm = mContext.getPackageManager();
		final Intent i = new Intent(Intent.ACTION_ANSWER);
		final List<ResolveInfo> list = pm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

}
