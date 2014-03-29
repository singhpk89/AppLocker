package com.twinone.locker.appselect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twinone.locker.R;
import com.twinone.locker.util.PrefUtil;

public class AppAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private PackageManager mPm;
	private Context mContext;
	private List<AppInfo> mItems;

	public AppAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPm = context.getPackageManager();
		loadAppsIntoList();
		Collections.sort(mItems);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public boolean areAllAppsLocked() {
		for (AppInfo app : mItems)
			if (app.isApp() && !app.locked)
				return false;
		return true;
	}

	/**
	 * Creates a completely new list with the apps. Should only be called once.
	 * 
	 * @param mContext
	 */
	public void loadAppsIntoList() {

		// Get all tracked apps from preferences
		HashSet<AppInfo> apps = new HashSet<AppInfo>();
		addImportantAndSystemApps(apps);

		// other apps
		final Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> ris = mPm.queryIntentActivities(i, 0);

		for (ResolveInfo ri : ris) {
			if (!mContext.getPackageName().equals(ri.activityInfo.packageName)) {
				final AppInfo ah = new AppInfo(ri.loadLabel(mPm).toString(),
						ri.activityInfo, AppInfo.PRIORITY_NORMAL_APPS);
				apps.add(ah);
			}
		}
		mItems = new ArrayList<AppInfo>(apps);
		updateLockSwitches();
	}

	private void addImportantAndSystemApps(Collection<AppInfo> apps) {
		final String phone = "com.android.dialer";
		final String installer = "com.android.packageinstaller";
		final String sysui = "com.android.systemui";

		final List<String> important = Arrays.asList(new String[] {
				"com.android.vending", "com.android.settings" });

		final List<String> system = Arrays
				.asList(new String[] { "com.android.dialer" });

		final PackageManager pm = mContext.getPackageManager();
		List<ApplicationInfo> list = pm.getInstalledApplications(0);
		boolean haveSystem = false;
		boolean haveImportant = false;
		for (ApplicationInfo pi : list) {
			if (phone.equals(pi.packageName)) {
				// apps.add(new AppInfo(mContext
				// .getString(R.string.applist_app_dialer), pi,
				// AppInfo.PRIORITY_SYSTEM_APPS));
				// haveSystem = true;
			} else if (sysui.equals(pi.packageName)) {
				apps.add(new AppInfo(mContext
						.getString(R.string.applist_app_sysui), pi,
						AppInfo.PRIORITY_SYSTEM_APPS));
				haveSystem = true;
			} else if (installer.equals(pi.packageName)) {
				apps.add(new AppInfo(mContext
						.getString(R.string.applist_app_pkginstaller), pi,
						AppInfo.PRIORITY_IMPORTANT_APPS));
				haveImportant = true;
			}
			if (important.contains(pi.packageName)) {
				apps.add(new AppInfo(pi.loadLabel(pm).toString(), pi,
						AppInfo.PRIORITY_IMPORTANT_APPS));
				haveImportant = true;
			}
			if (system.contains(pi.packageName)) {
				apps.add(new AppInfo(pi.loadLabel(pm).toString(), pi,
						AppInfo.PRIORITY_SYSTEM_APPS));
				haveSystem = true;
			}

			apps.add(new AppInfo(mContext.getString(R.string.applist_tit_apps),
					AppInfo.PRIORITY_NORMAL_CATEGORY));
			if (haveImportant) {
				apps.add(new AppInfo(mContext
						.getString(R.string.applist_tit_important),
						AppInfo.PRIORITY_IMPORTANT_CATEGORY));
			}
			if (haveSystem) {
				apps.add(new AppInfo(mContext
						.getString(R.string.applist_tit_system),
						AppInfo.PRIORITY_SYSTEM_CATEGORY));
			}
		}
	}

	/**
	 * Updates the true or false states.
	 * 
	 * @param c
	 */
	public void updateLockSwitches() {
		Set<String> lockedApps = PrefUtil.getLockedApps(mContext);
		for (AppInfo ah : mItems) {
			ah.locked = lockedApps.contains(ah.packageName);
		}
	}

	public void sort() {
		Log.d("", "Sort");
		Collections.sort(mItems);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	public List<AppInfo> getAllItems() {
		return mItems;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mItems.get(position).isApp()) {
			return createAppViewFromResource(position, convertView, parent);
		} else {
			return createSeparatorViewFromResource(position, convertView,
					parent);
		}
	}

	private View createSeparatorViewFromResource(int position,
			View convertView, ViewGroup parent) {
		AppInfo ah = mItems.get(position);

		View view = mInflater.inflate(R.layout.applist_item_category, parent,
				false);
		TextView tv = (TextView) view.findViewById(R.id.listName);
		tv.setText(ah.title);

		return view;
	}

	private View createAppViewFromResource(int position, View convertView,
			ViewGroup parent) {

		AppInfo ah = mItems.get(position);
		View view = mInflater.inflate(R.layout.applist_item_app, parent, false);
		// Every click
		final ImageView lock = (ImageView) view.findViewById(R.id.listLocked);
		lock.setVisibility(ah.locked ? View.VISIBLE : View.GONE);

		final TextView name = (TextView) view.findViewById(R.id.listName);
		name.setText(ah.title);

		final ImageView icon = (ImageView) view.findViewById(R.id.listIcon);
		final Drawable bg = ah.getIcon(mPm);
		if (bg == null)
			icon.setVisibility(View.GONE);
		else
			setBackgroundCompat(icon, bg);

		return view;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundCompat(View v, Drawable bg) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			v.setBackgroundDrawable(bg);
		else
			v.setBackground(bg);
	}

}
