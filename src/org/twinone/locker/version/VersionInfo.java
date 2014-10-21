package org.twinone.locker.version;

import java.io.Serializable;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class VersionInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6317794712430020731L;

	@Expose
	/**
	 * True if this version is not longer supported after deprecation_time
	 */
	public Boolean deprecated;

	@Expose
	/** The time at which the version will not longer be usable */
	public Long deprecation_time;

	@Expose
	/** The current server-time to ensure clock is right, never null */
	public Long server_time;
	@Expose
	/**
	 * The time at which we should start warning the user he has to update
	 */
	public Long warn_time;
	/**
	 * Used for server migrations <br>
	 * If this is not null, the app will no longer check the original url, but
	 * instead it will check this one. <br>
	 * Use with great care. If you put in the wrong url, the only way to update
	 * it is to upload a new app version
	 */
	public String new_url;
	/**
	 * Custom values that are passed to the user, may be null if the server
	 * didn't have values
	 */
	@Expose
	public Map<String, String> values;

}