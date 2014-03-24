package com.twinone.locker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.AppAdapter.AppHolder;
import com.twinone.locker.lock.AlarmService;
import com.twinone.locker.util.PrefUtil;

public class SelectActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	private ListView mListView;
	private AppAdapter mAppAdapter;
	private Button bAll;
	private Button bNone;
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

		bAll = (Button) findViewById(R.id.bLockAll);
		bAll.setOnClickListener(this);
		bNone = (Button) findViewById(R.id.bUnlockAll);
		bNone.setOnClickListener(this);
		bFinish = (Button) findViewById(R.id.bFinish);
		bFinish.setOnClickListener(this);

		mEditor = PrefUtil.appsPrefs(this).edit();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppHolder ah = (AppHolder) mAppAdapter.getItem(position);
		setTracking(!ah.tracked, ah.ri.activityInfo.packageName);

		// Move to top when re-enter
		mAppAdapter.updateLockSwitches(this);

		// Dynamic move to top
		// mAppAdapter.loadAppsIntoList(this);
		// mAppAdapter.sort();

		mAppAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bLockAll:
			setAllTracking(true);
			break;
		case R.id.bUnlockAll:
			setAllTracking(false);
			break;
		case R.id.bFinish:
			Log.d("SelectActivity", "finishing...");
			AlarmService.restart(this);
			PrefUtil.apply(mEditor);
			MainActivity.showWithoutPassword(this);
			break;
		}
	}

	private void setAllTracking(boolean track) {
		ArrayList<String> apps = new ArrayList<String>();
		for (AppHolder ah : mAppAdapter.getAllItems()) {
			apps.add(ah.ri.activityInfo.packageName);
		}
		setTracking(track, apps.toArray(new String[apps.size()]));

		mAppAdapter.loadAppsIntoList(this);
		mAppAdapter.sort();
		mAppAdapter.notifyDataSetChanged();
	}

	public final void setTracking(boolean shouldTrack, String... packageNames) {
		for (String packageName : packageNames) {
			if (shouldTrack) {
				mEditor.putBoolean(packageName, true);
			} else {
				mEditor.remove(packageName);
			}
		}
		PrefUtil.apply(mEditor);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

}
