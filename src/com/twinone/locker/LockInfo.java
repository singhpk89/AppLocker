package com.twinone.locker;

import java.io.Serializable;

public class LockInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5216460479711908074L;

	public final String packageName;
	public String className;
	public String password;
	public boolean lock;

	public LockInfo(String packageName, String password) {
		this.packageName = packageName;
		this.password = password;
		lock = true;
	}

	public LockInfo(String packageName, String password, boolean lock) {
		this.packageName = packageName;
		this.password = password;
		this.lock = lock;
	}

	public LockInfo setClassName(String className) {
		this.className = className;
		return this;
	}

	public LockInfo setLock(boolean lock) {
		this.lock = lock;
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

}
