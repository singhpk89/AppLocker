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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.twinone.locker.Constants;
import com.twinone.locker.R;
import com.twinone.locker.lock.AppLockService;

public class NavigationAdapter extends BaseAdapter {

	// private Context mContext;
	private LayoutInflater mInflater;
	private NavigationElement[] mItems;

	private boolean mServiceRunning = false;

	public NavigationAdapter(Context context) {
		// mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setupElements();
		mServiceRunning = AppLockService.isRunning(context);
	}

	public NavigationElement getItemFor(int type) {
		return mItems[getPositionFor(type)];
	}

	public int getPositionFor(int type) {
		for (int i = 0; i < mItems.length; i++) {
			if (mItems[i].type == type) {
				return i;
			}
		}
		return -1;
	}

	public int getTypeOf(int position) {
		return mItems[position].type;
	}

	public void setServiceState(boolean newState) {
		if (mServiceRunning != newState) {
			mServiceRunning = newState;
			notifyDataSetChanged();
		}
	}

	private void setupElements() {
		NavigationElement status = new NavigationElement();
		status.title = R.string.nav_status;
		status.type = NavigationElement.TYPE_STATUS;

		final NavigationElement apps = new NavigationElement();
		apps.title = R.string.nav_apps;
		apps.type = NavigationElement.TYPE_APPS;

		final NavigationElement change = new NavigationElement();
		change.title = R.string.nav_change;
		change.type = NavigationElement.TYPE_CHANGE;

		final NavigationElement settings = new NavigationElement();
		settings.title = R.string.nav_settings;
		settings.type = NavigationElement.TYPE_SETTINGS;

		final NavigationElement statistics = new NavigationElement();
		statistics.title = R.string.nav_statistics;
		statistics.type = NavigationElement.TYPE_STATISTICS;

		final NavigationElement pro = new NavigationElement();
		pro.title = R.string.nav_pro;
		pro.type = NavigationElement.TYPE_PRO;

		// Add test is necessary
		if (Constants.DEBUG) {
			final NavigationElement test = new NavigationElement();
			test.title = R.string.nav_apps;
			test.type = NavigationElement.TYPE_TEST;
			mItems = new NavigationElement[] { status, apps, change, settings,
					statistics, pro, test };
		} else {
			mItems = new NavigationElement[] { status, apps, change, settings,
					statistics, pro };
		}
	}

	@Override
	public int getCount() {
		return mItems.length;
	}

	@Override
	public Object getItem(int position) {
		return mItems[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup root = (ViewGroup) mInflater.inflate(
				R.layout.navigation_drawer_list_item, null);

		if (mItems[position].type == NavigationElement.TYPE_STATUS) {
			final CompoundButton cb = (CompoundButton) root
					.findViewById(R.id.navFlag);
			cb.setChecked(mServiceRunning);
			cb.setVisibility(View.VISIBLE);
		}

		TextView navTitle = (TextView) root.findViewById(R.id.navTitle);
		navTitle.setText(mItems[position].title);
		return root;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static CompoundButton getSwitchCompat(Context c) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return new CheckBox(c);
		else
			return new Switch(c);
	}
}
