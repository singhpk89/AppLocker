package org.twinone.locker.ui;

import org.twinone.locker.appselect.AppAdapter;
import org.twinone.locker.appselect.AppListElement;
import org.twinone.locker.appselect.AppAdapter.OnEventListener;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.twinone.locker.R;

public class AppsFragment extends Fragment implements OnItemClickListener,
		OnEventListener {
    private AppAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        ListView mListView = (ListView) inflater.inflate(R.layout.fragment_applist,
                container, false);
		mAdapter = new AppAdapter(getActivity());
		mAdapter.setOnEventListener(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		// mUndoBar = (LinearLayout) rootView.findViewById(R.id.undo_bar);
		// mUndoButton = (Button) rootView.findViewById(R.id.undo_bar_button);
		// mUndoButton.setOnClickListener(this);

		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.fragment_title_apps);
		return mListView;
	}

	private Toast mLockedToast;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppListElement item = (AppListElement) mAdapter.getItem(position);
		if (item.isApp()) {
			mAdapter.toggle(item);
			showToastSingle(item.locked, item.title);
			// Update lock image
			view.findViewById(R.id.applist_item_image).setVisibility(
					item.locked ? View.VISIBLE : View.GONE);

			// And the menu
			updateMenuLayout();
		}
	}

	private void showToast(String text) {
		if (mLockedToast != null) {
			mLockedToast.cancel();
		}

		mLockedToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
		mLockedToast.show();
	}

	private void showToastSingle(boolean locked, String title) {
		showToast(getString(locked ? R.string.apps_toast_locked_single
				: R.string.apps_toast_unlocked_single, title));
	}

	private void showToastAll(boolean locked) {
		showToast(getString(locked ? R.string.apps_toast_locked_all
				: R.string.apps_toast_unlocked_all));
	}

	public void onSearch(String query) {
		Log.d("AppsFragment", "onSearch (query=" + query + ")");
	}

	private Menu mMenu;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.apps, menu);
		mMenu = menu;
		updateMenuLayout();

		// MenuItem mi = menu.findItem(R.id.apps_menu_search);
		// EditText test = (EditText) MenuItemCompat.getActionView(mi);

		SearchManager sm = (SearchManager) getActivity().getSystemService(
				Context.SEARCH_SERVICE);
		MenuItem mi = (MenuItem) menu.findItem(R.id.apps_menu_search);
		SearchView sv = (SearchView) MenuItemCompat.getActionView(mi);
		sv.setSearchableInfo(sm.getSearchableInfo(getActivity()
				.getComponentName()));

	}

	private void updateMenuLayout() {
		boolean all = mAdapter.areAllAppsLocked();
		if (mMenu != null && mAdapter.isLoadComplete()) {
			mMenu.findItem(R.id.apps_menu_lock_all).setVisible(!all);
			mMenu.findItem(R.id.apps_menu_unlock_all).setVisible(all);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.apps_menu_sort:
			mAdapter.sort();
			break;
		case R.id.apps_menu_lock_all:
			onLockAllOptions(true);
			break;
		case R.id.apps_menu_unlock_all:
			onLockAllOptions(false);
			break;
		case R.id.apps_menu_search:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onLockAllOptions(boolean lockall) {
		mMenu.findItem(R.id.apps_menu_lock_all).setVisible(!lockall);
		mMenu.findItem(R.id.apps_menu_unlock_all).setVisible(lockall);
		mAdapter.prepareUndo();
		mAdapter.setAllLocked(lockall);
		showToastAll(lockall);
	}

	@Override
	public void onLoadComplete() {
		updateMenuLayout();
	}

	@Override
	public void onDirtyStateChanged(boolean dirty) {
		mMenu.findItem(R.id.apps_menu_sort).setVisible(dirty);
	}

}
