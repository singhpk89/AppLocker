package com.twinone.locker.version;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class VersionInfo implements Serializable {

	/***/
	public transient static final long serialVersionUID = -2984202672417839008L;
	/** Matches if device version is equal to specified version */
	public static final String CRITERIA_EQ = "eq";
	/** Matches if device version is different to specified version */
	public static final String CRITERIA_NE = "ne";
	/** Matches if device version is less than specified version */
	public static final String CRITERIA_LT = "lt";
	/** Matches if device version is greater than specified version */
	public static final String CRITERIA_GT = "gt";
	/** Matches if device version is less than or equal to specified version */
	public static final String CRITERIA_LE = "le";
	/** Matches if device version is greater than or equal to specified version */
	public static final String CRITERIA_GE = "ge";

	/**
	 * Versions this {@link VersionInfo} applies to
	 */
	@Expose
	public int version;
	/**
	 * Will be used like this:<br>
	 * installedVersion criteria targetVersion
	 */
	@Expose
	public String criteria;
	@Expose
	public boolean deprecated;
	@Expose
	public String message;

	/**
	 * @return true if this VersionInfo is applicable to manifestVersion
	 */
	public boolean isApplicable(int manifestVersion) {
		if (criteria == null)
			return false;
		if (CRITERIA_EQ.equals(criteria)) {
			return manifestVersion == version;
		} else if (CRITERIA_NE.equals(criteria)) {
			return manifestVersion != version;
		} else if (CRITERIA_LT.equals(criteria)) {
			return manifestVersion < version;
		} else if (CRITERIA_GT.equals(criteria)) {
			return manifestVersion > version;
		} else if (CRITERIA_LE.equals(criteria)) {
			return manifestVersion <= version;
		} else if (CRITERIA_GE.equals(criteria)) {
			return manifestVersion >= version;
		}
		return false;
	}
}