package com.twinone.locker.pro.pref;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ProEditTextPreference extends EditTextPreference {
	private Helper mHelper;

	public ProEditTextPreference(Context context) {
		super(context);
		mHelper = new Helper(context);
	}

	public ProEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHelper = new Helper(context);
	}

	@Override
	public View getView(View convertView, ViewGroup parent) {
		View v = super.getView(convertView, parent);
		return mHelper.getView(v);
	}

	@Override
	protected void onClick() {
		if (mHelper.onClick()) {
			super.onClick();
		}
	}

}
