/*
 * Copyright 2014 Luuk Willemsen (Twinone)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.twinone.automator;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * An event is a combination of one or more {@link Trigger}s and {@link Action}s<br>
 * 
 * 
 * @author twinone
 * 
 */
public class Event {

	/**
	 * This is comparable to an "or": When any of the triggers matches, this
	 * event will be executed
	 */
	public static final int TYPE_MATCH_ANY = 0x1 << 0;
	/**
	 * This is comparable to an "and": Only when all of the triggers match, this
	 * event will be executed
	 */
	public static final int TYPE_MATCH_ALL = 0x1 << 1;
	/**
	 * Set of triggers used for this {@link Event}. The behavior of the triggers
	 * is defined by {@link #type}
	 * 
	 * @see {@link #TYPE_MATCH_ANY}, {@link #TYPE_MATCH_ANY}
	 */
	@Expose
	public List<Trigger> triggers;

	/**
	 * When the triggers determine so, this actions will be executed.<br>
	 * They will be executed one by one, sequentially. If one action gives an
	 * error, subsequent actions will be cancelled and the user will get
	 * notified
	 */
	@Expose
	public List<Action> actions;

	/**
	 * The current type of event we're working with, default is
	 * {@link Event#TYPE_MATCH_ALL}
	 */
	@Expose
	public int type = TYPE_MATCH_ALL;

	public boolean matches(String[] criteria) {
		if (type == TYPE_MATCH_ALL) {
			for (Trigger trigger : triggers)
				if (!trigger.matches(criteria))
					return false;
			return true;
		} else if (type == TYPE_MATCH_ANY) {
			for (Trigger trigger : triggers)
				if (trigger.matches(criteria))
					return true;
			return false;
		}
		throw new IllegalStateException("Unknown Event type");
	}

	/*
	 * serializers/deserializers
	 */
	public static Event fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Event.class);
	}

	public String toJson() {
		return toJson(this);
	}

	public static String toJson(Event event) {
		Gson gson = new Gson();
		return gson.toJson(event, Event.class);
	}
}
