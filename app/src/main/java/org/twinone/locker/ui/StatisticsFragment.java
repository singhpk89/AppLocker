package org.twinone.locker.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twinone.locker.R;

public class StatisticsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_statistics, container,
				false);
		

		getActivity().setTitle(R.string.fragment_title_statistics);
		return root;
	}

}
