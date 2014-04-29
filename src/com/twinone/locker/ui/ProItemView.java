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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twinone.locker.R;

public class ProItemView extends LinearLayout {

	private TextView mTitleView;
	private TextView mDescriptionView;

	private String mTitle;
	private String mDescription;
	private int mTitleColor = R.color.pro_item_tit;
	private int mDescriptionColor = R.color.pro_item_desc;

	public ProItemView(Context c) {
		super(c);
	}

	public ProItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.pro_item, this, true);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ProItemView);
		try {
			mTitle = a.getString(R.styleable.ProItemView_proTitle);
			mDescription = a.getString(R.styleable.ProItemView_proDescription);
			mTitleColor = a.getColor(R.styleable.ProItemView_proTitleColor, -1);
		} finally {
			a.recycle();
		}
	}

	public void setTitleColor(int resid) {
		if (resid != 0) {
			mTitleView.setTextColor(getResources().getColor(resid));
		}
	}

	public void setDescriptionColor(int resid) {
		if (resid != 0) {
			mTitleView.setTextColor(getResources().getColor(resid));
		}
	}

	public void setTitle(String title) {
		mTitleView.setText(title);
	}

	public void setDescription(String description) {
		mDescriptionView.setText(description);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitleView = (TextView) findViewById(R.id.pro_item_title);
		mDescriptionView = (TextView) findViewById(R.id.pro_item_description);

		setTitle(mTitle);
		setDescription(mDescription);
		mTitleView.setTextColor(mTitleColor);
		mDescriptionView.setTextColor(mDescriptionColor);
	}

	public void setSelectedLockType(boolean selected) {
		if (selected) {
			setBackgroundResource(R.drawable.item_selected);
			mTitleView.setTextColor(getResources().getColor(
					R.color.pro_item_selected_tit));
			mTitleView.setTypeface(null, Typeface.BOLD);
			mDescriptionView.setTextColor(getResources().getColor(
					R.color.pro_item_selected_desc));
		} else {
			setBackgroundResource(R.drawable.item_not_selected);
			mTitleView.setTypeface(null, Typeface.NORMAL);
			mTitleView.setTextColor(getResources().getColor(
					R.color.pro_item_tit));
			mDescriptionView.setTextColor(getResources().getColor(
					R.color.pro_item_desc));
		}
	}
}
