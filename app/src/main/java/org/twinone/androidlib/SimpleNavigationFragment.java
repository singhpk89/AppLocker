package org.twinone.androidlib;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SimpleNavigationFragment extends NavigationFragment {

	private ListView mListView;
	private NavigableAdapter mAdapter;

	private int mListViewResId;
	private int mLayoutResId;

	private int mItemLayoutResId;
	private int mItemTextId;
	private int mItemImageId;

	public void setupCustomItemLayout(int layoutResId, int textViewResId,
			int imageViewResId) {
		mItemLayoutResId = layoutResId;
		mItemTextId = textViewResId;
		mItemImageId = imageViewResId;
	}

	public void setupCustomLayouts(int layoutResId, int listViewResId) {
		mListViewResId = listViewResId;
		mLayoutResId = layoutResId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = null;
		if (mLayoutResId != 0 && mListViewResId != 0) {
			v = inflater.inflate(mLayoutResId, container, false);
			mListView = (ListView) v.findViewById(mListViewResId);
		} else {
			mListView = new ListView(getActivity());
			v = mListView;
		}

		if (mAdapter != null) {
			mListView.setAdapter(mAdapter);
		}

		if (mOnItemClickListener != null)
			mListView.setOnItemClickListener(mOnItemClickListener);
		return v;
	}

	private OnItemClickListener mOnItemClickListener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
		if (mListView != null)
			mListView.setOnItemClickListener(mOnItemClickListener);
	}

	public void setItems(List<Navigable> items) {
		mAdapter = new NavigableAdapter(getActivity());
		mAdapter.setItems(items);
		if (mListView != null) {
			mListView.setAdapter(mAdapter);
		}
	}

	public NavigableAdapter getAdapter() {
		return mAdapter;
	}

	private boolean mShowImages;

	public void setShowImages(boolean showImages) {
		mShowImages = showImages;
	}

	private List<Navigable> mItems;

	public Navigable getItem(int position) {
		return (Navigable) mAdapter.getItem(position);

	}

	private class NavigableAdapter extends BaseAdapter {

		private final Context mContext;

		public void setItems(List<Navigable> items) {
			mItems = items;
		}

		public NavigableAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View root = convertView;

			Navigable item = mItems.get(position);
			if (root == null) {
				root = LayoutInflater.from(mContext).inflate(mItemLayoutResId,
						parent, false);
			}

			TextView tv = (TextView) root.findViewById(mItemTextId);
			tv.setText(item.text);
			ImageView iv = (ImageView) root.findViewById(mItemImageId);
			if (mShowImages) {
				int res = item.imageResId;
				if (res != 0) {
					iv.setVisibility(View.VISIBLE);
					iv.setImageResource(res);
				} else {
					iv.setVisibility(View.INVISIBLE);
				}
			} else {
				iv.setVisibility(View.GONE);
			}

			return root;
		}
	}

}
