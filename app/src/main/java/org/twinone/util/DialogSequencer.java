package org.twinone.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/**
 * Utility class to display some Android dialogs in sequence.
 * 
 * Using this class your {@link DialogInterface.OnDismissListener} will be
 * remapped to
 * 
 * @author Twinone
 * 
 */
public class DialogSequencer {

	// private Context mContext;
	private final List<DialogInterface> mDialogs;
	private DialogSequenceListener mListener;
	private int mCurrentDialog = 0;

	public DialogSequencer() {
		// mContext = context;
		mDialogs = new ArrayList<>();
	}

	public DialogSequencer(DialogSequenceListener listener) {
		this();
		mListener = listener;
	}

	public int size() {
		return mDialogs.size();
	}

	public void setListener(DialogSequenceListener listener) {
		mListener = listener;
	}

	/**
	 * @param dialog
	 *            The dialog to add. <br>
	 *            <b>Warning: </b> Don't use this {@link Dialog}'s
	 *            {@link Dialog#setOnDismissListener(OnDismissListener)} This
	 *            library needs the listener know when the dialog has been
	 *            dismissed. use
	 *            {@link DialogSequenceListener#onDismiss(DialogInterface, int)}
	 *            .
	 * @return
	 */
	public DialogSequencer addDialog(Dialog dialog) {
		if (dialog != null) {
			mDialogs.add(dialog);
			dialog.setOnDismissListener(mOnDismissListener);
		}
		return this;
	}

	public DialogSequencer addDialogs(Dialog... dialogs) {
		if (dialogs != null) {
			for (Dialog d : dialogs) {
				addDialog(d);
			}
		}
		return this;
	}

	/**
	 * Always call stop in {@link Activity#onResume()}, to hide dialogs that
	 * were still open.
	 * 
	 * <br>
	 * This will do nothing if there are no dialogs left
	 * 
	 * @return
	 */
	public DialogSequencer start() {
		displayNext();
		return this;
	}

	private void displayNext() {
		if (mCurrentDialog == mDialogs.size()) {
			return;
		}
		DialogInterface d = mDialogs.get(mCurrentDialog);
		((Dialog) d).show();

		if (mListener != null) {
			mListener.onShow(d, mCurrentDialog);
		}
		mCurrentDialog++;
	}

	/**
	 * Remove next dialog in the {@link DialogSequencer} <br>
	 * If you're running this from in a #{@link DialogInterface.OnClickListener}
	 * you can use the {@link DialogInterface} provided to you.
	 * 
	 * @param dialog
	 *            A reference to the current Dialog to determine its position
	 */
	public void removeNext(DialogInterface dialog) {
		mDialogs.remove(mDialogs.indexOf(dialog) + 1);
	}

	public int indexOf(DialogInterface dialog) {
		return mDialogs.indexOf(dialog);
	}

	/**
	 * Don't show any more dialogs.
	 */
    void cancel() {
		mCurrentDialog = mDialogs.size();

	}

	void remove(int index) {
		if (index < 0 || index > mDialogs.size()) {
			return;
		}
		if (index < mCurrentDialog) {
			mCurrentDialog--;
		}
		mDialogs.remove(index);
	}

	/**
	 * Go to this position in the list
	 * 
	 * @param index
	 * @param wait
	 *            False to go directly to the specified dialog, false to wait
	 *            until current dialog is dismissed.
	 */
    void gotoDialog(int index, boolean wait) {
		int oldIndex = mCurrentDialog;
		mCurrentDialog = index;
		if (!wait) {
			mDialogs.get(oldIndex).dismiss();
		}
	}

	/**
	 * Go to this dialog in the list
	 * 
	 * @param dialog
	 *            The dialog to go to, if the same dialog was added multiple
	 *            times, this will go to the first occurrence.
	 * @param wait
	 *            False to go directly to the specified dialog, false to wait
	 *            until current dialog is dismissed.
	 */
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

	public void stop() {
		cancel();
		for (DialogInterface d : mDialogs) {
			d.dismiss();
		}
	}

	public interface DialogSequenceListener {
		/**
		 * A dialog has been shown
		 * 
		 * @param dialog
         */
		public void onShow(DialogInterface dialog, int position);

		public void onDismiss(DialogInterface dialog, int position);
	}

	private final OnDismissListener mOnDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (mListener != null) {
				mListener.onDismiss(dialog, mCurrentDialog);
			}
			displayNext();
		}
	};
}
