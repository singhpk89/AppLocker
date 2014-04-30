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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.twinone.locker.R;
import com.twinone.locker.appselect.AppAdapter;
import com.twinone.locker.appselect.AppListElement;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.util.PrefUtils;

public class AppsFragment extends Fragment implements OnItemClickListener {
	private ListView mListView;
	private AppAdapter mAdapter;
	// private boolean mAllLocked;
	private SharedPreferences.Editor mEditor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mListView = (ListView) inflater.inflate(R.layout.fragment_applist,
				container, false);
		mAdapter = new AppAdapter(getActivity());

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		mEditor = PrefUtils.appsPrefs(getActivity()).edit();
		getActivity().setTitle(R.string.fragment_title_apps);
		return mListView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d("", "itemclick");
		AppListElement item = (AppListElement) mAdapter.getItem(position);
		if (item.isApp()) {
			item.locked = !item.locked;

			setLocked(item.locked, item.packageName);
			final ImageView image = (ImageView) view
					.findViewById(R.id.applist_item_image);
			image.setVisibility(item.locked ? View.VISIBLE : View.GONE);
		}
		updateLayout();
	}

	private void updateLayout() {
		if (mAdapter.areAllAppsLocked()) {

		} else {

		}
	}

	// TODO add a button for this
	public// private
	void setAllLocked(boolean lock) {
		ArrayList<String> apps = new ArrayList<String>();
		for (AppListElement ah : mAdapter.getAllItems()) {
			if (ah.isApp())
				apps.add(ah.packageName);
		}
		setLocked(lock, apps.toArray(new String[apps.size()]));

		mAdapter.loadAppsIntoList();
		mAdapter.sort();
		mAdapter.notifyDataSetChanged();
	}

	private void setLocked(boolean shouldTrack, String... packageNames) {
		Log.d("", "setLocked");
		for (String packageName : packageNames) {
			if (shouldTrack) {
				mEditor.putBoolean(packageName, true);
			} else {
				mEditor.remove(packageName);
			}
		}
		PrefUtils.apply(mEditor);
		AppLockService.restart(getActivity());
	}

}
