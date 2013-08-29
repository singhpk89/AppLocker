package com.twinone.locker.prefs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.MainActivity;
import com.twinone.locker.ObserverService;
import com.twinone.locker.ObserverService.LocalBinder;
import com.twinone.locker.R;
import com.twinone.locker.prefs.AppAdapter.AppHolder;

public class SelectActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	ListView mListView;
	AppAdapter mAppAdapter;
	Button bAll;
	Button bNone;

	private ObserverService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Dark);
		setContentView(R.layout.activity_applist);

		mListView = (ListView) findViewById(R.id.lvAppList);
		mAppAdapter = new AppAdapter(this);
		mListView.setAdapter(mAppAdapter);
		mListView.setOnItemClickListener(this);

		bAll = (Button) findViewById(R.id.bLockAll);
		bNone = (Button) findViewById(R.id.bUnlockAll);
		bAll.setOnClickListener(this);
		bNone.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent(this, ObserverService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder binder) {
			LocalBinder b = (LocalBinder) binder;
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
