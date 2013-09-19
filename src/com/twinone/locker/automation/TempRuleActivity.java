package com.twinone.locker.automation;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.twinone.locker.MainActivity;
import com.twinone.locker.R;

public class TempRuleActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager pm = getPreferenceManager();
		pm.setSharedPreferencesName("beta");
		pm.setSharedPreferencesMode(MODE_PRIVATE);
		addPreferencesFromResource(R.xml.temp_rules);
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.beta_tit);
		ab.setCancelable(false);
		ab.setMessage(R.string.beta_desc);
		ab.setPositiveButton(android.R.string.ok, null);
		ab.show();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MainActivity.showWithoutPassword(this);
	}

}
