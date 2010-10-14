/*
 * Sone - ShellCache.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.data;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Shell}-aware cache that will replace {@link Shell}s with the real
 * objects but not the other way around.
 *
 * @param <T>
 *            The type of the cached objects
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ShellCache<T> {

	/** The object cache. */
	private final Map<String, T> cache = new HashMap<String, T>();

	/**
	 * Stores the given object in this cache. If the given object is not a
	 * {@link Shell}, it is stored. If it is a {@link Shell} it is only stored
	 * if there is no object stored for the given ID.
	 *
	 * @param id
	 *            The ID of the object
	 * @param object
	 *            The object to store
	 */
	public void put(String id, T object) {
		if (!(object instanceof Shell<?>) || !cache.containsKey(id)) {
			cache.put(id, object);
		}
	}

	/**
	 * Returns the post with the given ID.
	 *
	 * @param id
	 *            The ID of the post
	 * @return The post with the given ID, or {@code null} if there is no post
	 *         with the given ID
	 */
	public T get(String id) {
		return cache.get(id);
	}

}
