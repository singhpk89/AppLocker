package com.twinone.locker.automation;

import java.util.Arrays;

public class Event {
	/** Requires 1 String parameter, the SSID */
	public static final String SSID_MATCH = "event_ssid_match";
	public static final String WIFI_DISCONNECTED = "event_wifi_disconnected";

	public final String event;
	public final String[] values;

	public Event(String event) {
		this.event = event;
		values = null;
	}

	public Event(String event, String[] values) {
		this.event = event;
		this.values = values;
	}

	public static final Event fromString(String fromString) {
		String[] split = fromString.split(":");
		// TODO out of bounds
		return new Event(split[0], Arrays.copyOfRange(split, 1,
				split.length - 1));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(event);
		return sb.toString();
		
	}
}