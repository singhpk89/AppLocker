package com.twinone.locker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class LockTestActivity extends Activity {
	RelativeLayout rl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_locker);

		rl = (RelativeLayout) findViewById(R.id.rlContainer);
		rl.setBackgroundColor(Color.RED);
		
	}

	@Override
	protected void onResume() {
		super.onResume();

//		Animation a = AnimationUtils.loadAnimation(this,
//				android.R.anim.fade_out);
//		rl.startAnimation(a);

	}

}
