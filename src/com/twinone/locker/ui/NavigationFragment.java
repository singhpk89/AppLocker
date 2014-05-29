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

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.R;

public class NavigationFragment extends Fragment implements
		View.OnClickListener {
	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the
	 * user manually expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private NavigationListener mListener;

	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	private ListView mListView;
	private View mFragmentView;

	private Button bRate;
	private Button bShare;

	private boolean isOpen;
	private int mCurrentSelectedPosition = -1;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

	private NavigationAdapter mAdapter;

	public NavigationAdapter getAdapter() {
		return mAdapter;
	}

	public NavigationFragment() {
	}

	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated
		// awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		mAdapter = new NavigationAdapter(getActivity());

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState
					.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		} else {
			// Select default fragment
			mCurrentSelectedPosition = mAdapter
					.getPositionFor(NavigationElement.TYPE_APPS);
		}

		// Select either the default item (0) or the last selected item.
		selectItem(mCurrentSelectedPosition);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_nav, container, false);

		bRate = (Button) root.findViewById(R.id.nav_b_rate);
		bRate.setOnClickListener(this);
		bShare = (Button) root.findViewById(R.id.nav_b_share);
		bShare.setOnClickListener(this);

		mListView = (ListView) root.findViewById(R.id.nav_list);
		// mListView = (ListView) inflater.inflate(
		// R.layout.fragment_navigation_drawer, container, false);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectItem(position);
			}
		});
		mListView.setAdapter(mAdapter);
		mListView.setItemChecked(mCurrentSelectedPosition, true);
		return root;
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(mFragmentView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation
	 * drawer interactions.
	 * 
	 * @param fragmentId
	 *            The android:id of this fragment in its activity's layout.
	 * @param drawerLayout
	 *            The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		hackDrawerLayoutWidth();

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, 0, 0) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				isOpen = false;
				if (!isAdded()) {
					return;
				}
				getActivity().supportInvalidateOptionsMenu(); // calls
																// onPrepareOptionsMenu()
				mListener.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				isOpen = true;
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to
					// prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager
							.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true)
							.commit();
				}

				getActivity().supportInvalidateOptionsMenu(); // calls
																// onPrepareOptionsMenu()
				mListener.onDrawerOpened(drawerView);
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce
		// them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void selectItem(int position) {
		mCurrentSelectedPosition = position;
		if (mListView != null) {
			mListView.setItemChecked(position, true);
		}
		boolean close = true;
		if (mListener != null) {
			close = mListener
					.onNavigationElementSelected(((NavigationElement) mAdapter
							.getItem(position)).type);
		}
		if (mDrawerLayout != null && close) {
			mDrawerLayout.closeDrawer(mFragmentView);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (NavigationListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationListener.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar.
		// See also
		// showGlobalContextActionBar, which controls the top-left area of the
		// action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.global, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Allow menu button to toggle navigation drawer
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to
	 * show the global app 'context', rather than just what's in the current
	 * screen.
	 */
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.application_name);
	}

	private ActionBar getActionBar() {
		return ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must
	 * implement.
	 */
	public static interface NavigationListener {
		/**
		 * Called when an item in the navigation drawer is selected.
		 * 
		 * @return True if the navigation drawer should hide.
		 */

		boolean onNavigationElementSelected(int type);

		void onDrawerOpened(View drawerView);

		void onDrawerClosed(View drawerView);

		void onShareButton();

		void onRateButton();

	}

	public void open() {
		mDrawerLayout.openDrawer(mFragmentView);
	}

	public void close() {
		mDrawerLayout.closeDrawer(mFragmentView);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_b_rate:
			if (mListener != null)
				mListener.onRateButton();
			break;
		case R.id.nav_b_share:
			if (mListener != null)
				mListener.onShareButton();
			break;
		}
	}

	// Source:
	// http://stackoverflow.com/questions/16988597/set-drag-margin-for-android-navigation-drawer/17802569#17802569
	private void hackDrawerLayoutWidth() {
		try {
			Field mDragger = mDrawerLayout.getClass().getDeclaredField(
					"mLeftDragger");// mRightDragger for right obviously
			mDragger.setAccessible(true);
			ViewDragHelper draggerObj = (ViewDragHelper) mDragger
					.get(mDrawerLayout);

			Field mEdgeSize = draggerObj.getClass().getDeclaredField(
					"mEdgeSize");
			mEdgeSize.setAccessible(true);
			int edge = mEdgeSize.getInt(draggerObj);

			// dp
			mEdgeSize.setInt(draggerObj, edge * 5);
		} catch (Exception e) {
		}
	}

}
