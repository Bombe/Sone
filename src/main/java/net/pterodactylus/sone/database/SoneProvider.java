/*
 * Sone - SoneProvider.java - Copyright © 2011–2013 David Roden
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

package net.pterodactylus.sone.database;

import java.util.Collection;

import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Optional;

/**
 * Interface for objects that can provide {@link Sone}s by their ID.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneProvider {

	/**
	 * Returns the Sone with the given ID, or {@link Optional#absent()} if it
	 * does not exist.
	 *
	 * @param soneId
	 *            The ID of the Sone to return
	 * @return The Sone with the given ID, or {@link Optional#absent()}
	 */
	public Optional<Sone> getSone(String soneId);

	/**
	 * Returns all Sones.
	 *
	 * @return All Sones
	 */
	public Collection<Sone> getSones();

	/**
	 * Returns all local Sones.
	 *
	 * @return All local Sones
	 */
	public Collection<Sone> getLocalSones();

	/**
	 * Returns all remote Sones.
	 *
	 * @return All remote Sones
	 */
	public Collection<Sone> getRemoteSones();

}
