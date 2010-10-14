/*
 * Sone - Shell.java - Copyright © 2010 David Roden
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

/**
 * Marker interface for classes that are only empty shells for objects that have
 * not yet been retrieved from Freenet.
 *
 * @param <T>
 *            The type of object that is shelled
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Shell<T> {

	/**
	 * Returns whether the shelled object can be unshelled now.
	 *
	 * @return {@code true} if {@link #getShelled()} can return the shelled
	 *         object, {@code false} otherwise
	 */
	public boolean canUnshell();

	/**
	 * Returns the object that is shelled. Returns {@code null} if the real
	 * object has not yet been retrieved.
	 *
	 * @return The shelled object, or {@code null} if the shelled object is not
	 *         retrieved yet
	 */
	public T getShelled();

}
