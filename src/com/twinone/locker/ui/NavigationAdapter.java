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

import java.util.ArrayList;
import java.util.List;

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
	private List<NavigationElement> mItems;

	private boolean mServiceRunning = false;

	public NavigationAdapter(Context context) {
		// mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mServiceRunning = AppLockService.isRunning(context);
		mItems = new ArrayList<NavigationElement>();
		setupElements();
	}

	public NavigationElement getItemFor(int type) {
		return mItems.get(getPositionFor(type));
	}

	public int getPositionFor(int type) {
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i).type == type) {
				return i;
			}
		}
		return -1;
	}

	public int getTypeOf(int position) {
		return mItems.get(position).type;
	}

	public void setServiceState(boolean newState) {
		if (mServiceRunning != newState) {
			mServiceRunning = newState;
			notifyDataSetChanged();
		}
	}

	private void addElement(int title, int type) {
		final NavigationElement el = new NavigationElement();
		el.title = title;
		el.type = type;
		mItems.add(el);
	}

	private void setupElements() {
		addElement(R.string.nav_status, NavigationElement.TYPE_STATUS);
		addElement(R.string.nav_apps, NavigationElement.TYPE_APPS);
		addElement(R.string.nav_change, NavigationElement.TYPE_CHANGE);
		addElement(R.string.nav_settings, NavigationElement.TYPE_SETTINGS);
		if (Constants.DEBUG) {
			addElement(R.string.nav_statistics,
					NavigationElement.TYPE_STATISTICS);

			addElement(R.string.nav_test, NavigationElement.TYPE_TEST);
		}
		addElement(R.string.nav_pro, NavigationElement.TYPE_PRO);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup root = (ViewGroup) mInflater.inflate(
				R.layout.navigation_drawer_list_item, null);

		if (mItems.get(position).type == NavigationElement.TYPE_STATUS) {
			final CompoundButton cb = (CompoundButton) root
					.findViewById(R.id.navFlag);
			cb.setChecked(mServiceRunning);
			cb.setVisibility(View.VISIBLE);
		}

		TextView navTitle = (TextView) root.findViewById(R.id.navTitle);
		navTitle.setText(mItems.get(position).title);
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
