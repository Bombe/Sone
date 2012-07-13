/*
 * Sone - SoneProvider.java - Copyright © 2011–2012 David Roden
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

package net.pterodactylus.sone.core;

import net.pterodactylus.sone.data.Sone;

/**
 * Interface for objects that can provide {@link Sone}s by their ID.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneProvider {

	/**
	 * Returns the Sone with the given ID, if it exists. If it does not exist
	 * and {@code create} is {@code false}, {@code null} is returned; otherwise,
	 * a new Sone with the given ID is created and returned.
	 *
	 * @param soneId
	 *            The ID of the Sone to return
	 * @param create
	 *            {@code true} to create a new Sone if no Sone with the given ID
	 *            exists, {@code false} to return {@code null} instead
	 * @return The Sone with the given ID, or {@code null}
	 */
	public Sone getSone(String soneId, boolean create);

}
