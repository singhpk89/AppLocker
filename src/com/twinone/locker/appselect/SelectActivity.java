package com.twinone.locker.appselect;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.MainActivity;
import com.twinone.locker.R;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.util.PrefUtil;

public class SelectActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	private ListView mListView;
	private AppAdapter mAppAdapter;
	private boolean mAllLocked;
	private Button bToggleAll;
	private Button bFinish;
	private SharedPreferences.Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applist);

		mListView = (ListView) findViewById(R.id.lvAppList);
		mAppAdapter = new AppAdapter(this);
		mListView.setAdapter(mAppAdapter);
		mListView.setOnItemClickListener(this);

		bToggleAll = (Button) findViewById(R.id.bToggleAll);
		bToggleAll.setOnClickListener(this);
		bFinish = (Button) findViewById(R.id.bFinish);
		bFinish.setOnClickListener(this);

		mEditor = PrefUtil.appsPrefs(this).edit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppLockService.restart(this);
		finish();
		MainActivity.showWithoutPassword(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppInfo sih = (AppInfo) mAppAdapter.getItem(position);
		if (sih.isApp()) {
			setLocked(!sih.locked, sih.packageName);
		}
		// Move to top when re-enter
		mAppAdapter.updateLockSwitches();

		// Dynamic move to top
		// mAppAdapter.loadAppsIntoList(this);
		// mAppAdapter.sort();

		mAppAdapter.notifyDataSetChanged();
		updateLayout();
	}

	private void updateLayout() {
		mAllLocked = mAppAdapter.areAllAppsLocked();
		bToggleAll.setText(mAllLocked ? R.string.applist_unlock_all
				: R.string.applist_lock_all);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bToggleAll:
			setAllTracking(!mAllLocked);
			updateLayout();
			break;
		case R.id.bFinish:
			MainActivity.showWithoutPassword(this);
			break;
		}
	}

	private void setAllTracking(boolean track) {
		ArrayList<String> apps = new ArrayList<String>();
		for (AppInfo ah : mAppAdapter.getAllItems()) {
			apps.add(ah.packageName);
		}
		setLocked(track, apps.toArray(new String[apps.size()]));

		mAppAdapter.loadAppsIntoList();
		mAppAdapter.sort();
		mAppAdapter.notifyDataSetChanged();
	}

	public final void setLocked(boolean shouldTrack, String... packageNames) {
		for (String packageName : packageNames) {
			if (shouldTrack) {
				mEditor.putBoolean(packageName, true);
			} else {
				mEditor.remove(packageName);
			}
		}
		PrefUtil.apply(mEditor);
	}

}
