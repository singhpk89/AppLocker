package com.twinone.locker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class SelectActivity extends Activity {

	ListView mListView;
	AppAdapter mAppAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_applist);
		
		mListView = (ListView)findViewById(R.id.lvAppList);
		mAppAdapter = new AppAdapter(this);
		mListView.setAdapter(mAppAdapter);
		
		
	}

	// Go back to main activity.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}
}
