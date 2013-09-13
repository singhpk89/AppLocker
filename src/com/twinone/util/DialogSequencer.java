package com.twinone.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.Log;

/**
 * Utility class to display some Android dialogs in sequence.
 * 
 * Using this class your {@link DialogInterface.OnDismissListener} will be
 * remapped to {@link DialogSequenceListener#onDismiss(DialogInterface)}
 * 
 * @author Twinone
 * 
 */
public class DialogSequencer {

	// private Context mContext;
	private List<Dialog> mDialogs;
	private DialogSequenceListener mListener;
	private int mCurrentDialog = 0;

	public DialogSequencer() {
		// mContext = context;
		mDialogs = new ArrayList<Dialog>();
	}

	public DialogSequencer(DialogSequenceListener listener) {
		this();
		mListener = listener;
	}

	public void setListener(DialogSequenceListener listener) {
		mListener = listener;
	}

	public DialogSequencer addDialog(Dialog dialog) {
		mDialogs.add(dialog);
		dialog.setOnDismissListener(mOnDismissListener);
		return this;
	}

	public DialogSequencer addDialogs(Dialog... dialogs) {
		for (Dialog d : dialogs) {
			addDialog(d);
		}
		return this;
	}

	public DialogSequencer startDisplaying() {
		displayNext();
		return this;
	}

	private void displayNext() {
		if (mCurrentDialog == mDialogs.size()) {
			return;
		}
		Log.d("ODM", "Displaying dialog" + mCurrentDialog);
		Dialog d = mDialogs.get(mCurrentDialog);
		d.show();
		if (mListener != null) {
			mListener.onShow(d, mCurrentDialog);
		}
		mCurrentDialog++;
	}

	/**
	 * Don't show any more dialogs until {@link #startDisplaying()} is called
	 * again.
	 */
	public void cancel() {
		mCurrentDialog = mDialogs.size();
	}

	public void remove(int index) {
		if (index < 0 || index > mDialogs.size()) {
			return;
		}
		if (index < mCurrentDialog) {
			mCurrentDialog--;
		}
		mDialogs.remove(index);
	}

	public void gotoDialog(int index, boolean wait) {
		int oldIndex = mCurrentDialog;
		mCurrentDialog = index;
		if (!wait) {
			mDialogs.get(oldIndex).dismiss();
		}
	}

	public void gotoDialog(Dialog dialog, boolean wait) {
		gotoDialog(mDialogs.indexOf(dialog), wait);
	}

	public void insert(Dialog dialog, int index) {
		if (index <= mCurrentDialog) {
			mCurrentDialog++;
		}
		mDialogs.add(index, dialog);
	}

	/**
	 * Insert a dialog just after the currently displayed dialog
	 * 
	 * @param dialog
	 */
	public void insert(Dialog dialog) {
		mDialogs.add(mCurrentDialog + 1, dialog);
	}

	public void remove(Dialog d) {
		remove(mDialogs.indexOf(d));
	}

	public interface DialogSequenceListener {
		/**
		 * A dialog has been shown
		 * 
		 * @param dialog
		 * @param dialogId
		 *            The position of this dialog in the queue
		 */
		public void onShow(Dialog dialog, int position);

		public void onDismiss(DialogInterface dialog, int position);
	}

	private OnDismissListener mOnDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			Log.d("ODM", "Dismissed a dialog" + mDialogs.indexOf(dialog));
			if (mListener != null) {
				mListener.onDismiss(dialog, mCurrentDialog);
			}
			displayNext();
		}
	};
}
