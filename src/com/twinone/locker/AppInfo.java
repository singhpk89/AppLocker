package com.twinone.locker;

import java.io.Serializable;

public class AppInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5216460479711908074L;

	public String packageName;
	public String className;
	public String label;
	public boolean locked;

	public AppInfo(String packageName) {
		init(packageName, null, true);
	}

	public AppInfo(String packageName, String label) {
		init(packageName, label, true);
	}

	public AppInfo(String packageName, String label, boolean lock) {
		init(packageName, label, lock);
	}

	public AppInfo(String packageName, boolean lock) {
		init(packageName, null, lock);
	}

	private void init(String packageName, String label, boolean lock) {
		this.packageName = packageName;
		this.label = label;
		this.locked = lock;
	}

	public AppInfo setClassName(String className) {
		this.className = className;
		return this;
	}

	public AppInfo setLock(boolean lock) {
		this.locked = lock;
		return this;
	}

	@Override
	public boolean equals(Object object) {

		if (object == null)
			return false;
		// Compare to another LockInfo
		if (object instanceof AppInfo) {
			return this.packageName.equals(((AppInfo) object).packageName);
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
}
