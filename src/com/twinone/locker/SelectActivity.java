package com.twinone.locker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.twinone.locker.AppAdapter.AppHolder;
import com.twinone.locker.ObserverService.LocalBinder;

public class SelectActivity extends Activity implements OnItemClickListener {

	ListView mListView;
	AppAdapter mAppAdapter;

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
	}

	@Override
	protected void onStart() {
		super.onStart();
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
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	// Go back to main activity.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AppHolder ah = (AppHolder) mAppAdapter.getItem(position);

		if (mBound) {
			mService.setTracking(ah.ri.activityInfo.packageName, !ah.tracked);
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
}
