/*
 * FreenetSone - Profile.java - Copyright © 2010 David Roden
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
 * A profile stores personal information about a {@link User}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Profile {

	/** The name of the user this profile belongs to. */
	private final String username;

	/**
	 * Creates a new profile.
	 *
	 * @param username
	 *            The name of the user this profile belongs to
	 */
	public Profile(String username) {
		this.username = username;
	}

	/**
	 * Returns the name of the user this profile belongs to.
	 *
	 * @return The name of the user this profile belongs to
	 */
	public String getUsername() {
		return username;
	}

}
