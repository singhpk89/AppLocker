package com.twinone.locker;

import java.io.Serializable;

public class LockInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5216460479711908074L;

	public String packageName;
	public String className;
	public String label;
	public boolean locked;

	public LockInfo(String packageName) {
		init(packageName, null, true);
	}

	public LockInfo(String packageName, String label) {
		init(packageName, label, true);
	}

	public LockInfo(String packageName, String label, boolean lock) {
		init(packageName, label, lock);
	}

	public LockInfo(String packageName, boolean lock) {
		init(packageName, null, lock);
	}

	private void init(String packageName, String label, boolean lock) {
		this.packageName = packageName;
		this.label = label;
		this.locked = lock;
	}

	public LockInfo setClassName(String className) {
		this.className = className;
		return this;
	}

	public LockInfo setLock(boolean lock) {
		this.locked = lock;
		return this;
	}

	@Override
	public boolean equals(Object object) {

		if (object == null)
			return false;
		// Compare to another lockinfo
		if (object instanceof LockInfo) {
			return this.packageName.equals(((LockInfo) object).packageName);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return new StringBuilder("1191").append(packageName).toString()
				.hashCode();
	}

	@Override
	public String toString() {
		return "LI:" + (className == null ? packageName : className);
	}

	// @Override
	// public int compareTo(LockInfo o) {
	// // Log.d("TEST", this.label + " " + ((LockInfo) o).label + " "
	// // + this.label.compareTo(((LockInfo) o).label));
	// // if (this.label.equals("Contacts")
	// // || ((LockInfo) o).label.equals("Contacts")) {
	// // }
	// return this.label.compareTo(((LockInfo) o).label);
	//
	// // if (this.tracked != ((LockInfo) o).tracked) {
	// // return this.tracked ? -1 : 1;
	// // } else {
	// // return this.label.compareTo(((LockInfo) o).label);
	// // }
	// }
}
