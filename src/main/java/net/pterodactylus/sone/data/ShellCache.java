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

import java.util.Collection;
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
	private final Map<String, T> objectCache = new HashMap<String, T>();

	/** The shell cache. */
	private final Map<String, Shell<T>> shellCache = new HashMap<String, Shell<T>>();

	/** The shell creator. */
	private final ShellCreator<T> shellCreator;

	/**
	 * Creates a new shell cache.
	 *
	 * @param shellCreator
	 *            The creator for new shells
	 */
	public ShellCache(ShellCreator<T> shellCreator) {
		this.shellCreator = shellCreator;
	}

	//
	// ACCESSORS
	//

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
	@SuppressWarnings("unchecked")
	public void put(String id, T object) {
		if (!(object instanceof Shell<?>)) {
			objectCache.put(id, object);
			shellCache.remove(id);
		} else {
			shellCache.put(id, (Shell<T>) object);
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
		if (!objectCache.containsKey(id)) {
			Shell<T> shell = shellCreator.createShell();
			shellCache.put(id, shell);
			return shell.getShelled();
		}
		return objectCache.get(id);
	}

	/**
	 * Returns all cached shells.
	 *
	 * @return All cached shells
	 */
	public Collection<Shell<T>> getShells() {
		return shellCache.values();
	}

}
