package com.twinone.locker.appselect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twinone.locker.R;
import com.twinone.locker.lock.AppLockService;
import com.twinone.locker.ui.MainActivity;
import com.twinone.locker.util.PrefUtils;

public class SelectActivity extends Activity {

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
		setContentView(R.layout.fragment_applist);

		mListView = (ListView) findViewById(R.id.lvAppList);
		mAppAdapter = new AppAdapter(this);
		mListView.setAdapter(mAppAdapter);
		// mListView.setOnItemClickListener(this);

		// bToggleAll = (Button) findViewById(R.id.bToggleAll);
		// bToggleAll.setOnClickListener(this);
		// bFinish = (Button) findViewById(R.id.bFinish);
		// bFinish.setOnClickListener(this);

		mEditor = PrefUtils.appsPrefs(this).edit();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppLockService.restart(this);
		finish();
		MainActivity.showWithoutPassword(this);
	}

}
