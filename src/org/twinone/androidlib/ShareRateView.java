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
package org.twinone.androidlib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twinone.locker.R;

/**
 * View to be used at the bottom of a navigation, to provide share and rate.
 * 
 * @author twinone
 * 
 */
public class ShareRateView extends LinearLayout {

	private LayoutInflater mInflater;

	public ShareRateView(Context context) {
		super(context);
		init();
	}

	public ShareRateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOrientation(VERTICAL);
		mInflater = LayoutInflater.from(getContext());
	}

	public View addItem(String text, int imageResId, OnClickListener listener) {
		final View v = mInflater.inflate(R.layout.share_rate_list_item, this,
				false);
		final ImageView iv = (ImageView) v
				.findViewById(R.id.share_rate_list_item_image);
		iv.setImageResource(imageResId);
		final TextView tv = (TextView) v
				.findViewById(R.id.share_rate_list_item_text);
		tv.setText(text);
		v.setOnClickListener(listener);
		addView(v);
		return v;
	}

	public View addItem(int id, String text, int imageResId,
			OnClickListener listener) {
		final View v = addItem(text, imageResId, listener);
		v.setId(id);
		return v;
	}

	public View addItem(int text, int imageResId, OnClickListener listener) {
		return addItem(getContext().getResources().getString(text), imageResId,
				listener);
	}

	public View addItem(int id, int text, int imageResId,
			OnClickListener listener) {
		final View v = addItem(getContext().getResources().getString(text),
				imageResId, listener);
		v.setId(id);
		return v;
	}

}
