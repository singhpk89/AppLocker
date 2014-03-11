package com.twinone.locker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.AppAdapter.AppHolder;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.lock.AppLockService.LocalBinder;

public class SelectActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	ListView mListView;
	AppAdapter mAppAdapter;
	Button bAll;
	Button bNone;
	Button bFinish;

	private AppLockService mService;
	boolean mBound = false;

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
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent(this, AppLockService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			LocalBinder b = (LocalBinder) binder;
			Log.w("SelectActivity", "SelectActivity BINDING");
			mService = b.getInstance();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mBound = false;
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (mBound) {
			// We don't need mService.restart()
			// because this only changes apps
			mService.loadPreferences();
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppHolder ah = (AppHolder) mAppAdapter.getItem(position);
		if (mBound) {
			mService.setTracking(!ah.tracked, ah.ri.activityInfo.packageName);
		} else {
			Log.w("SelectActivity", "not bound");
		}

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
			MainActivity.showWithoutPassword(this);
			break;
		}
	}

	private void setAllTracking(boolean track) {
		if (mBound) {
			ArrayList<String> apps = new ArrayList<String>();
			for (AppHolder ah : mAppAdapter.getAllItems()) {
				apps.add(ah.ri.activityInfo.packageName);
			}
			mService.setTracking(track, apps.toArray(new String[apps.size()]));

			mAppAdapter.loadAppsIntoList(this);
			mAppAdapter.sort();
			mAppAdapter.notifyDataSetChanged();
		} else {
			Log.w("SelectActivity", "not bound");
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

}
