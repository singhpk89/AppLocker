package com.twinone.locker.pro.pref;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ProListPreference extends ListPreference implements IProPreference {
	private Helper mHelper;

	public Helper getHelper() {
		return mHelper;
	}

	public ProListPreference(Context context) {
		super(context);
		mHelper = new Helper(context);
	}

	public ProListPreference(Context context, AttributeSet attrs) {
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
