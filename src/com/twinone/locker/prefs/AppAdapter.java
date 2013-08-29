package com.twinone.locker.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twinone.locker.ObserverService;
import com.twinone.locker.R;

public class AppAdapter extends BaseAdapter {

	/** The layout for each item */
	private static final int ITEM_LAYOUT = R.layout.applist_item;

	private LayoutInflater mInflater;
	private PackageManager mPm;
	private List<AppHolder> mApps;
	private String mPackageName;

	public AppAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPm = context.getPackageManager();
		mPackageName = context.getApplicationInfo().packageName;
		loadAppsIntoList(context);
		Collections.sort(mApps);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	/**
	 * Creates a completely new list with the apps. Should only be called once.
	 * 
	 * @param c
	 */
	public void loadAppsIntoList(Context c) {

		// Get all tracked apps from preferences
		ArrayList<AppHolder> apps = new ArrayList<AppHolder>();

		// Get all apps
		final Intent i = new Intent(Intent.ACTION_MAIN, null);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> ris = mPm.queryIntentActivities(i, 0);

		for (ResolveInfo ri : ris) {
			if (!mPackageName.equals(ri.activityInfo.packageName)) {
				AppHolder ah = new AppHolder(ri.loadLabel(mPm).toString(), ri);
				apps.add(ah);
			}
		}
		mApps = new ArrayList<AppHolder>(apps);
		updateLockSwitches(c);
	}

	/**
	 * Updates the true or false states.
	 * 
	 * @param c
	 */
	public void updateLockSwitches(Context c) {
		for (AppHolder ah : mApps) {
			Set<String> trackedApps = ObserverService.getTrackedApps(c);
			ah.tracked = trackedApps.contains(ah.ri.activityInfo.packageName);
		}
	}

	public void sort() {
		Collections.sort(mApps);
	}

	public class AppHolder implements Comparable<AppHolder> {

		String label;
		ResolveInfo ri;
		public boolean tracked = true;

		public AppHolder(String label, ResolveInfo ri) {
			this.label = label;
			this.ri = ri;
		}

		public AppHolder(String label, ResolveInfo ri, boolean tracked) {
			this.label = label;
			this.ri = ri;
			this.tracked = tracked;
		}

		@Override
		public boolean equals(Object object) {

			if (object == null)
				return false;
			// Compare to another lockinfo
			if (object instanceof AppHolder) {
				return this.ri.activityInfo.packageName
						.equals(((AppHolder) object).ri.activityInfo.packageName);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return new StringBuilder("1191")
					.append(ri.activityInfo.packageName).toString().hashCode();
		}

		@Override
		public int compareTo(AppHolder o) {
			// If it's locked
			if (((AppHolder) o).tracked != this.tracked) {
				return this.tracked ? -1 : 1;
			}
			return this.label.compareToIgnoreCase(((AppHolder) o).label);
		}
	}

	@Override
	public int getCount() {
		return mApps.size();
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	public List<AppHolder> getAllItems() {
		return mApps;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		return createViewFromResource(arg0, convertView, parent, ITEM_LAYOUT);
	}

	@SuppressWarnings("deprecation")
	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view = convertView;
		AppHolder ah = mApps.get(position);
		// ApplicationInfo ai = Util.getAI(ah.ri.activityInfo.packageName, mPm);
		if (ah != null) {
			if (view == null) { // Execute only when a new view is created
								// (should never be called)
				view = mInflater.inflate(resource, parent, false);

			}
			// Every click
			Button lock = (Button) view.findViewById(R.id.listLocked);
			lock.setVisibility(ah.tracked ? View.VISIBLE : View.GONE);
			ImageView icon = (ImageView) view.findViewById(R.id.listIcon);
			TextView name = (TextView) view.findViewById(R.id.listName);
			// Avoid deprecation, but stay backwards compatible
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				icon.setBackgroundDrawable(ah.ri.loadIcon(mPm));
			} else {
				icon.setBackground(ah.ri.loadIcon(mPm));
			}
			name.setText(ah.label);
		}
		return view;
	}

}
