package com.twinone.locker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AppAdapter extends BaseAdapter {

	/** The layout for each item */
	private static final int ITEM_LAYOUT = R.layout.applist_item;

	private LayoutInflater mInflater;
	private PackageManager mPm;
	private List<String> mApps;
	private List<String> mLockedApps;

	public AppAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPm = context.getPackageManager();
		// Get all apps
		Set<String> apps = new HashSet<String>();
		final Intent i = new Intent(Intent.ACTION_MAIN, null);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> appList = context.getPackageManager()
				.queryIntentActivities(i, 0);
		for (ResolveInfo ri : appList) {
			apps.add(ri.activityInfo.packageName);
		}
		mApps = new ArrayList<String>(apps);
		
		// Get locked apps (in prederences)
		// TODO
	}

	@Override
	public int getCount() {
		return mApps.size();
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int arg0, View convertView, ViewGroup parent) {
		return createViewFromResource(arg0, convertView, parent, ITEM_LAYOUT);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		ImageView icon = (ImageView) view.findViewById(R.id.listIcon);
		TextView name = (TextView) view.findViewById(R.id.listName);
		ToggleButton lock = (ToggleButton) view.findViewById(R.id.listLocked);

		return view;
	}

}
