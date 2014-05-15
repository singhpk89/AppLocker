/*
 * Copyright 2014 Luuk Willemsen (Twinone)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.twinone.locker.ui;

import com.twinone.locker.R;
import com.twinone.locker.pro.ProUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProFragment extends Fragment implements OnClickListener {
	private ProItemView mPvFree;
	private ProItemView mPvAds;
	private ProItemView mPvPaid;
	private LinearLayout mLlFeatures;
	private ProUtils mProUtils;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProUtils = new ProUtils(getActivity());
	}

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {

		getActivity().setTitle(R.string.fragment_title_pro);
		
		View root = inflater.inflate(R.layout.fragment_pro, container, false);
		mLlFeatures = (LinearLayout) root.findViewById(R.id.prollFeatures);
		mPvFree = (ProItemView) root.findViewById(R.id.proItemFree);
		mPvAds = (ProItemView) root.findViewById(R.id.proItemAds);
		mPvPaid = (ProItemView) root.findViewById(R.id.proItemPaid);

		mPvFree.setOnClickListener(this);
		mPvAds.setOnClickListener(this);
		mPvPaid.setOnClickListener(this);
		mPvPaid.setVisibility(View.GONE);

		String[] fts = getResources()
				.getStringArray(R.array.pro_features_array);
		for (String ft : fts) {
			TextView tv = new TextView(getActivity());
			tv.setText(Html.fromHtml("&#8226; " + ft));
			mLlFeatures.addView(tv);
		}
		return root;
	}

	public void onResume() {
		super.onResume();
		updateLayout();
	}

	private void updateLayout() {
		int type = mProUtils.getProType();

		mPvFree.setSelectedLockType(type == ProUtils.TYPE_FREE);
		mPvAds.setSelectedLockType(type == ProUtils.TYPE_ADS);
		mPvPaid.setSelectedLockType(type == ProUtils.TYPE_PAID);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.proItemFree:
			changeToFree();
			break;
		case R.id.proItemAds:
			changeTo(ProUtils.TYPE_ADS);
			break;

		// case R.id.proItemPaid:
		// changeTo(ProUtils.TYPE_PAID);
		// break;
		}
	}

	/** Sets the preference */
	private void changeTo(int type) {
		mProUtils.setProType(type);
		updateLayout();
	}

	/**
	 * Show a dialog if the user is on pro, so he will not accidentally remove
	 * pro features
	 */
	private void changeToFree() {
		if (!mProUtils.proFeaturesEnabled()) {
			changeTo(ProUtils.TYPE_FREE);
			return;
		}
		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(R.string.pro_tofree_tit);
		ab.setMessage(R.string.pro_tofree_msg);
		ab.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						changeTo(ProUtils.TYPE_FREE);
					}
				});
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.show();
	}

}
